package com.careercompass.service;

import com.careercompass.dto.ParsedResumeData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests the parsing/mapping logic in ResumeParsingService with the actual
 * AI call mocked out — no API key, no network call, no cost. This verifies
 * the JSON contract is handled correctly, including markdown-fence
 * stripping (a real thing models sometimes add despite being told not to)
 * and malformed-response error handling.
 */
class ResumeParsingServiceTest {

    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec callResponseSpec;
    private ResumeParsingService service;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(chatClient);

        service = new ResumeParsingService(builder, new ObjectMapper());
    }

    @Test
    void parsesWellFormedJsonResponseCorrectly() {
        String json = """
                {
                  "candidateName": "Jane Doe",
                  "skills": ["Java", "Spring Boot"],
                  "softSkills": ["Communication"],
                  "certifications": ["AWS Certified Developer"],
                  "educationLevel": "B.Tech",
                  "yearsOfExperience": 2.5,
                  "projects": [
                    {"name": "Test Project", "description": "A test", "technologies": ["Java"]}
                  ]
                }
                """;
        when(callResponseSpec.content()).thenReturn(json);

        ParsedResumeData result = service.parse("some resume text");

        assertThat(result.candidateName()).isEqualTo("Jane Doe");
        assertThat(result.skills()).containsExactly("Java", "Spring Boot");
        assertThat(result.softSkills()).containsExactly("Communication");
        assertThat(result.certifications()).containsExactly("AWS Certified Developer");
        assertThat(result.educationLevel()).isEqualTo("B.Tech");
        assertThat(result.yearsOfExperience()).isEqualByComparingTo("2.5");
        assertThat(result.projects()).hasSize(1);
        assertThat(result.projects().get(0).name()).isEqualTo("Test Project");
    }

    @Test
    void stripsMarkdownFencesIfModelAddsThemDespiteInstructions() {
        String jsonWithFences = """
                ```json
                {"candidateName": null, "skills": ["Python"], "softSkills": [], "certifications": [],
                 "educationLevel": null, "yearsOfExperience": 0, "projects": []}
                ```
                """;
        when(callResponseSpec.content()).thenReturn(jsonWithFences);

        ParsedResumeData result = service.parse("resume text");

        assertThat(result.skills()).containsExactly("Python");
        assertThat(result.candidateName()).isNull();
    }

    @Test
    void throwsIllegalStateExceptionOnEmptyResponse() {
        when(callResponseSpec.content()).thenReturn("");

        assertThatThrownBy(() -> service.parse("resume text"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("empty response");
    }

    @Test
    void throwsIllegalStateExceptionOnMalformedJson() {
        when(callResponseSpec.content()).thenReturn("this is not valid json at all");

        assertThatThrownBy(() -> service.parse("resume text"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to parse");
    }
}
