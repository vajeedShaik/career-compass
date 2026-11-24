package com.careercompass.dto;

import java.util.List;
import java.util.Map;

/**
 * Structured content for the full report. Category is computed
 * deterministically in Java from the match score (not AI-generated, to keep
 * it consistent and auditable). Depending on whether the match score is
 * above or below the configured threshold, either roadmapBlocks OR
 * interviewPrepSections will be populated — never both.
 */
public record ReportContent(
        String aiSummary,
        List<String> strengths,
        List<String> weaknesses,
        List<String> prioritySkills,
        List<RoadmapBlock> roadmapBlocks,                    // populated only below threshold
        List<InterviewPrepSection> interviewPrepSections,    // populated only at/above threshold
        Map<String, List<String>> practiceQuestionsPerSkill,
        List<TimelineWeek> timeline,
        String finalRecommendation,
        String estimatedTime,
        String nextActionPlan
) {
    public record RoadmapBlock(
            String skill,
            String difficulty,      // e.g. "Easy", "Medium", "Hard" — AI estimate
            String durationWeeks,   // e.g. "2 weeks" — AI estimate
            String prerequisites,   // e.g. "Java basics" or "None"
            String guidance
    ) {}

    public record InterviewPrepSection(
            String heading,
            String content
    ) {}

    public record TimelineWeek(
            int week,
            String focus,
            String milestone
    ) {}
}
