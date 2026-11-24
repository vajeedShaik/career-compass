import { useMutation } from "@tanstack/react-query";
import { toast } from "sonner";
import { isAxiosError } from "axios";
import { uploadResume } from "@/api/resumes";

export function useUploadResume() {
  return useMutation({
    mutationFn: (file: File) => uploadResume(file),
    onError: (error) => {
      const message = isAxiosError(error)
        ? error.response?.data?.message ?? "Failed to process resume"
        : "Failed to process resume";
      toast.error(message);
    },
  });
}
