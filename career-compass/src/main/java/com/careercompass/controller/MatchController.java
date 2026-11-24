package com.careercompass.controller;

import com.careercompass.dto.MatchResponse;
import com.careercompass.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * Computes a match score between a specific resume and job description.
     * Generates and stores embeddings for both (if not already current),
     * then combines semantic similarity with hard-constraint checks against
     * education level and years of experience.
     */
    @PostMapping
    public ResponseEntity<MatchResponse> computeMatch(
            @RequestParam UUID resumeId,
            @RequestParam UUID jdId
    ) {
        MatchResponse response = matchService.computeMatch(resumeId, jdId);
        return ResponseEntity.ok(response);
    }
}
