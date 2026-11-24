package com.careercompass.dto;

import java.util.List;

/**
 * Shape of the structured data extracted from resume text.
 *
 * candidateName IS now extracted (as of the report feature) — a deliberate,
 * scoped exception to the earlier no-PII rule: it's used only for display
 * in the report emailed back to the resume's own owner, never in scoring
 * or shared with any other party. Email/phone remain excluded — no use case
 * for them exists yet.
 */
public record ParsedResumeData(
        String candidateName,        // null if not confidently found — never guessed
        List<String> skills,
        List<String> softSkills,     // e.g. "communication", "teamwork" — empty list if none found
        List<String> certifications, // empty list if none found — never invented
        String educationLevel,
        java.math.BigDecimal yearsOfExperience,
        List<ProjectData> projects
) {
    public record ProjectData(
            String name,
            String description,
            List<String> technologies
    ) {}
}
