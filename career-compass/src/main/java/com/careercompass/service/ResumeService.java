package com.careercompass.service;

import com.careercompass.dto.ParsedResumeData;
import com.careercompass.dto.ResumeResponse;
import com.careercompass.entity.Resume;
import com.careercompass.repository.ResumeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class ResumeService {

    private final PdfExtractionService pdfExtractionService;
    private final ResumeParsingService resumeParsingService;
    private final ResumeRepository resumeRepository;
    private final ObjectMapper objectMapper;

    public ResumeService(PdfExtractionService pdfExtractionService,
                          ResumeParsingService resumeParsingService,
                          ResumeRepository resumeRepository,
                          ObjectMapper objectMapper) {
        this.pdfExtractionService = pdfExtractionService;
        this.resumeParsingService = resumeParsingService;
        this.resumeRepository = resumeRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ResumeResponse uploadAndParse(UUID userId, MultipartFile file) {
        String rawText = pdfExtractionService.extractText(file);
        ParsedResumeData parsed = resumeParsingService.parse(rawText);

        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setOriginalFilename(file.getOriginalFilename());
        resume.setCandidateName(parsed.candidateName());
        resume.setRawText(rawText);
        resume.setExtractedSkills(parsed.skills());
        resume.setSoftSkills(parsed.softSkills());
        resume.setCertifications(parsed.certifications());
        resume.setEducationLevel(parsed.educationLevel());
        resume.setYearsExperience(parsed.yearsOfExperience());
        resume.setProjectsJson(toJson(parsed.projects()));

        Resume saved = resumeRepository.save(resume);

        return new ResumeResponse(
                saved.getId(),
                saved.getOriginalFilename(),
                saved.getCandidateName(),
                saved.getExtractedSkills(),
                saved.getSoftSkills(),
                saved.getCertifications(),
                saved.getEducationLevel(),
                saved.getYearsExperience(),
                parsed.projects(),
                saved.getCreatedAt()
        );
    }

    private String toJson(List<ParsedResumeData.ProjectData> projects) {
        try {
            return objectMapper.writeValueAsString(projects);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize extracted projects: " + e.getMessage(), e);
        }
    }
}
