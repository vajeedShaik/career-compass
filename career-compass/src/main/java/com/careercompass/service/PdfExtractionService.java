package com.careercompass.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Extracts raw text from an uploaded PDF resume.
 * Kept as its own small service so the parsing strategy (PDFBox today,
 * OCR fallback later for scanned resumes) can evolve independently.
 */
@Service
public class PdfExtractionService {

    public String extractText(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text == null || text.isBlank()) {
                throw new IllegalStateException(
                        "No extractable text found in PDF. It may be a scanned image — OCR support is not yet implemented.");
            }
            return text;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read PDF file: " + e.getMessage(), e);
        }
    }
}
