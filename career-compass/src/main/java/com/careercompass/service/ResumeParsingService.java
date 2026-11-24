package com.careercompass.service;

import com.careercompass.dto.ParsedResumeData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Extracts structured data from resume text in a single AI call: skills,
 * education level, years of experience, and projects.
 *
 * Personal details (name/email/phone) are deliberately NOT extracted here —
 * they carry no eligibility-scoring value and would only add unnecessary
 * PII handling.
 *
 * The prompt asks for raw JSON matching ParsedResumeData's shape, which is
 * then parsed directly with Jackson. This is more reliable across different
 * model providers than relying on a provider-specific structured-output
 * feature, and keeps the same predictable pattern used elsewhere in the app.
 */
@Service
public class ResumeParsingService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public ResumeParsingService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    private static final String PROMPT_TEMPLATE = """
            Extract structured information from the resume text below.

            Return ONLY valid JSON, no markdown fences, no commentary, matching exactly this shape:
            {
              "candidateName": "the candidate's full name if clearly present, otherwise null — never guess",
              "skills": ["Java", "Spring Boot", "..."],
              "softSkills": ["communication", "teamwork", "..."],
              "certifications": ["certification name", "..."],
              "educationLevel": "highest degree only, e.g. B.Tech, M.Sc, Diploma, or null if not mentioned",
              "yearsOfExperience": 0.0,
              "projects": [
                {
                  "name": "project name",
                  "description": "one or two sentence summary",
                  "technologies": ["tech1", "tech2"]
                }
              ]
            }

            Rules:
            - Do not invent information that is not present in the text. If softSkills or certifications
              are not explicitly mentioned or clearly implied, return an empty array — do not guess.
            - If the candidate is a fresher with no work experience, set yearsOfExperience to 0.
            - Include ALL projects mentioned, even small ones — for freshers, projects are often the
              strongest signal of ability, so do not omit any.
            - candidateName should only be set if a real name clearly appears in the text (e.g. as a header
              or "Name:" field). If uncertain, set it to null rather than guessing.

            RESUME TEXT:
            %s
            """;

    public ParsedResumeData parse(String resumeText) {
        String prompt = PROMPT_TEMPLATE.formatted(resumeText);

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("AI extraction returned an empty response");
        }

        String cleaned = stripMarkdownFences(response);

        try {
            return objectMapper.readValue(cleaned, ParsedResumeData.class);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to parse AI extraction response as JSON: " + e.getMessage(), e);
        }
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
}
