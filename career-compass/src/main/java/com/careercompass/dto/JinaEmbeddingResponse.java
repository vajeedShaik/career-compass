package com.careercompass.dto;

import java.util.List;

public record JinaEmbeddingResponse(List<Data> data) {
    public record Data(float[] embedding) {}
}