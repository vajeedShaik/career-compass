package com.careercompass.service;

import com.careercompass.dto.ReportContent;
import com.careercompass.dto.RoadmapResponse;
import com.careercompass.entity.*;
import com.careercompass.repository.*;
import com.careercompass.security.CurrentUserProvider;
import com.careercompass.service.pdf.PdfGenerationService;
import com.careercompass.util.SkillNameMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Generates a personalized preparation roadmap for the skill gaps found in
 * a Match (used for the /api/roadmaps response and as chat context), and
 * additionally builds and emails the full multi-page career assessment
 * report as a PDF. Email/PDF failures are caught and logged rather than
 * failing the whole request — the roadmap itself is still valid and useful
 * even if the email couldn't be sent (e.g. SMTP not configured yet).
 */
@Service
public class RoadmapService {

    private static final Logger log = LoggerFactory.getLogger(RoadmapService.class);

    private final MatchRepository matchRepository;
    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final UserRepository userRepository;
    private final LearningResourceRepository learningResourceRepository;
    private final RoadmapRepository roadmapRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ReportContentService reportContentService;
    private final PdfGenerationService pdfGenerationService;
    private final EmailService emailService;
    private final ChatClient chatClient;

    public RoadmapService(MatchRepository matchRepository,
                           ResumeRepository resumeRepository,
                           JobDescriptionRepository jobDescriptionRepository,
                           UserRepository userRepository,
                           LearningResourceRepository learningResourceRepository,
                           RoadmapRepository roadmapRepository,
                           CurrentUserProvider currentUserProvider,
                           ReportContentService reportContentService,
                           PdfGenerationService pdfGenerationService,
                           EmailService emailService,
                           ChatClient.Builder chatClientBuilder) {
        this.matchRepository = matchRepository;
        this.resumeRepository = resumeRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.userRepository = userRepository;
        this.learningResourceRepository = learningResourceRepository;
        this.roadmapRepository = roadmapRepository;
        this.currentUserProvider = currentUserProvider;
        this.reportContentService = reportContentService;
        this.pdfGenerationService = pdfGenerationService;
        this.emailService = emailService;
        this.chatClient = chatClientBuilder.build();
    }

    private static final String PROMPT_TEMPLATE = """
            You are creating a short, practical preparation roadmap for a candidate based on their skill gaps
            for a specific job.

            Missing skills: %s

            Below is a list of REAL, curated learning resources retrieved for these skills. You MUST only
            recommend resources from this list — do not invent, guess, or reference any course, book, or URL
            that is not explicitly listed below. If a missing skill has no resources listed, say so honestly
            instead of making one up.

            RETRIEVED RESOURCES:
            %s

            Write the roadmap as clear markdown with:
            - A short intro (1-2 sentences)
            - One section per missing skill, in a sensible learning order (foundational skills first)
            - Under each skill: 1-2 sentences of practical guidance, followed by the retrieved resource(s) for
              that skill as a markdown link
            - Keep the whole thing concise — this should be a quick, actionable plan, not an essay
            """;

    @Transactional
    public RoadmapResponse generate(UUID matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        List<String> missingSkills = match.getMissingSkills() != null ? match.getMissingSkills() : List.of();
        List<LearningResource> allResources = learningResourceRepository.findAll();

        Roadmap saved;
        List<RoadmapResponse.ResourceUsed> resourcesUsed = new ArrayList<>();

        if (missingSkills.isEmpty()) {
            saved = save(matchId, "No skill gaps were found for this match — no roadmap needed.");
        } else {
            StringBuilder retrievedBlock = new StringBuilder();
            for (String skill : missingSkills) {
                List<LearningResource> matchedResources = allResources.stream()
                        .filter(r -> SkillNameMatcher.matches(skill, r.getSkill()))
                        .toList();

                if (matchedResources.isEmpty()) {
                    retrievedBlock.append("- ").append(skill).append(": (no curated resource available)\n");
                } else {
                    for (LearningResource r : matchedResources) {
                        retrievedBlock.append("- ").append(skill).append(": ")
                                .append(r.getTitle()).append(" (").append(r.getUrl()).append(")\n");
                        resourcesUsed.add(new RoadmapResponse.ResourceUsed(skill, r.getTitle(), r.getUrl()));
                    }
                }
            }

            String prompt = PROMPT_TEMPLATE.formatted(String.join(", ", missingSkills), retrievedBlock.toString());
            String roadmapContent = chatClient.prompt().user(prompt).call().content();

            if (roadmapContent == null || roadmapContent.isBlank()) {
                throw new IllegalStateException("AI roadmap generation returned an empty response");
            }
            saved = save(matchId, roadmapContent);
        }

        boolean emailSent = tryGenerateAndEmailReport(match, allResources);

        return new RoadmapResponse(
                saved.getId(), saved.getMatchId(), saved.getContent(),
                resourcesUsed, emailSent, saved.getCreatedAt()
        );
    }

    /**
     * Builds the full PDF report and emails it to the authenticated user.
     * Returns false (without throwing) on any failure, so a broken email
     * config never breaks the core roadmap feature.
     */
    private boolean tryGenerateAndEmailReport(Match match, List<LearningResource> allResources) {
        try {
            Resume resume = resumeRepository.findById(match.getResumeId())
                    .orElseThrow(() -> new IllegalStateException("Resume not found for match"));
            JobDescription jd = jobDescriptionRepository.findById(match.getJdId())
                    .orElseThrow(() -> new IllegalStateException("JD not found for match"));
            User user = userRepository.findById(currentUserProvider.getUserId())
                    .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

            boolean highMatch = reportContentService.isHighMatch(match.getMatchScore());
            String category = reportContentService.categoryFor(match.getMatchScore());

            ReportContent content = reportContentService.generate(
                    match.getMatchScore(),
                    match.getMatchedSkills() != null ? match.getMatchedSkills() : List.of(),
                    match.getMissingSkills() != null ? match.getMissingSkills() : List.of(),
                    resume.getEducationLevel(), jd.getMinEducationLevel(),
                    resume.getYearsExperience(), jd.getMinYearsExperience()
            );

            byte[] pdf = pdfGenerationService.generate(
                    resume, jd, match, content, allResources, category, highMatch, user.getEmail());

            emailService.sendReport(user.getEmail(), pdf);
            return true;
        } catch (Exception e) {
            log.warn("Report generation/email failed for match {}: {}", match.getId(), e.getMessage(), e);
            return false;
        }
    }

    private Roadmap save(UUID matchId, String content) {
        Roadmap roadmap = new Roadmap();
        roadmap.setMatchId(matchId);
        roadmap.setContent(content);
        return roadmapRepository.save(roadmap);
    }
}
