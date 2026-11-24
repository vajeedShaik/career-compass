package com.careercompass.service;

import com.careercompass.dto.JobDescriptionResponse;
import com.careercompass.dto.ParsedJdData;
import com.careercompass.entity.JobDescription;
import com.careercompass.repository.JobDescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class JobDescriptionService {

    private final PdfExtractionService pdfExtractionService;
    private final JdParsingService jdParsingService;
    private final JobDescriptionRepository jobDescriptionRepository;

    public JobDescriptionService(PdfExtractionService pdfExtractionService,
                                  JdParsingService jdParsingService,
                                  JobDescriptionRepository jobDescriptionRepository) {
        this.pdfExtractionService = pdfExtractionService;
        this.jdParsingService = jdParsingService;
        this.jobDescriptionRepository = jobDescriptionRepository;
    }

    /**
     * Accepts a JD as either pasted text or a PDF file — exactly one of
     * rawText / file should be provided. If both are given, the file wins.
     */
    @Transactional
    public JobDescriptionResponse submit(UUID userId, String title, String rawText, MultipartFile file) {
        String resolvedText = resolveText(rawText, file);
        ParsedJdData parsed = jdParsingService.parse(resolvedText);

        JobDescription jd = new JobDescription();
        jd.setUserId(userId);
        jd.setTitle(title);
        jd.setCompanyName(parsed.companyName());
        jd.setRawText(resolvedText);
        jd.setRequiredSkills(parsed.requiredSkills());
        jd.setMinEducationLevel(parsed.minEducationLevel());
        jd.setMinYearsExperience(parsed.minYearsExperience());

        JobDescription saved = jobDescriptionRepository.save(jd);

        return new JobDescriptionResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getCompanyName(),
                saved.getRequiredSkills(),
                saved.getMinEducationLevel(),
                saved.getMinYearsExperience(),
                saved.getCreatedAt()
        );
    }

    private String resolveText(String rawText, MultipartFile file) {
        boolean hasFile = file != null && !file.isEmpty();
        boolean hasText = rawText != null && !rawText.isBlank();

        if (!hasFile && !hasText) {
            throw new IllegalArgumentException(
                    "Provide either 'rawText' or a PDF 'file' — neither was given.");
        }

        // If a file is provided, it takes priority over any accompanying text.
        if (hasFile) {
            return pdfExtractionService.extractText(file);
        }
        return rawText;
    }
}
