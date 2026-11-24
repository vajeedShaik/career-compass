package com.careercompass.service.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages cursor position and automatic page breaks for hand-drawn PDF
 * content via PDFBox's low-level content stream API. PDFBox has no
 * built-in text wrapping or flow layout — this fills that gap for the
 * simple report we need here.
 */
public class PdfWriter implements AutoCloseable {

    public static final float MARGIN = 50f;
    public static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    public static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    public static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    public static final PDFont FONT_BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public static final PDFont FONT_REGULAR =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private final PDDocument document;
    private PDPage currentPage;
    private PDPageContentStream cs;
    private float cursorY;

    public PdfWriter(PDDocument document) throws IOException {
        this.document = document;
        newPage();
    }

    private void newPage() throws IOException {
        if (cs != null) {
            cs.close();
        }

        currentPage = new PDPage(PDRectangle.A4);
        document.addPage(currentPage);

        cs = new PDPageContentStream(document, currentPage);

        cursorY = PAGE_HEIGHT - MARGIN;
    }

    /**
     * AI-generated text often contains Unicode punctuation such as
     * non-breaking hyphens, en dashes, em dashes, curly quotes, ellipsis,
     * and non-breaking spaces.
     *
     * PDFBox's built-in Helvetica font cannot render many of these
     * characters. Convert common Unicode punctuation to ASCII equivalents
     * before sending text to PDFBox.
     *
     * The final regex is a safety net for any remaining non-Latin-1
     * characters.
     */
    private String sanitize(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\u2011", "-")   // non-breaking hyphen
                .replace("\u2010", "-")   // hyphen
                .replace("\u2013", "-")   // en dash
                .replace("\u2014", "-")   // em dash
                .replace("\u2018", "'")   // left single quote
                .replace("\u2019", "'")   // right single quote
                .replace("\u201C", "\"")  // left double quote
                .replace("\u201D", "\"")  // right double quote
                .replace("\u2026", "...") // ellipsis
                .replace("\u00A0", " ")   // non-breaking space
                .replaceAll("[^\\x00-\\xFF]", "?");
    }

    public void ensureSpace(float neededHeight) throws IOException {
        if (cursorY - neededHeight < MARGIN) {
            newPage();
        }
    }

    public void text(
            String content,
            PDFont font,
            float fontSize,
            Color color
    ) throws IOException {

        content = sanitize(content);

        ensureSpace(fontSize + 4);

        cs.beginText();
        cs.setFont(font, fontSize);
        cs.setNonStrokingColor(color);
        cs.newLineAtOffset(MARGIN, cursorY);
        cs.showText(content);
        cs.endText();

        cursorY -= (fontSize + 6);
    }

    /**
     * Wraps text to CONTENT_WIDTH and writes each line,
     * advancing the cursor.
     */
    public void wrappedText(
            String content,
            PDFont font,
            float fontSize,
            Color color
    ) throws IOException {

        for (String line : wrap(content, font, fontSize, CONTENT_WIDTH)) {
            text(line, font, fontSize, color);
        }
    }

    public void spacer(float height) {
        cursorY -= height;
    }

    public List<String> wrap(
            String text,
            PDFont font,
            float fontSize,
            float maxWidth
    ) throws IOException {

        text = sanitize(text);

        List<String> lines = new ArrayList<>();

        for (String paragraph : text.split("\n")) {

            if (paragraph.isBlank()) {
                lines.add("");
                continue;
            }

            StringBuilder line = new StringBuilder();

            for (String word : paragraph.split(" ")) {

                String candidate =
                        line.isEmpty()
                                ? word
                                : line + " " + word;

                float width =
                        font.getStringWidth(candidate)
                                / 1000
                                * fontSize;

                if (width > maxWidth && !line.isEmpty()) {

                    lines.add(line.toString());

                    line = new StringBuilder(word);

                } else {

                    line = new StringBuilder(candidate);
                }
            }

            if (!line.isEmpty()) {
                lines.add(line.toString());
            }
        }

        return lines;
    }

    // ------------------------------------------------------------
    // Diagram primitives
    // ------------------------------------------------------------

    public void box(
            float x,
            float yTop,
            float width,
            float height,
            String title,
            String subtitle,
            Color strokeColor
    ) throws IOException {

        title = sanitize(title);
        subtitle = sanitize(subtitle);

        ensureSpace(0);

        float y = yTop - height;

        cs.setNonStrokingColor(
                new Color(
                        strokeColor.getRed(),
                        strokeColor.getGreen(),
                        strokeColor.getBlue(),
                        30
                )
        );

        cs.addRect(x, y, width, height);
        cs.fill();

        cs.setStrokingColor(strokeColor);
        cs.setLineWidth(1.2f);

        cs.addRect(x, y, width, height);
        cs.stroke();

        // Title
        cs.beginText();

        cs.setFont(FONT_BOLD, 9.5f);
        cs.setNonStrokingColor(new Color(27, 42, 74));

        float titleWidth =
                FONT_BOLD.getStringWidth(title)
                        / 1000
                        * 9.5f;

        cs.newLineAtOffset(
                x + (width - titleWidth) / 2,
                y + height / 2 + (subtitle != null ? 4 : -3)
        );

        cs.showText(title);
        cs.endText();

        // Subtitle
        if (subtitle != null) {

            cs.beginText();

            cs.setFont(FONT_REGULAR, 7.5f);
            cs.setNonStrokingColor(new Color(90, 90, 90));

            float subWidth =
                    FONT_REGULAR.getStringWidth(subtitle)
                            / 1000
                            * 7.5f;

            cs.newLineAtOffset(
                    x + (width - subWidth) / 2,
                    y + height / 2 - 10
            );

            cs.showText(subtitle);

            cs.endText();
        }
    }

    public void downArrow(
            float xCenter,
            float yTop,
            float length,
            Color color
    ) throws IOException {

        float yBottom = yTop - length;

        cs.setStrokingColor(color);
        cs.setLineWidth(1.2f);

        cs.moveTo(xCenter, yTop);
        cs.lineTo(xCenter, yBottom + 6);
        cs.stroke();

        cs.setNonStrokingColor(color);

        cs.moveTo(xCenter - 4, yBottom + 6);
        cs.lineTo(xCenter + 4, yBottom + 6);
        cs.lineTo(xCenter, yBottom);

        cs.closePath();
        cs.fill();
    }

    public float getCursorY() {
        return cursorY;
    }

    public void setCursorY(float y) {
        this.cursorY = y;
    }

    public void forceNewPage() throws IOException {
        newPage();
    }

    public void sectionHeader(
            String text,
            Color color
    ) throws IOException {

        ensureSpace(28);

        cursorY -= 6;

        text(text, FONT_BOLD, 13f, color);

        cs.setStrokingColor(color);
        cs.setLineWidth(1f);

        cs.moveTo(MARGIN, cursorY + 10);
        cs.lineTo(MARGIN + CONTENT_WIDTH, cursorY + 10);

        cs.stroke();

        cursorY -= 6;
    }

    public void bulletList(
            List<String> items,
            Color color
    ) throws IOException {

        for (String item : items) {

            List<String> lines =
                    wrap(
                            "-  " + item,
                            FONT_REGULAR,
                            9.5f,
                            CONTENT_WIDTH - 10
                    );

            for (String line : lines) {
                text(
                        line,
                        FONT_REGULAR,
                        9.5f,
                        color
                );
            }
        }
    }

    /**
     * Simple bordered two-column table:
     * bold label column, wrapped value column.
     */
    public void twoColumnTable(
            List<String[]> rows,
            float labelWidth,
            Color borderColor
    ) throws IOException {

        float valueWidth =
                CONTENT_WIDTH - labelWidth - 16;

        for (String[] row : rows) {

            String label =
                    sanitize(row[0]);

            String value =
                    sanitize(
                            row.length > 1 && row[1] != null
                                    ? row[1]
                                    : "-"
                    );

            List<String> valueLines =
                    wrap(
                            value,
                            FONT_REGULAR,
                            9f,
                            valueWidth
                    );

            float rowHeight =
                    Math.max(
                            20,
                            valueLines.size() * 12 + 8
                    );

            ensureSpace(rowHeight);

            float yTop = cursorY + 4;
            float yBottom = yTop - rowHeight;

            // Outer border
            cs.setStrokingColor(borderColor);
            cs.setLineWidth(0.6f);

            cs.addRect(
                    MARGIN,
                    yBottom,
                    CONTENT_WIDTH,
                    rowHeight
            );

            cs.stroke();

            // Vertical divider
            cs.moveTo(
                    MARGIN + labelWidth,
                    yTop
            );

            cs.lineTo(
                    MARGIN + labelWidth,
                    yBottom
            );

            cs.stroke();

            // Label
            cs.beginText();

            cs.setFont(FONT_BOLD, 9f);
            cs.setNonStrokingColor(
                    new Color(27, 42, 74)
            );

            cs.newLineAtOffset(
                    MARGIN + 6,
                    yTop - 12
            );

            cs.showText(label);

            cs.endText();

            // Value
            float lineY = yTop - 12;

            for (String line : valueLines) {

                line = sanitize(line);

                cs.beginText();

                cs.setFont(FONT_REGULAR, 9f);
                cs.setNonStrokingColor(
                        new Color(40, 40, 40)
                );

                cs.newLineAtOffset(
                        MARGIN + labelWidth + 8,
                        lineY
                );

                cs.showText(line);

                cs.endText();

                lineY -= 12;
            }

            cursorY = yBottom - 4;
        }
    }

    public void centeredTextInternal(
            String content,
            PDFont font,
            float fontSize,
            Color color
    ) throws IOException {

        content = sanitize(content);

        ensureSpace(fontSize + 4);

        float textWidth =
                font.getStringWidth(content)
                        / 1000
                        * fontSize;

        float x =
                (PAGE_WIDTH - textWidth) / 2;

        cs.beginText();

        cs.setFont(font, fontSize);
        cs.setNonStrokingColor(color);

        cs.newLineAtOffset(x, cursorY);

        cs.showText(content);

        cs.endText();

        cursorY -= (fontSize + 6);
    }

    // ------------------------------------------------------------
    // Absolute-position drawing helpers
    // ------------------------------------------------------------

    /**
     * Fills a rectangle at absolute page coordinates.
     * Does not affect cursorY.
     */
    public void filledRect(
            float x,
            float y,
            float width,
            float height,
            Color color
    ) throws IOException {

        cs.setNonStrokingColor(color);

        cs.addRect(
                x,
                y,
                width,
                height
        );

        cs.fill();
    }

    /**
     * Strokes a rectangle outline at absolute page coordinates.
     * Does not affect cursorY.
     */
    public void strokedRect(
            float x,
            float y,
            float width,
            float height,
            Color color,
            float lineWidth
    ) throws IOException {

        cs.setStrokingColor(color);
        cs.setLineWidth(lineWidth);

        cs.addRect(
                x,
                y,
                width,
                height
        );

        cs.stroke();
    }

    /**
     * Draws a straight line between two absolute points.
     * Does not affect cursorY.
     */
    public void drawLine(
            float x1,
            float y1,
            float x2,
            float y2,
            Color color,
            float lineWidth
    ) throws IOException {

        cs.setStrokingColor(color);
        cs.setLineWidth(lineWidth);

        cs.moveTo(x1, y1);
        cs.lineTo(x2, y2);

        cs.stroke();
    }

    /**
     * Draws a rounded rectangle (card / badge)
     * at absolute coordinates.
     *
     * Pass null for fillColor or strokeColor
     * to skip that operation.
     */
    public void roundedRect(
            float x,
            float y,
            float width,
            float height,
            float radius,
            Color fillColor,
            Color strokeColor,
            float lineWidth
    ) throws IOException {

        buildRoundedPath(
                x,
                y,
                width,
                height,
                radius
        );

        if (fillColor != null) {
            cs.setNonStrokingColor(fillColor);
        }

        if (strokeColor != null) {
            cs.setStrokingColor(strokeColor);
            cs.setLineWidth(lineWidth);
        }

        if (fillColor != null && strokeColor != null) {

            cs.fillAndStroke();

        } else if (fillColor != null) {

            cs.fill();

        } else if (strokeColor != null) {

            cs.stroke();
        }
    }

    private void buildRoundedPath(
            float x,
            float y,
            float width,
            float height,
            float radius
    ) throws IOException {

        float x0 = x;
        float y0 = y;

        float x1 = x + width;
        float y1 = y + height;

        cs.moveTo(
                x0 + radius,
                y0
        );

        cs.lineTo(
                x1 - radius,
                y0
        );

        cs.curveTo(
                x1,
                y0,
                x1,
                y0,
                x1,
                y0 + radius
        );

        cs.lineTo(
                x1,
                y1 - radius
        );

        cs.curveTo(
                x1,
                y1,
                x1,
                y1,
                x1 - radius,
                y1
        );

        cs.lineTo(
                x0 + radius,
                y1
        );

        cs.curveTo(
                x0,
                y1,
                x0,
                y1,
                x0,
                y1 - radius
        );

        cs.lineTo(
                x0,
                y0 + radius
        );

        cs.curveTo(
                x0,
                y0,
                x0,
                y0,
                x0 + radius,
                y0
        );

        cs.closePath();
    }

    /**
     * Draws text centered horizontally around centerX
     * at an absolute baseline y.
     *
     * Does not affect cursorY.
     */
    public void centeredTextAt(
            String content,
            PDFont font,
            float fontSize,
            Color color,
            float centerX,
            float y
    ) throws IOException {

        content = sanitize(content);

        float textWidth =
                font.getStringWidth(content)
                        / 1000
                        * fontSize;

        float x =
                centerX - textWidth / 2;

        cs.beginText();

        cs.setFont(font, fontSize);
        cs.setNonStrokingColor(color);

        cs.newLineAtOffset(x, y);

        cs.showText(content);

        cs.endText();
    }

    /**
     * Draws text left-aligned at an absolute position.
     * Does not affect cursorY.
     */
    public void leftTextAt(
            String content,
            PDFont font,
            float fontSize,
            Color color,
            float x,
            float y
    ) throws IOException {

        content = sanitize(content);

        cs.beginText();

        cs.setFont(font, fontSize);
        cs.setNonStrokingColor(color);

        cs.newLineAtOffset(x, y);

        cs.showText(content);

        cs.endText();
    }

    public PDDocument getDocument() {
        return document;
    }

    @Override
    public void close() throws IOException {
        if (cs != null) {
            cs.close();
        }
    }
}