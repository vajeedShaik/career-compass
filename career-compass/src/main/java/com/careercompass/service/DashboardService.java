package com.careercompass.service;

import com.careercompass.dto.DashboardResponse;
import com.careercompass.entity.Match;
import com.careercompass.entity.Resume;
import com.careercompass.entity.Roadmap;
import com.careercompass.repository.MatchRepository;
import com.careercompass.repository.ResumeRepository;
import com.careercompass.repository.RoadmapRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Computes dashboard stats from real data — no hardcoded numbers.
 * Deliberately simple Java-side aggregation rather than complex SQL, since
 * data volumes here are small (per-user counts), and it keeps the queries
 * easy to reason about.
 */
@Service
public class DashboardService {

    private final ResumeRepository resumeRepository;
    private final MatchRepository matchRepository;
    private final RoadmapRepository roadmapRepository;

    public DashboardService(ResumeRepository resumeRepository,
                            MatchRepository matchRepository,
                            RoadmapRepository roadmapRepository) {
        this.resumeRepository = resumeRepository;
        this.matchRepository = matchRepository;
        this.roadmapRepository = roadmapRepository;
    }

    public DashboardResponse getDashboard(UUID userId) {
        List<Resume> resumes = resumeRepository.findByUserId(userId);
        List<Match> matches = matchRepository.findByUserId(userId);
        List<UUID> matchIds = matches.stream().map(Match::getId).toList();
        List<Roadmap> roadmaps = matchIds.isEmpty()
                ? List.of()
                : roadmapRepository.findByMatchIdIn(matchIds);

        double averageScore = matches.stream()
                .map(Match::getMatchScore)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        List<DashboardResponse.ActivityItem> activity = new ArrayList<>();
        for (Resume r : resumes) {
            activity.add(new DashboardResponse.ActivityItem(
                    "RESUME", "Resume uploaded: " + r.getOriginalFilename(), r.getCreatedAt()));
        }
        for (Match m : matches) {
            activity.add(new DashboardResponse.ActivityItem(
                    "MATCH", "Match computed: " + m.getMatchScore() + "% score", m.getCreatedAt()));
        }
        for (Roadmap rm : roadmaps) {
            activity.add(new DashboardResponse.ActivityItem(
                    "ROADMAP", "Roadmap generated", rm.getCreatedAt()));
        }
        activity.sort(Comparator.comparing(DashboardResponse.ActivityItem::createdAt).reversed());
        List<DashboardResponse.ActivityItem> recent = activity.stream().limit(5).toList();

        return new DashboardResponse(resumes.size(), roadmaps.size(), averageScore, recent);
    }
}