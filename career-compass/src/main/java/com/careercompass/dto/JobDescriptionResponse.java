package com.careercompass.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JobDescriptionResponse(
        UUID id,
        String title,
        String companyName,
        List<String> requiredSkills,
        String minEducationLevel,
        java.math.BigDecimal minYearsExperience,
        Instant createdAt
) {}
