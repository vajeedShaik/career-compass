package com.careercompass.service.pdf;

import com.careercompass.dto.ReportContent;
import com.careercompass.entity.JobDescription;
import com.careercompass.entity.LearningResource;
import com.careercompass.entity.Match;
import com.careercompass.entity.Resume;
import com.careercompass.util.SkillNameMatcher;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the full multi-page career assessment report as a byte array,
 * ready to email as an attachment. Drawing logic lives in PdfWriter /
 * PdfDiagramRenderer; this class is purely page-by-page orchestration.
 */
@Service
public class PdfGenerationService {

    private static final Color NAVY = new Color(27, 42, 74);
    private static final Color ACCENT = new Color(46, 111, 149);
    private static final Color GREY = new Color(85, 85, 85);
    private static final Color TEAL = new Color(13, 148, 136);
    private static final Color CORAL = new Color(220, 38, 38);
    private static final Color BLUE = new Color(37, 99, 235);

    // Cover page styling constants
    private static final Color CARD_FILL = new Color(247, 249, 251);
    private static final Color CARD_BORDER = new Color(215, 220, 228);
    private static final Color BANNER_SUBTITLE = new Color(200, 215, 235);
    private static final Color FOOTER_LINE = new Color(210, 210, 210);

    private final PdfDiagramRenderer diagramRenderer;
    private final String developerName;
    private final String githubUrl;

    public PdfGenerationService(PdfDiagramRenderer diagramRenderer,
                                @Value("${app.report.developer-name}") String developerName,
                                @Value("${app.report.github-url}") String githubUrl) {
        this.diagramRenderer = diagramRenderer;
        this.developerName = developerName;
        this.githubUrl = githubUrl;
    }

    public byte[] generate(Resume resume, JobDescription jd, Match match, ReportContent content,
                           List<LearningResource> allResources, String category, boolean highMatch,
                           String candidateEmail) throws IOException {

        try (PDDocument document = new PDDocument()) {
            try (PdfWriter w = new PdfWriter(document)) {

                drawCoverPage(w, resume, jd, match, category);
                w.forceNewPage();
                drawExecutiveSummary(w, resume, jd, match, content, category, candidateEmail);
                w.forceNewPage();
                drawSkillAnalysis(w, match, content);

                w.forceNewPage();
                if (highMatch) {
                    drawInterviewPrep(w, content);
                } else {
                    drawRoadmapPage(w, content);
                }

                w.forceNewPage();
                drawTimeline(w, content, highMatch);

                if (!match.getMissingSkills().isEmpty()) {
                    w.forceNewPage();
                    drawResourcesPage(w, match.getMissingSkills(), allResources, content);
                }

                w.forceNewPage();
                drawFinalPage(w, content);
            }

            addPageNumbersAndFooter(document);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    // ---------------- Page 1: Cover (redesigned) ----------------

    private void drawCoverPage(PdfWriter w, Resume resume, JobDescription jd, Match match, String category) throws IOException {
        float pageW = PdfWriter.PAGE_WIDTH;
        float pageH = PdfWriter.PAGE_HEIGHT;
        float margin = PdfWriter.MARGIN;
        float contentW = PdfWriter.CONTENT_WIDTH;

        // ---- Header banner ----
        float bannerHeight = 150f;
        float bannerTop = pageH;
        w.filledRect(0, bannerTop - bannerHeight, pageW, bannerHeight, NAVY);

        w.centeredTextAt("CAREER COMPASS", PdfWriter.FONT_BOLD, 30f, Color.WHITE, pageW / 2f, bannerTop - 65f);
        w.centeredTextAt("AI Career Assessment Report", PdfWriter.FONT_REGULAR, 13f, BANNER_SUBTITLE, pageW / 2f, bannerTop - 90f);
        w.drawLine(pageW / 2f - 70f, bannerTop - 105f, pageW / 2f + 70f, bannerTop - 105f, new Color(120, 150, 180), 1f);

        // ---- Candidate / Role cards ----
        float sectionTop = bannerTop - bannerHeight - 45f;
        float cardGap = 20f;
        float cardWidth = (contentW - cardGap) / 2f;
        float cardHeight = 68f;

        String candidateName = resume.getCandidateName() != null ? resume.getCandidateName() : "Candidate";
        String role = jd.getTitle() != null ? jd.getTitle() : "Target Role";

        drawInfoCard(w, margin, sectionTop, cardWidth, cardHeight, "CANDIDATE", candidateName);
        drawInfoCard(w, margin + cardWidth + cardGap, sectionTop, cardWidth, cardHeight, "TARGET ROLE", role);

        float cardsBottom = sectionTop - cardHeight;

        // ---- Match score card ----
        float scoreCardTop = cardsBottom - 40f;
        float scoreCardHeight = 165f;
        float scoreCardY = scoreCardTop - scoreCardHeight;

        w.roundedRect(margin, scoreCardY, contentW, scoreCardHeight, 10f, CARD_FILL, CARD_BORDER, 1f);

        w.centeredTextAt("MATCH SCORE", PdfWriter.FONT_BOLD, 11f, GREY, pageW / 2f, scoreCardTop - 28f);

        String scoreText = match.getMatchScore().setScale(0, java.math.RoundingMode.HALF_UP) + "%";
        w.centeredTextAt(scoreText, PdfWriter.FONT_BOLD, 46f, ACCENT, pageW / 2f, scoreCardTop - 90f);

        Color statusColor = categoryColor(category);
        drawStatusBadge(w, pageW / 2f, scoreCardTop - 130f, category.toUpperCase(), statusColor);

        // ---- Footer ----
        float footerLineY = margin + 90f;
        w.drawLine(margin, footerLineY, pageW - margin, footerLineY, FOOTER_LINE, 0.75f);

        String generatedOn = java.time.Instant.now().atZone(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm 'UTC'"));

        w.centeredTextAt("Generated on " + generatedOn, PdfWriter.FONT_REGULAR, 9f, GREY, pageW / 2f, footerLineY - 20f);
        w.centeredTextAt("Powered by Career Compass AI", PdfWriter.FONT_REGULAR, 9f, GREY, pageW / 2f, footerLineY - 36f);
        w.centeredTextAt("Developed by " + developerName, PdfWriter.FONT_REGULAR, 8.5f, GREY, pageW / 2f, footerLineY - 52f);

        w.setCursorY(footerLineY - 70f);
    }

    private void drawInfoCard(PdfWriter w, float x, float yTop, float width, float height,
                              String label, String value) throws IOException {
        float y = yTop - height;
        w.roundedRect(x, y, width, height, 8f, CARD_FILL, CARD_BORDER, 1f);
        w.leftTextAt(label, PdfWriter.FONT_BOLD, 8.5f, ACCENT, x + 14f, yTop - 22f);

        String displayValue = (value != null && value.length() > 42) ? value.substring(0, 39) + "..." : value;
        w.leftTextAt(displayValue, PdfWriter.FONT_BOLD, 14f, NAVY, x + 14f, yTop - 44f);
    }

    private void drawStatusBadge(PdfWriter w, float centerX, float centerY, String text, Color color) throws IOException {
        float paddingX = 16f;
        float textWidth = PdfWriter.FONT_BOLD.getStringWidth(text) / 1000f * 11f;
        float badgeWidth = textWidth + paddingX * 2f;
        float badgeHeight = 24f;
        float x = centerX - badgeWidth / 2f;
        float y = centerY - badgeHeight / 2f;

        w.roundedRect(x, y, badgeWidth, badgeHeight, badgeHeight / 2f, color, null, 0f);
        w.centeredTextAt(text, PdfWriter.FONT_BOLD, 11f, Color.WHITE, centerX, centerY - 4f);
    }

    // ---------------- Page 2: Executive Summary ----------------

    private void drawExecutiveSummary(PdfWriter w, Resume resume, JobDescription jd, Match match,
                                      ReportContent content, String category, String candidateEmail) throws IOException {
        w.sectionHeader("Executive Summary", NAVY);
        w.spacer(4);

        w.sectionHeader("Candidate Information", ACCENT);
        List<String[]> candidateRows = new ArrayList<>();
        candidateRows.add(new String[]{"Full Name", resume.getCandidateName() != null ? resume.getCandidateName() : "Not extracted"});
        candidateRows.add(new String[]{"Registered Email", candidateEmail});
        candidateRows.add(new String[]{"Resume File", resume.getOriginalFilename()});
        candidateRows.add(new String[]{"Education", nonEmpty(resume.getEducationLevel())});
        candidateRows.add(new String[]{"Experience", resume.getYearsExperience() + " years"});
        candidateRows.add(new String[]{"Certifications", joinOrNone(resume.getCertifications())});
        candidateRows.add(new String[]{"Projects", String.valueOf(countProjects(resume))});
        candidateRows.add(new String[]{"Technical Skills", joinOrNone(resume.getExtractedSkills())});
        candidateRows.add(new String[]{"Soft Skills", joinOrNone(resume.getSoftSkills())});
        w.twoColumnTable(candidateRows, 130, new Color(200, 200, 200));

        w.spacer(10);
        w.sectionHeader("Job Information", ACCENT);
        List<String[]> jobRows = new ArrayList<>();
        jobRows.add(new String[]{"Company", nonEmpty(jd.getCompanyName())});
        jobRows.add(new String[]{"Target Role", nonEmpty(jd.getTitle())});
        jobRows.add(new String[]{"Required Skills", joinOrNone(jd.getRequiredSkills())});
        jobRows.add(new String[]{"Required Experience", jd.getMinYearsExperience() + " years"});
        jobRows.add(new String[]{"Required Education", nonEmpty(jd.getMinEducationLevel())});
        w.twoColumnTable(jobRows, 130, new Color(200, 200, 200));

        w.spacer(10);
        w.sectionHeader("Overall Match Summary", ACCENT);
        w.text("Overall Match Score: " + match.getMatchScore() + " / 100", PdfWriter.FONT_BOLD, 11, NAVY);
        w.text("Category: " + category, PdfWriter.FONT_BOLD, 11, categoryColor(category));
        w.spacer(6);
        w.text("AI Summary:", PdfWriter.FONT_BOLD, 9.5f, NAVY);
        w.wrappedText(content.aiSummary(), PdfWriter.FONT_REGULAR, 9.5f, new Color(40, 40, 40));
    }

    // ---------------- Page 3: Skill Analysis ----------------

    private void drawSkillAnalysis(PdfWriter w, Match match, ReportContent content) throws IOException {
        w.sectionHeader("Skill Analysis", NAVY);

        List<String[]> skillRows = new ArrayList<>();
        skillRows.add(new String[]{"Matched Skills", joinOrNone(match.getMatchedSkills())});
        skillRows.add(new String[]{"Missing Skills", joinOrNone(match.getMissingSkills())});
        w.twoColumnTable(skillRows, 130, new Color(200, 200, 200));

        w.spacer(10);
        w.sectionHeader("Strengths", TEAL);
        w.bulletList(content.strengths(), new Color(40, 40, 40));

        w.spacer(8);
        w.sectionHeader("Weaknesses", CORAL);
        w.bulletList(content.weaknesses(), new Color(40, 40, 40));

        w.spacer(8);
        w.sectionHeader("Priority Skills", BLUE);
        w.bulletList(content.prioritySkills(), new Color(40, 40, 40));
    }

    // ---------------- Roadmap page (low match) ----------------

    private void drawRoadmapPage(PdfWriter w, ReportContent content) throws IOException {
        w.sectionHeader("Learning Roadmap", NAVY);
        w.spacer(6);
        if (content.roadmapBlocks() == null || content.roadmapBlocks().isEmpty()) {
            w.text("No skill gaps found — no roadmap needed.", PdfWriter.FONT_REGULAR, 10, GREY);
            return;
        }
        diagramRenderer.drawRoadmapBlocks(w, content.roadmapBlocks());
    }

    // ---------------- Interview prep page (high match) ----------------

    private void drawInterviewPrep(PdfWriter w, ReportContent content) throws IOException {
        w.sectionHeader("Interview Preparation Report", NAVY);
        w.spacer(4);
        for (ReportContent.InterviewPrepSection section : content.interviewPrepSections()) {
            w.text(section.heading(), PdfWriter.FONT_BOLD, 10.5f, ACCENT);
            w.wrappedText(section.content(), PdfWriter.FONT_REGULAR, 9.5f, new Color(40, 40, 40));
            w.spacer(6);
        }
    }

    // ---------------- Timeline page ----------------

    private void drawTimeline(PdfWriter w, ReportContent content, boolean highMatch) throws IOException {
        w.sectionHeader(highMatch ? "Interview Prep Timeline" : "Learning Timeline", NAVY);
        w.spacer(4);
        for (ReportContent.TimelineWeek week : content.timeline()) {
            w.text("Week " + week.week() + ": " + week.focus(), PdfWriter.FONT_BOLD, 10, ACCENT);
            w.wrappedText("Milestone: " + week.milestone(), PdfWriter.FONT_REGULAR, 9, new Color(40, 40, 40));
            w.spacer(6);
        }
        w.spacer(6);
        w.text("Estimated Time: " + content.estimatedTime(), PdfWriter.FONT_BOLD, 10, NAVY);
    }

    // ---------------- Resources page ----------------

    private void drawResourcesPage(PdfWriter w, List<String> missingSkills, List<LearningResource> allResources,
                                   ReportContent content) throws IOException {
        w.sectionHeader("Learning Resources", NAVY);
        w.spacer(4);

        for (String skill : missingSkills) {
            w.text(skill, PdfWriter.FONT_BOLD, 10.5f, ACCENT);

            List<LearningResource> matched = allResources.stream()
                    .filter(r -> SkillNameMatcher.matches(skill, r.getSkill()))
                    .toList();

            if (matched.isEmpty()) {
                w.text("   Official Documentation: not yet curated for this skill", PdfWriter.FONT_REGULAR, 9, GREY);
            } else {
                for (LearningResource r : matched) {
                    w.wrappedText("   " + r.getResourceType() + ": " + r.getTitle() + " (" + r.getUrl() + ")",
                            PdfWriter.FONT_REGULAR, 9, new Color(40, 40, 40));
                }
            }
            w.text("   Recommended: search YouTube for \"" + skill + " full course\"", PdfWriter.FONT_REGULAR, 9, GREY);

            List<String> questions = content.practiceQuestionsPerSkill() != null
                    ? content.practiceQuestionsPerSkill().getOrDefault(skill, List.of())
                    : List.of();
            if (!questions.isEmpty()) {
                w.text("   Practice Questions:", PdfWriter.FONT_REGULAR, 9, GREY);
                w.bulletList(questions, new Color(60, 60, 60));
            }
            w.spacer(8);
        }
    }

    // ---------------- Final page ----------------

    private void drawFinalPage(PdfWriter w, ReportContent content) throws IOException {
        w.sectionHeader("Career Summary", NAVY);
        w.spacer(4);

        w.text("Strengths", PdfWriter.FONT_BOLD, 10, TEAL);
        w.bulletList(content.strengths(), new Color(40, 40, 40));
        w.spacer(6);

        w.text("Weaknesses", PdfWriter.FONT_BOLD, 10, CORAL);
        w.bulletList(content.weaknesses(), new Color(40, 40, 40));
        w.spacer(6);

        w.text("Priority Focus Areas", PdfWriter.FONT_BOLD, 10, BLUE);
        w.bulletList(content.prioritySkills(), new Color(40, 40, 40));
        w.spacer(10);

        w.text("Final Recommendation", PdfWriter.FONT_BOLD, 10, NAVY);
        w.wrappedText(content.finalRecommendation(), PdfWriter.FONT_REGULAR, 9.5f, new Color(40, 40, 40));
        w.spacer(6);

        w.text("Estimated Preparation Time: " + content.estimatedTime(), PdfWriter.FONT_BOLD, 9.5f, NAVY);
        w.spacer(6);

        w.text("Next Action Plan", PdfWriter.FONT_BOLD, 10, NAVY);
        w.wrappedText(content.nextActionPlan(), PdfWriter.FONT_REGULAR, 9.5f, new Color(40, 40, 40));

        w.spacer(30);
        w.centeredTextInternal("Generated by Career Compass", PdfWriter.FONT_REGULAR, 9, GREY);
        w.centeredTextInternal("\u00A9 Career Compass. All Rights Reserved.", PdfWriter.FONT_REGULAR, 8.5f, GREY);
        w.centeredTextInternal("Developed by " + developerName, PdfWriter.FONT_REGULAR, 8.5f, GREY);
        w.centeredTextInternal("GitHub: " + githubUrl, PdfWriter.FONT_REGULAR, 8.5f, GREY);
        w.spacer(6);
        w.wrappedText("This report was generated automatically using AI and is intended for career guidance purposes.",
                PdfWriter.FONT_REGULAR, 8, GREY);
    }

    // ---------------- Helpers ----------------

    private String nonEmpty(String s) {
        return (s == null || s.isBlank()) ? "Not specified" : s;
    }

    private String joinOrNone(List<String> items) {
        if (items == null || items.isEmpty()) return "None";
        return String.join(", ", items);
    }

    private int countProjects(Resume resume) {
        if (resume.getProjectsJson() == null) return 0;
        return resume.getProjectsJson().split("\"name\"").length - 1;
    }

    private Color categoryColor(String category) {
        return switch (category) {
            case "Excellent" -> TEAL;
            case "Good" -> BLUE;
            case "Moderate" -> new Color(217, 119, 6);
            default -> CORAL;
        };
    }

    private void addPageNumbersAndFooter(PDDocument document) throws IOException {
        int total = document.getNumberOfPages();
        for (int i = 0; i < total; i++) {
            PDPage page = document.getPage(i);
            try (PDPageContentStream cs = new PDPageContentStream(document, page,
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                String footer = "Page " + (i + 1) + " of " + total;
                cs.beginText();
                cs.setFont(PdfWriter.FONT_REGULAR, 8f);
                cs.setNonStrokingColor(GREY);
                cs.newLineAtOffset(PdfWriter.PAGE_WIDTH - PdfWriter.MARGIN - 60, 25);
                cs.showText(footer);
                cs.endText();
            }
        }
    }
}