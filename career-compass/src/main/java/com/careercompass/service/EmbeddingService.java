package com.careercompass.service;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * Generates vector embeddings locally using a bundled ONNX export of
 * sentence-transformers/all-MiniLM-L6-v2, run via ONNX Runtime, with
 * tokenization handled by DJL's HuggingFace tokenizer bindings.
 *
 * No external API, no API key, no network call at request time — the
 * model and tokenizer files are packaged on the classpath and loaded
 * once at startup.
 *
 * IMPORTANT: this model produces 384-dimensional embeddings, matching the
 * existing vector(384) column defined in V1__init_schema.sql, so no Flyway
 * migration is needed for this change. If you ever swap to a different
 * model with a different hidden size, treat it as a two-part change: this
 * config AND a new migration to ALTER the column type, together.
 */
@Service
public class EmbeddingService {

    private static final String MODEL_ONNX_CLASSPATH = "models/all-MiniLM-L6-v2/model.onnx";
    private static final String TOKENIZER_CLASSPATH = "models/all-MiniLM-L6-v2/tokenizer.json";
    private static final int MAX_SEQUENCE_LENGTH = 256;

    private final int dimensions;

    private OrtEnvironment env;
    private OrtSession session;
    private HuggingFaceTokenizer tokenizer;

    public EmbeddingService(@Value("${app.embedding.dimensions:384}") int dimensions) {
        this.dimensions = dimensions;
    }

    @PostConstruct
    public void init() throws IOException, OrtException {
        env = OrtEnvironment.getEnvironment();
        Path modelPath = extractToTempFile(
                MODEL_ONNX_CLASSPATH,
                "onnx-model",
                ".onnx"
        );

        session = env.createSession(
                modelPath.toString(),
                new OrtSession.SessionOptions()
        );
        Path tokenizerPath = extractToTempFile(TOKENIZER_CLASSPATH, "tokenizer", ".json");
        tokenizer = HuggingFaceTokenizer.builder()
                .optTokenizerPath(tokenizerPath)
                .optAddSpecialTokens(true)
                .optTruncation(true)
                .optMaxLength(MAX_SEQUENCE_LENGTH)
                .build();
    }

    private Path extractToTempFile(String classpathLocation, String prefix, String suffix) throws IOException {
        Path tempFile = Files.createTempFile(prefix, suffix);
        tempFile.toFile().deleteOnExit();
        try (InputStream in = new ClassPathResource(classpathLocation).getInputStream()) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }

    public float[] embed(String text) {
        try {
            Encoding encoding = tokenizer.encode(text);
            long[] ids = encoding.getIds();
            long[] attentionMask = encoding.getAttentionMask();
            long[] typeIds = encoding.getTypeIds();

            long[][] inputIds = {ids};
            long[][] attMask = {attentionMask};
            long[][] tokenTypeIds = {typeIds};

            try (OnnxTensor inputIdsTensor = OnnxTensor.createTensor(env, inputIds);
                 OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(env, attMask);
                 OnnxTensor tokenTypeIdsTensor = OnnxTensor.createTensor(env, tokenTypeIds)) {

                Map<String, OnnxTensor> inputs = Map.of(
                        "input_ids", inputIdsTensor,
                        "attention_mask", attentionMaskTensor,
                        "token_type_ids", tokenTypeIdsTensor
                );

                try (OrtSession.Result result = session.run(inputs)) {
                    // First output of the optimum-exported model is last_hidden_state:
                    // shape [1, seq_len, hidden_size]
                    float[][][] tokenEmbeddings = (float[][][]) result.get(0).getValue();
                    float[] pooled = meanPool(tokenEmbeddings[0], attentionMask);
                    float[] normalized = l2Normalize(pooled);

                    if (normalized.length != dimensions) {
                        throw new IllegalStateException(
                                "Expected %d-dimensional embedding but model produced %d — check the bundled ONNX model, " +
                                        "and remember the database column is a fixed vector(%d) type regardless of this config"
                                                .formatted(dimensions, normalized.length, dimensions));
                    }
                    return normalized;
                }
            }
        } catch (OrtException e) {
            throw new IllegalStateException("Failed to generate local embedding: " + e.getMessage(), e);
        }
    }

    /** Mean-pools token embeddings over non-padding tokens, per the sentence-transformers pooling strategy. */
    private float[] meanPool(float[][] tokenEmbeddings, long[] attentionMask) {
        int hiddenSize = tokenEmbeddings[0].length;
        float[] summed = new float[hiddenSize];
        float maskSum = 0f;

        for (int t = 0; t < tokenEmbeddings.length; t++) {
            float mask = attentionMask[t];
            if (mask == 0f) continue;
            for (int h = 0; h < hiddenSize; h++) {
                summed[h] += tokenEmbeddings[t][h] * mask;
            }
            maskSum += mask;
        }

        if (maskSum == 0f) maskSum = 1e-9f;

        float[] pooled = new float[hiddenSize];
        for (int h = 0; h < hiddenSize; h++) {
            pooled[h] = summed[h] / maskSum;
        }
        return pooled;
    }

    private float[] l2Normalize(float[] vector) {
        double sumSquares = 0.0;
        for (float v : vector) sumSquares += (double) v * v;
        double norm = Math.sqrt(sumSquares);
        if (norm < 1e-12) norm = 1e-12;

        float[] normalized = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = (float) (vector[i] / norm);
        }
        return normalized;
    }

    public String toPgVectorLiteral(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    @PreDestroy
    public void close() {
        try {
            if (session != null) session.close();
        } catch (OrtException ignored) {
            // best-effort cleanup on shutdown
        }
        if (env != null) env.close();
    }
}