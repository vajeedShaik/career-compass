package com.careercompass.dto;

import java.util.List;

public record JinaEmbeddingRequest(
        String model,
        String task,
        int dimensions,
        List<String> input
) {}