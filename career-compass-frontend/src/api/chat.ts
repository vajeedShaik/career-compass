import { apiClient } from "./client";
import type { ChatResponse } from "@/types/roadmap";

export async function sendChatMessage(
  roadmapId: string,
  message: string
): Promise<ChatResponse> {
  const { data } = await apiClient.post<ChatResponse>(`/api/roadmaps/${roadmapId}/chat`, {
    message,
  });
  return data;
}
