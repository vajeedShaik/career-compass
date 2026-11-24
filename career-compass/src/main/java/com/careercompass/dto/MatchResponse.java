package com.careercompass.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MatchResponse(
        UUID id,
        UUID resumeId,
        UUID jdId,
        BigDecimal matchScore,
        List<String> matchedSkills,
        List<String> missingSkills,
        String scoringNotes,
        Instant createdAt
) {}
