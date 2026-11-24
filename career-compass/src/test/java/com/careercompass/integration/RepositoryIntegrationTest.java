package com.careercompass.integration;

import com.careercompass.entity.JobDescription;
import com.careercompass.entity.Resume;
import com.careercompass.repository.EmbeddingRepository;
import com.careercompass.repository.JobDescriptionRepository;
import com.careercompass.repository.ResumeRepository;
import com.careercompass.service.EmbeddingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the real database layer against an actual, ephemeral
 * PostgreSQL + pgvector instance (via Testcontainers) — not mocks. Flyway
 * runs the real migrations (V1-V6) against it automatically, exactly as
 * it would in production, so this catches schema/mapping issues unit
 * tests can't.
 *
 * No AI API calls are made here — the AI chat provider's API key is
 * overridden with a dummy value purely so the Spring context can start
 * (Spring AI's autoconfiguration requires a non-blank key to construct
 * the bean, even though nothing in this test actually calls it). Embedding
 * generation DOES run for real, using the free local model — no API key
 * or cost involved there.
 *
 * Note: the local embedding model downloads itself (~90MB) on first use,
 * same as in normal app startup — the first run of this test may be slower
 * than subsequent ones.
 */
@Testcontainers
@SpringBootTest
class RepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Dummy key so the AI bean can construct at context startup — not used in this test.
        registry.add("spring.ai.openai.api-key", () -> "dummy-test-key-not-real");
    }

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private JobDescriptionRepository jobDescriptionRepository;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private EmbeddingRepository embeddingRepository;

    // Matches the user seeded by V2__seed_test_user.sql, needed to satisfy
    // the foreign key constraint on resumes.user_id / job_descriptions.user_id.
    private static final UUID SEEDED_TEST_USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Test
    void savesAndRetrievesResumeWithAllStructuredFields() {
        Resume resume = new Resume();
        resume.setUserId(SEEDED_TEST_USER_ID);
        resume.setOriginalFilename("test.pdf");
        resume.setCandidateName("Test Candidate");
        resume.setRawText("Some raw resume text");
        resume.setExtractedSkills(List.of("Java", "Spring Boot"));
        resume.setSoftSkills(List.of("Communication"));
        resume.setCertifications(List.of("AWS Certified"));
        resume.setEducationLevel("B.Tech");
        resume.setYearsExperience(new BigDecimal("2.5"));

        Resume saved = resumeRepository.save(resume);
        Resume found = resumeRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getCandidateName()).isEqualTo("Test Candidate");
        assertThat(found.getExtractedSkills()).containsExactly("Java", "Spring Boot");
        assertThat(found.getYearsExperience()).isEqualByComparingTo("2.5");
    }

    @Test
    void savesAndRetrievesJobDescriptionWithAllStructuredFields() {
        JobDescription jd = new JobDescription();
        jd.setUserId(SEEDED_TEST_USER_ID);
        jd.setTitle("Backend Developer");
        jd.setCompanyName("Test Corp");
        jd.setRawText("Some raw JD text");
        jd.setRequiredSkills(List.of("Java", "PostgreSQL"));
        jd.setMinEducationLevel("B.Tech");
        jd.setMinYearsExperience(new BigDecimal("2"));

        JobDescription saved = jobDescriptionRepository.save(jd);
        JobDescription found = jobDescriptionRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getCompanyName()).isEqualTo("Test Corp");
        assertThat(found.getRequiredSkills()).containsExactly("Java", "PostgreSQL");
    }

    @Test
    void identicalTextProducesHighCosineSimilarity() {
        Resume resume = new Resume();
        resume.setUserId(SEEDED_TEST_USER_ID);
        resume.setRawText("placeholder");
        resume.setExtractedSkills(List.of("Java"));
        Resume savedResume = resumeRepository.save(resume);

        JobDescription jd = new JobDescription();
        jd.setUserId(SEEDED_TEST_USER_ID);
        jd.setRawText("placeholder");
        jd.setRequiredSkills(List.of("Java"));
        JobDescription savedJd = jobDescriptionRepository.save(jd);

        String sameText = "Skills: Java, Spring Boot. Education: B.Tech. Experience: 3 years.";
        float[] vec1 = embeddingService.embed(sameText);
        float[] vec2 = embeddingService.embed(sameText);

        embeddingRepository.updateResumeEmbedding(savedResume.getId(), embeddingService.toPgVectorLiteral(vec1));
        embeddingRepository.updateJdEmbedding(savedJd.getId(), embeddingService.toPgVectorLiteral(vec2));

        Double similarity = embeddingRepository.calculateCosineSimilarity(savedResume.getId(), savedJd.getId());

        // Identical text embedded twice should be near-perfect similarity (allowing tiny float variance).
        assertThat(similarity).isGreaterThan(0.99);
    }

    @Test
    void unrelatedTextProducesLowerCosineSimilarityThanIdenticalText() {
        Resume resume = new Resume();
        resume.setUserId(SEEDED_TEST_USER_ID);
        resume.setRawText("placeholder");
        Resume savedResume = resumeRepository.save(resume);

        JobDescription jd = new JobDescription();
        jd.setUserId(SEEDED_TEST_USER_ID);
        jd.setRawText("placeholder");
        JobDescription savedJd = jobDescriptionRepository.save(jd);

        float[] resumeVec = embeddingService.embed("Skills: Java, Spring Boot, backend development.");
        float[] jdVec = embeddingService.embed("A recipe for chocolate cake with flour, sugar, and eggs.");

        embeddingRepository.updateResumeEmbedding(savedResume.getId(), embeddingService.toPgVectorLiteral(resumeVec));
        embeddingRepository.updateJdEmbedding(savedJd.getId(), embeddingService.toPgVectorLiteral(jdVec));

        Double similarity = embeddingRepository.calculateCosineSimilarity(savedResume.getId(), savedJd.getId());

        assertThat(similarity).isLessThan(0.5);
    }
}
