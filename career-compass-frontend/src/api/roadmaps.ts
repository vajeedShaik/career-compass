import { apiClient } from "./client";
import type { RoadmapResponse } from "@/types/roadmap";

export async function generateRoadmap(matchId: string): Promise<RoadmapResponse> {
  const { data } = await apiClient.post<RoadmapResponse>("/api/roadmaps", null, {
    params: { matchId },
  });
  return data;
}
