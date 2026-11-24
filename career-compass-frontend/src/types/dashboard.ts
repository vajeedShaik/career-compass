export interface ActivityItem {
    type: "RESUME" | "MATCH" | "ROADMAP";
    message: string;
    createdAt: string;
}

export interface DashboardResponse {
    resumesUploaded: number;
    reportsGenerated: number;
    averageMatchScore: number;
    recentActivity: ActivityItem[];
}