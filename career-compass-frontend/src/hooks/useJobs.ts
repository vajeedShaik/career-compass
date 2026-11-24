import { useMutation } from "@tanstack/react-query";
import { toast } from "sonner";
import { isAxiosError } from "axios";
import { submitJob, type SubmitJobParams } from "@/api/jobs";

export function useSubmitJob() {
  return useMutation({
    mutationFn: (params: SubmitJobParams) => submitJob(params),
    onError: (error) => {
      const message = isAxiosError(error)
        ? error.response?.data?.message ?? "Failed to process job description"
        : "Failed to process job description";
      toast.error(message);
    },
  });
}
