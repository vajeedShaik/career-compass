package com.careercompass.service;

import com.careercompass.dto.ParsedJdData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Extracts structured requirements from job description text: required
 * skills, minimum education level, and minimum years of experience.
 * Mirrors ResumeParsingService's approach exactly — one AI call, a prompt
 * demanding raw JSON, parsed with Jackson — so both sides of the eventual
 * match (Module 3) are produced the same, predictable way.
 */
@Service
public class JdParsingService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public JdParsingService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    private static final String PROMPT_TEMPLATE = """
            Extract structured requirements from the job description text below.

            Return ONLY valid JSON, no markdown fences, no commentary, matching exactly this shape:
            {
              "companyName": "the hiring company's name if clearly stated, otherwise null — never guess",
              "requiredSkills": ["Java", "Spring Boot", "..."],
              "minEducationLevel": "e.g. B.Tech, M.Sc, Diploma, or null if not specified",
              "minYearsExperience": 0.0
            }

            Rules:
            - Do not invent requirements that are not stated or clearly implied in the text.
            - If the JD does not mention a minimum experience requirement, set minYearsExperience to 0.
            - List every distinct required or strongly preferred technical skill, normalized casing
              (e.g. "javascript" -> "JavaScript").

            JOB DESCRIPTION TEXT:
            %s
            """;

    public ParsedJdData parse(String jdText) {
        String prompt = PROMPT_TEMPLATE.formatted(jdText);

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("AI extraction returned an empty response");
        }

        String cleaned = stripMarkdownFences(response);

        try {
            return objectMapper.readValue(cleaned, ParsedJdData.class);
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
