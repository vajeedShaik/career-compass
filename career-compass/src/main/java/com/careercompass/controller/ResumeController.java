package com.careercompass.controller;

import com.careercompass.dto.ResumeResponse;
import com.careercompass.security.CurrentUserProvider;
import com.careercompass.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;
    private final CurrentUserProvider currentUserProvider;

    public ResumeController(ResumeService resumeService, CurrentUserProvider currentUserProvider) {
        this.resumeService = resumeService;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Uploads a PDF resume, extracts its text, and derives structured data
     * via the AI extraction service. userId now comes from the
     * authenticated JWT (see Module 6: Auth) rather than a request param.
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ResumeResponse> uploadResume(@RequestParam("file") MultipartFile file) {
        ResumeResponse response = resumeService.uploadAndParse(currentUserProvider.getUserId(), file);
        return ResponseEntity.ok(response);
    }
}
