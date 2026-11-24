package com.careercompass.service.pdf;

import com.careercompass.dto.ReportContent;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

/**
 * Draws the actual flow diagrams (process pipeline, roadmap blocks) as real
 * PDFBox vector shapes — boxes, borders, and connecting arrows — kept
 * separate from PdfGenerationService so diagram logic doesn't get tangled
 * with page/section layout logic.
 */
@Component
public class PdfDiagramRenderer {

    private static final Color GRAY = new Color(107, 114, 128);
    private static final Color PURPLE = new Color(124, 58, 237);
    private static final Color TEAL = new Color(13, 148, 136);
    private static final Color AMBER = new Color(217, 119, 6);
    private static final Color CORAL = new Color(220, 38, 38);
    private static final Color BLUE = new Color(37, 99, 235);

    private static final Color[] PALETTE = {GRAY, PURPLE, TEAL, AMBER, CORAL, BLUE};

    /** Draws the 5-stage process pipeline: Resume -> AI Parsing -> Skill Match -> Gap Analysis -> Roadmap/Interview Prep. */
    public void drawProcessFlow(PdfWriter w, boolean highMatch) throws IOException {
        String[] titles = {
                "Resume", "AI Parsing", "Skill Match", "Gap Analysis",
                highMatch ? "Interview Prep" : "Learning Roadmap"
        };
        String[] subtitles = {
                "PDF uploaded", "Structured extraction", "Embedding + hybrid score",
                "Matched vs missing skills", highMatch ? "Readiness plan generated" : "Personalized plan generated"
        };
        Color[] colors = {GRAY, PURPLE, TEAL, AMBER, highMatch ? BLUE : CORAL};

        float boxWidth = 260f;
        float boxHeight = 46f;
        float gap = 24f;
        float x = PdfWriter.MARGIN + (PdfWriter.CONTENT_WIDTH - boxWidth) / 2;

        float totalHeight = titles.length * boxHeight + (titles.length - 1) * gap;
        w.ensureSpace(totalHeight + 20);

        float yTop = w.getCursorY();
        for (int i = 0; i < titles.length; i++) {
            w.box(x, yTop, boxWidth, boxHeight, titles[i], subtitles[i], colors[i]);
            if (i < titles.length - 1) {
                w.downArrow(x + boxWidth / 2, yTop - boxHeight, gap, new Color(46, 111, 149));
            }
            yTop -= (boxHeight + gap);
        }
        w.setCursorY(yTop + gap - 10);
    }

    /** Draws each roadmap block as a connected box with skill/difficulty/duration/prerequisites. */
    public void drawRoadmapBlocks(PdfWriter w, List<ReportContent.RoadmapBlock> blocks) throws IOException {
        float boxWidth = 340f;
        float boxHeight = 58f;
        float gap = 22f;
        float x = PdfWriter.MARGIN + (PdfWriter.CONTENT_WIDTH - boxWidth) / 2;

        for (int i = 0; i < blocks.size(); i++) {
            ReportContent.RoadmapBlock b = blocks.get(i);
            w.ensureSpace(boxHeight + gap + 20);
            float yTop = w.getCursorY();
            Color color = PALETTE[i % PALETTE.length];

            String title = (i + 1) + ". " + b.skill();
            String subtitle = "Difficulty: " + safe(b.difficulty()) + "   |   Duration: " + safe(b.durationWeeks());

            w.box(x, yTop, boxWidth, boxHeight, title, subtitle, color);
            w.setCursorY(yTop - boxHeight - 4);

            if (b.prerequisites() != null && !b.prerequisites().isBlank()
                    && !b.prerequisites().equalsIgnoreCase("none")) {
                w.text("   Prerequisites: " + b.prerequisites(), PdfWriter.FONT_REGULAR, 8f, new Color(90, 90, 90));
            }
            if (i < blocks.size() - 1) {
                w.downArrow(x + boxWidth / 2, w.getCursorY() + 4, gap, new Color(46, 111, 149));
                w.setCursorY(w.getCursorY() - gap + 4);
            }
        }
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "n/a" : s;
    }
}
