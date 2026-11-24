package com.careercompass.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ResumeResponse(
        UUID id,
        String originalFilename,
        String candidateName,
        List<String> extractedSkills,
        List<String> softSkills,
        List<String> certifications,
        String educationLevel,
        java.math.BigDecimal yearsOfExperience,
        List<ParsedResumeData.ProjectData> projects,
        Instant createdAt
) {}
