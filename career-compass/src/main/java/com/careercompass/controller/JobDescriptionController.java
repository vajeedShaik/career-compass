package com.careercompass.controller;

import com.careercompass.dto.JobDescriptionResponse;
import com.careercompass.security.CurrentUserProvider;
import com.careercompass.service.JobDescriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/jobs")
public class JobDescriptionController {

    private final JobDescriptionService jobDescriptionService;
    private final CurrentUserProvider currentUserProvider;

    public JobDescriptionController(JobDescriptionService jobDescriptionService,
                                     CurrentUserProvider currentUserProvider) {
        this.jobDescriptionService = jobDescriptionService;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Submits a job description either as pasted text or a PDF upload.
     * Provide exactly one of 'rawText' or 'file'. If both are given, the
     * file takes priority. userId now comes from the authenticated JWT.
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<JobDescriptionResponse> submitJobDescription(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "rawText", required = false) String rawText,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        JobDescriptionResponse response = jobDescriptionService.submit(
                currentUserProvider.getUserId(), title, rawText, file);
        return ResponseEntity.ok(response);
    }
}
