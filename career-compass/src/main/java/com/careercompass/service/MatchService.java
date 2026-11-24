package com.careercompass.service;

import com.careercompass.dto.MatchResponse;
import com.careercompass.entity.JobDescription;
import com.careercompass.entity.Match;
import com.careercompass.entity.Resume;
import com.careercompass.repository.EmbeddingRepository;
import com.careercompass.repository.JobDescriptionRepository;
import com.careercompass.repository.MatchRepository;
import com.careercompass.repository.ResumeRepository;
import com.careercompass.util.EducationLevelUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Computes a match score between a resume and a JD using a hybrid approach:
 * embedding-based semantic similarity, adjusted by hard checks against
 * structured fields (education level, years of experience).
 *
 * This directly addresses a known weakness of pure embedding matching:
 * semantic similarity can score a candidate highly on topic overlap alone,
 * even when they clearly fail a hard requirement like minimum experience.
 * The structured-field penalties below catch exactly that case.
 */
@Service
public class MatchService {

    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final MatchRepository matchRepository;
    private final EmbeddingService embeddingService;
    private final EmbeddingRepository embeddingRepository;

    public MatchService(ResumeRepository resumeRepository,
                         JobDescriptionRepository jobDescriptionRepository,
                         MatchRepository matchRepository,
                         EmbeddingService embeddingService,
                         EmbeddingRepository embeddingRepository) {
        this.resumeRepository = resumeRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.matchRepository = matchRepository;
        this.embeddingService = embeddingService;
        this.embeddingRepository = embeddingRepository;
    }

    @Transactional
    public MatchResponse computeMatch(UUID resumeId, UUID jdId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + resumeId));
        JobDescription jd = jobDescriptionRepository.findById(jdId)
                .orElseThrow(() -> new IllegalArgumentException("Job description not found: " + jdId));

        // 1. Embed and store both sides (using a compact profile summary,
        // not the full raw text — cleaner signal for similarity than raw
        // noisy resume/JD text would give).
        String resumeProfile = buildResumeProfile(resume);
        String jdProfile = buildJdProfile(jd);

        float[] resumeEmbedding = embeddingService.embed(resumeProfile);
        float[] jdEmbedding = embeddingService.embed(jdProfile);

        embeddingRepository.updateResumeEmbedding(resumeId, embeddingService.toPgVectorLiteral(resumeEmbedding));
        embeddingRepository.updateJdEmbedding(jdId, embeddingService.toPgVectorLiteral(jdEmbedding));

        // 2. Base semantic similarity score (0-100)
        Double cosineSimilarity = embeddingRepository.calculateCosineSimilarity(resumeId, jdId);
        double baseScore = Math.max(0, Math.min(1, cosineSimilarity)) * 100;

        // 3. Hard-constraint adjustments against structured fields
        StringBuilder notes = new StringBuilder();
        double penalty = 0;

        BigDecimal candidateExp = resume.getYearsExperience() != null ? resume.getYearsExperience() : BigDecimal.ZERO;
        BigDecimal requiredExp = jd.getMinYearsExperience() != null ? jd.getMinYearsExperience() : BigDecimal.ZERO;
        if (candidateExp.compareTo(requiredExp) < 0) {
            double gap = requiredExp.subtract(candidateExp).doubleValue();
            double expPenalty = Math.min(20, gap * 5);
            penalty += expPenalty;
            notes.append(String.format(
                    "Experience gap: has %.1f yrs, JD requires %.1f yrs (-%.1f pts). ",
                    candidateExp.doubleValue(), requiredExp.doubleValue(), expPenalty));
        }

        int candidateEduRank = EducationLevelUtil.rank(resume.getEducationLevel());
        int requiredEduRank = EducationLevelUtil.rank(jd.getMinEducationLevel());
        if (candidateEduRank < requiredEduRank) {
            penalty += 10;
            notes.append(String.format(
                    "Education below requirement: has '%s', JD requires '%s' (-10 pts). ",
                    resume.getEducationLevel(), jd.getMinEducationLevel()));
        }

        double finalScore = Math.max(0, baseScore - penalty);
        BigDecimal matchScore = BigDecimal.valueOf(finalScore).setScale(2, RoundingMode.HALF_UP);

        if (notes.isEmpty()) {
            notes.append("No hard-constraint penalties applied.");
        }

        // 4. Skill overlap (simple case-insensitive set comparison)
        List<String> resumeSkills = resume.getExtractedSkills() != null ? resume.getExtractedSkills() : List.of();
        List<String> requiredSkills = jd.getRequiredSkills() != null ? jd.getRequiredSkills() : List.of();

        List<String> resumeSkillsLower = resumeSkills.stream().map(String::toLowerCase).collect(Collectors.toList());

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        for (String required : requiredSkills) {
            if (resumeSkillsLower.contains(required.toLowerCase())) {
                matchedSkills.add(required);
            } else {
                missingSkills.add(required);
            }
        }

        // 5. Persist
        Match match = new Match();
        match.setResumeId(resumeId);
        match.setJdId(jdId);
        match.setMatchScore(matchScore);
        match.setMatchedSkills(matchedSkills);
        match.setMissingSkills(missingSkills);

        Match saved = matchRepository.save(match);

        return new MatchResponse(
                saved.getId(),
                saved.getResumeId(),
                saved.getJdId(),
                saved.getMatchScore(),
                saved.getMatchedSkills(),
                saved.getMissingSkills(),
                notes.toString().trim(),
                saved.getCreatedAt()
        );
    }

    private String buildResumeProfile(Resume resume) {
        return """
                Skills: %s.
                Education: %s.
                Experience: %s years.
                """.formatted(
                String.join(", ", resume.getExtractedSkills() != null ? resume.getExtractedSkills() : List.of()),
                resume.getEducationLevel() != null ? resume.getEducationLevel() : "not specified",
                resume.getYearsExperience() != null ? resume.getYearsExperience().toString() : "0"
        );
    }

    private String buildJdProfile(JobDescription jd) {
        return """
                Required skills: %s.
                Minimum education: %s.
                Minimum experience: %s years.
                """.formatted(
                String.join(", ", jd.getRequiredSkills() != null ? jd.getRequiredSkills() : List.of()),
                jd.getMinEducationLevel() != null ? jd.getMinEducationLevel() : "not specified",
                jd.getMinYearsExperience() != null ? jd.getMinYearsExperience().toString() : "0"
        );
    }
}
