package com.careercompass.service;

import com.careercompass.dto.MatchResponse;
import com.careercompass.entity.JobDescription;
import com.careercompass.entity.Match;
import com.careercompass.entity.Resume;
import com.careercompass.repository.EmbeddingRepository;
import com.careercompass.repository.JobDescriptionRepository;
import com.careercompass.repository.MatchRepository;
import com.careercompass.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the hybrid scoring logic in MatchService — the core, riskiest piece
 * of business logic in the project — with the database and embedding model
 * mocked out. This verifies the exact scenario found in manual testing: a
 * high embedding similarity score correctly reduced by a real experience
 * gap, which pure semantic similarity alone would have missed.
 */
class MatchServiceTest {

    private ResumeRepository resumeRepository;
    private JobDescriptionRepository jobDescriptionRepository;
    private MatchRepository matchRepository;
    private EmbeddingService embeddingService;
    private EmbeddingRepository embeddingRepository;
    private MatchService matchService;

    private final UUID resumeId = UUID.randomUUID();
    private final UUID jdId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        resumeRepository = mock(ResumeRepository.class);
        jobDescriptionRepository = mock(JobDescriptionRepository.class);
        matchRepository = mock(MatchRepository.class);
        embeddingService = mock(EmbeddingService.class);
        embeddingRepository = mock(EmbeddingRepository.class);

        matchService = new MatchService(
                resumeRepository, jobDescriptionRepository, matchRepository,
                embeddingService, embeddingRepository
        );

        when(embeddingService.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f, 0.3f});
        when(embeddingService.toPgVectorLiteral(any())).thenReturn("[0.1,0.2,0.3]");
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> {
            Match m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });
    }

    private Resume resumeWith(BigDecimal yearsExperience, String educationLevel, List<String> skills) {
        Resume r = new Resume();
        r.setId(resumeId);
        r.setYearsExperience(yearsExperience);
        r.setEducationLevel(educationLevel);
        r.setExtractedSkills(skills);
        return r;
    }

    private JobDescription jdWith(BigDecimal minYearsExperience, String minEducationLevel, List<String> requiredSkills) {
        JobDescription j = new JobDescription();
        j.setId(jdId);
        j.setMinYearsExperience(minYearsExperience);
        j.setMinEducationLevel(minEducationLevel);
        j.setRequiredSkills(requiredSkills);
        return j;
    }

    @Test
    void appliesNoPenaltyWhenExperienceAndEducationMeetRequirements() {
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(
                resumeWith(new BigDecimal("3"), "B.Tech", List.of("Java", "Spring Boot"))));
        when(jobDescriptionRepository.findById(jdId)).thenReturn(Optional.of(
                jdWith(new BigDecimal("2"), "B.Tech", List.of("Java", "Spring Boot"))));
        when(embeddingRepository.calculateCosineSimilarity(resumeId, jdId)).thenReturn(0.80);

        MatchResponse response = matchService.computeMatch(resumeId, jdId);

        // 0.80 similarity -> 80 base score, no penalties applied
        assertThat(response.matchScore()).isEqualByComparingTo("80.00");
        assertThat(response.scoringNotes()).contains("No hard-constraint penalties");
    }

    @Test
    void appliesExperiencePenaltyWhenCandidateFallsShort() {
        // Mirrors the real manual test result: 0 years vs 2 years required
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(
                resumeWith(BigDecimal.ZERO, "B.Tech", List.of("Java"))));
        when(jobDescriptionRepository.findById(jdId)).thenReturn(Optional.of(
                jdWith(new BigDecimal("2"), "B.Tech", List.of("Java", "Spring Boot"))));
        when(embeddingRepository.calculateCosineSimilarity(resumeId, jdId)).thenReturn(0.45);

        MatchResponse response = matchService.computeMatch(resumeId, jdId);

        // 45 base - min(20, gap(2)*5=10) = 45 - 10 = 35
        assertThat(response.matchScore()).isEqualByComparingTo("35.00");
        assertThat(response.scoringNotes()).contains("Experience gap");
    }

    @Test
    void capsExperiencePenaltyAtTwentyPointsForLargeGaps() {
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(
                resumeWith(BigDecimal.ZERO, "B.Tech", List.of("Java"))));
        when(jobDescriptionRepository.findById(jdId)).thenReturn(Optional.of(
                jdWith(new BigDecimal("10"), "B.Tech", List.of("Java"))));
        when(embeddingRepository.calculateCosineSimilarity(resumeId, jdId)).thenReturn(0.90);

        MatchResponse response = matchService.computeMatch(resumeId, jdId);

        // gap(10)*5=50, but capped at 20 -> 90 - 20 = 70
        assertThat(response.matchScore()).isEqualByComparingTo("70.00");
    }

    @Test
    void appliesEducationPenaltyWhenBelowRequiredLevel() {
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(
                resumeWith(new BigDecimal("5"), "Diploma", List.of("Java"))));
        when(jobDescriptionRepository.findById(jdId)).thenReturn(Optional.of(
                jdWith(BigDecimal.ZERO, "M.Tech", List.of("Java"))));
        when(embeddingRepository.calculateCosineSimilarity(resumeId, jdId)).thenReturn(0.70);

        MatchResponse response = matchService.computeMatch(resumeId, jdId);

        // 70 base - 10 (education penalty) = 60
        assertThat(response.matchScore()).isEqualByComparingTo("60.00");
        assertThat(response.scoringNotes()).contains("Education below requirement");
    }

    @Test
    void scoreNeverGoesBelowZeroEvenWithLargeCombinedPenalties() {
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(
                resumeWith(BigDecimal.ZERO, "Diploma", List.of())));
        when(jobDescriptionRepository.findById(jdId)).thenReturn(Optional.of(
                jdWith(new BigDecimal("10"), "PhD", List.of("Java"))));
        when(embeddingRepository.calculateCosineSimilarity(resumeId, jdId)).thenReturn(0.10);

        MatchResponse response = matchService.computeMatch(resumeId, jdId);

        assertThat(response.matchScore()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    void correctlySeparatesMatchedAndMissingSkillsCaseInsensitively() {
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(
                resumeWith(new BigDecimal("3"), "B.Tech", List.of("java", "SQL"))));
        when(jobDescriptionRepository.findById(jdId)).thenReturn(Optional.of(
                jdWith(BigDecimal.ZERO, "B.Tech", List.of("Java", "Spring Boot", "Docker"))));
        when(embeddingRepository.calculateCosineSimilarity(resumeId, jdId)).thenReturn(0.60);

        MatchResponse response = matchService.computeMatch(resumeId, jdId);

        assertThat(response.matchedSkills()).containsExactly("Java");
        assertThat(response.missingSkills()).containsExactlyInAnyOrder("Spring Boot", "Docker");
    }
}
