import { apiClient } from "./client";
import type { ResumeResponse } from "@/types/resume";

export async function uploadResume(file: File): Promise<ResumeResponse> {
  const formData = new FormData();
  formData.append("file", file);

  const { data } = await apiClient.post<ResumeResponse>("/api/resumes/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return data;
}
