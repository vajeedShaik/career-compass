package com.careercompass.controller;

import com.careercompass.dto.RoadmapResponse;
import com.careercompass.service.RoadmapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    /**
     * Generates a personalized preparation roadmap for the skill gaps found
     * in a previously computed match (see Module 3, /api/matches).
     */
    @PostMapping
    public ResponseEntity<RoadmapResponse> generateRoadmap(@RequestParam UUID matchId) {
        RoadmapResponse response = roadmapService.generate(matchId);
        return ResponseEntity.ok(response);
    }
}
