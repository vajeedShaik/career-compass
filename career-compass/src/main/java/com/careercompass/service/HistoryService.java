package com.careercompass.service;

import com.careercompass.dto.HistoryResponse;
import com.careercompass.repository.JobDescriptionRepository;
import com.careercompass.repository.MatchRepository;
import com.careercompass.repository.ResumeRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class HistoryService {

    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final MatchRepository matchRepository;

    public HistoryService(ResumeRepository resumeRepository,
                          JobDescriptionRepository jobDescriptionRepository,
                          MatchRepository matchRepository) {
        this.resumeRepository = resumeRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.matchRepository = matchRepository;
    }

    public HistoryResponse getHistory(UUID userId) {
        List<HistoryResponse.ResumeSummary> resumes = resumeRepository.findByUserId(userId).stream()
                .map(r -> new HistoryResponse.ResumeSummary(r.getId(), r.getOriginalFilename(), r.getCreatedAt()))
                .sorted(Comparator.comparing(HistoryResponse.ResumeSummary::createdAt).reversed())
                .toList();

        List<HistoryResponse.JobSummary> jobs = jobDescriptionRepository.findByUserId(userId).stream()
                .map(j -> new HistoryResponse.JobSummary(j.getId(), j.getTitle(), j.getCompanyName(), j.getCreatedAt()))
                .sorted(Comparator.comparing(HistoryResponse.JobSummary::createdAt).reversed())
                .toList();

        List<HistoryResponse.MatchSummary> matches = matchRepository.findByUserId(userId).stream()
                .map(m -> new HistoryResponse.MatchSummary(m.getId(), m.getMatchScore(), m.getCreatedAt()))
                .sorted(Comparator.comparing(HistoryResponse.MatchSummary::createdAt).reversed())
                .toList();

        return new HistoryResponse(resumes, jobs, matches);
    }
}