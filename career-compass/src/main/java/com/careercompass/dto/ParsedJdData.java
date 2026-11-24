package com.careercompass.dto;

import java.util.List;

/**
 * Shape of the structured data extracted from a job description.
 */
public record ParsedJdData(
        String companyName,           // null if not mentioned — never guessed
        List<String> requiredSkills,
        String minEducationLevel,
        java.math.BigDecimal minYearsExperience
) {}
