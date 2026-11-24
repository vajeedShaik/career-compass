import { apiClient } from "./client";
import type { MatchResponse } from "@/types/match";

export async function computeMatch(resumeId: string, jdId: string): Promise<MatchResponse> {
  const { data } = await apiClient.post<MatchResponse>("/api/matches", null, {
    params: { resumeId, jdId },
  });
  return data;
}
