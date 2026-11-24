import { apiClient } from "./client";
import type { JobDescriptionResponse } from "@/types/job";

export interface SubmitJobParams {
  title?: string;
  rawText?: string;
  file?: File;
}

/**
 * Backend accepts exactly one of rawText or file (file takes priority if
 * both are given) — see JobDescriptionController.submitJobDescription.
 */
export async function submitJob(params: SubmitJobParams): Promise<JobDescriptionResponse> {
  const formData = new FormData();
  if (params.title) formData.append("title", params.title);
  if (params.rawText) formData.append("rawText", params.rawText);
  if (params.file) formData.append("file", params.file);

  const { data } = await apiClient.post<JobDescriptionResponse>("/api/jobs", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return data;
}
