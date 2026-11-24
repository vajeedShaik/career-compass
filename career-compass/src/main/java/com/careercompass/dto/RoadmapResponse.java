package com.careercompass.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RoadmapResponse(
        UUID id,
        UUID matchId,
        String roadmapContent,
        List<ResourceUsed> resourcesUsed,
        boolean emailSent,
        Instant createdAt
) {
    public record ResourceUsed(String skill, String title, String url) {}
}
