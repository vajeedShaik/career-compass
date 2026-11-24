package com.careercompass.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record HistoryResponse(
        List<ResumeSummary> resumes,
        List<JobSummary> jobDescriptions,
        List<MatchSummary> matches
) {
    public record ResumeSummary(UUID id, String originalFilename, Instant createdAt) {}
    public record JobSummary(UUID id, String title, String companyName, Instant createdAt) {}
    public record MatchSummary(UUID id, BigDecimal matchScore, Instant createdAt) {}
}