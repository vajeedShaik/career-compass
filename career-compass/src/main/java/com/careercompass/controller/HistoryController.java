package com.careercompass.controller;

import com.careercompass.dto.HistoryResponse;
import com.careercompass.security.CurrentUserProvider;
import com.careercompass.service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HistoryController {

    private final HistoryService historyService;
    private final CurrentUserProvider currentUserProvider;

    public HistoryController(HistoryService historyService, CurrentUserProvider currentUserProvider) {
        this.historyService = historyService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/api/history")
    public ResponseEntity<HistoryResponse> getHistory() {
        HistoryResponse response = historyService.getHistory(currentUserProvider.getUserId());
        return ResponseEntity.ok(response);
    }
}