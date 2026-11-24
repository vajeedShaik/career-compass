package com.careercompass.service;

import com.careercompass.dto.ReportContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Generates the narrative content for the full report in one structured AI
 * call, branching into either a beginner learning roadmap or an interview
 * preparation plan depending on the match score vs the configured
 * threshold. Interview questions generated here are general AI-written
 * practice questions, explicitly not presented as sourced/leaked questions
 * from any real company or interview.
 *
 * Production hardening: prompts instruct the model to return strict JSON
 * only, the raw response is logged before parsing, malformed/truncated
 * responses are recovered from where possible (fence stripping, JSON
 * extraction), and the AI call is retried once before failing loudly with
 * the raw response included in the thrown exception.
 */
@Service
public class ReportContentService {

    private static final Logger log = LoggerFactory.getLogger(ReportContentService.class);

    private static final int MAX_PRIORITY_SKILLS = 8;
    private static final int MAX_TIMELINE_WEEKS = 8;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final BigDecimal matchThreshold;

    public ReportContentService(ChatClient.Builder chatClientBuilder,
                                ObjectMapper objectMapper,
                                @Value("${app.report.match-threshold}") BigDecimal matchThreshold) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.matchThreshold = matchThreshold;
    }

    public boolean isHighMatch(BigDecimal matchScore) {
        return matchScore.compareTo(matchThreshold) >= 0;
    }

    public String categoryFor(BigDecimal matchScore) {
        double s = matchScore.doubleValue();
        if (s >= 85) return "Excellent";
        if (s >= 70) return "Good";
        if (s >= 50) return "Moderate";
        return "Needs Improvement";
    }

    private static final String LOW_MATCH_PROMPT = """
            Write JSON content for a candidate's career assessment report.

            Match score: %s / 100
            Matched skills: %s
            Priority missing skills (max %d, in learning order): %s
            Education: candidate has %s, role requires %s
            Experience: candidate has %s years, role requires %s years

            Score is BELOW the readiness threshold: include a beginner learning roadmap, not interview prep.

            Return ONLY this JSON shape, no other text:
            {
              "aiSummary": "2-3 sentences on why the candidate got this score",
              "strengths": ["...", "..."],
              "weaknesses": ["...", "..."],
              "prioritySkills": ["2-3 most important missing skills to learn first"],
              "roadmapBlocks": [
                {"skill": "...", "difficulty": "Easy|Medium|Hard", "durationWeeks": "e.g. 2 weeks",
                 "prerequisites": "e.g. Java basics, or None", "guidance": "1-2 sentences of practical advice"}
              ],
              "interviewPrepSections": [],
              "practiceQuestionsPerSkill": {"skillName": ["question 1", "question 2"]},
              "timeline": [
                {"week": 1, "focus": "focus for this week", "milestone": "expected outcome by week end"}
              ],
              "finalRecommendation": "1-2 sentence honest recommendation",
              "estimatedTime": "e.g. 6-8 weeks",
              "nextActionPlan": "1-2 sentences on the immediate next step"
            }

            Rules:
            - One roadmapBlock per priority missing skill listed above, foundational skills first.
            - practiceQuestionsPerSkill: only include entries for the priority missing skills listed above.
            - timeline: at most %d weeks total, covering estimatedTime.
            - Practice questions are general study questions YOU write — never claim they are real questions
              from a specific company or actual past interview.
            - Be honest and specific, grounded only in the data given above. Do not invent skills, scores, or facts.
            - Return ONLY valid JSON: no markdown fences, no explanations, no trailing commas, no comments.
            """;

    private static final String HIGH_MATCH_PROMPT = """
            Write JSON content for a candidate's career assessment report.

            Match score: %s / 100
            Matched skills: %s
            Priority missing skills (max %d): %s
            Education: candidate has %s, role requires %s
            Experience: candidate has %s years, role requires %s years

            Score is AT OR ABOVE the readiness threshold: include an interview preparation plan, NOT a
            beginner learning roadmap.

            Return ONLY this JSON shape, no other text:
            {
              "aiSummary": "2-3 sentences on why the candidate got this score",
              "strengths": ["...", "..."],
              "weaknesses": ["...", "..."],
              "prioritySkills": ["1-3 areas most worth polishing before the interview"],
              "roadmapBlocks": [],
              "interviewPrepSections": [
                {"heading": "Interview Preparation Strategy", "content": "..."},
                {"heading": "Important Java Topics", "content": "..."},
                {"heading": "Spring Boot Revision Plan", "content": "..."},
                {"heading": "REST API Checklist", "content": "..."},
                {"heading": "SQL Revision Checklist", "content": "..."},
                {"heading": "Behavioral Interview Tips", "content": "..."},
                {"heading": "HR Interview Preparation", "content": "..."},
                {"heading": "Resume Improvement Suggestions", "content": "..."},
                {"heading": "Portfolio Improvement Suggestions", "content": "..."},
                {"heading": "GitHub Improvement Suggestions", "content": "..."},
                {"heading": "LinkedIn Improvement Suggestions", "content": "..."},
                {"heading": "Mock Interview Plan", "content": "..."},
                {"heading": "Final Readiness Checklist", "content": "..."}
              ],
              "practiceQuestionsPerSkill": {"skillName": ["question 1", "question 2"]},
              "timeline": [
                {"week": 1, "focus": "focus for this week", "milestone": "expected outcome by week end"}
              ],
              "finalRecommendation": "1-2 sentence honest recommendation",
              "estimatedTime": "e.g. 1-2 weeks of focused prep",
              "nextActionPlan": "1-2 sentences on the immediate next step"
            }

            Rules:
            - Cover every heading listed above, 2-4 concise sentences each.
            - practiceQuestionsPerSkill: only include entries for the priority missing skills listed above.
            - timeline: at most %d weeks total.
            - Practice questions are general study questions YOU write — never claim they are real questions
              from a specific company or actual past interview.
            - Be honest and specific, grounded only in the data given above. Do not invent skills, scores, or facts.
            - Return ONLY valid JSON: no markdown fences, no explanations, no trailing commas, no comments.
            """;

    public ReportContent generate(BigDecimal matchScore, List<String> matchedSkills, List<String> missingSkills,
                                  String candidateEducation, String requiredEducation,
                                  BigDecimal candidateExperience, BigDecimal requiredExperience) {

        boolean highMatch = isHighMatch(matchScore);
        List<String> prioritySkills = limitPrioritySkills(missingSkills);
        String template = highMatch ? HIGH_MATCH_PROMPT : LOW_MATCH_PROMPT;

        String prompt = template.formatted(
                matchScore.toPlainString(),
                joinOrNone(matchedSkills),
                MAX_PRIORITY_SKILLS,
                joinOrNone(prioritySkills),
                orNotSpecified(candidateEducation), orNotSpecified(requiredEducation),
                candidateExperience.toPlainString(), requiredExperience.toPlainString(),
                MAX_TIMELINE_WEEKS
        );

        return callAndParseWithRetry(prompt);
    }

    /**
     * Calls the AI model and parses the JSON response, retrying the call
     * once if parsing fails (e.g. truncated or malformed JSON).
     */
    private ReportContent callAndParseWithRetry(String prompt) {
        String firstResponse = callModel(prompt);
        log.info("AI report content response (attempt 1): {}", firstResponse);

        try {
            return parseResponse(firstResponse);
        } catch (Exception firstFailure) {
            log.warn("Failed to parse AI report content JSON on first attempt, retrying once: {}",
                    firstFailure.getMessage());

            String secondResponse = callModel(prompt);
            log.info("AI report content response (attempt 2): {}", secondResponse);

            try {
                return parseResponse(secondResponse);
            } catch (Exception secondFailure) {
                throw new IllegalStateException(
                        "Failed to parse AI report content as JSON after retry: " + secondFailure.getMessage()
                                + " | Raw AI response: " + secondResponse, secondFailure);
            }
        }
    }

    private String callModel(String prompt) {
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("AI report content generation returned an empty response");
        }
        return response;
    }

    private ReportContent parseResponse(String response) throws Exception {
        String json = extractJson(stripMarkdownFences(response));
        return objectMapper.readValue(json, ReportContent.class);
    }

    private List<String> limitPrioritySkills(List<String> missingSkills) {
        if (missingSkills == null || missingSkills.isEmpty()) {
            return List.of();
        }
        return missingSkills.size() > MAX_PRIORITY_SKILLS
                ? missingSkills.subList(0, MAX_PRIORITY_SKILLS)
                : missingSkills;
    }

    private String joinOrNone(List<String> items) {
        return String.join(", ", items == null || items.isEmpty() ? List.of("none") : items);
    }

    private String orNotSpecified(String value) {
        return (value == null || value.isBlank()) ? "not specified" : value;
    }

    private String stripMarkdownFences(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(json)?", "").trim();
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
            }
        }
        return trimmed;
    }

    /**
     * Extracts the JSON object from a response that may contain extra
     * leading/trailing text around it (e.g. "Here is the JSON: { ... }").
     * Falls back to the original (trimmed) text if no braces are found,
     * so the caller still gets a meaningful parse error.
     */
    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }
}