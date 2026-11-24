package com.careercompass.dto;

import java.util.List;

public record DashboardResponse(
        int resumesUploaded,
        int reportsGenerated,
        double averageMatchScore,
        List<ActivityItem> recentActivity
) {
    public record ActivityItem(String type, String message, java.time.Instant createdAt) {}
}