import { apiClient } from "./client";
import type { HistoryResponse } from "@/types/history";

export async function getHistory(): Promise<HistoryResponse> {
    const { data } = await apiClient.get<HistoryResponse>("/api/history");
    return data;
}