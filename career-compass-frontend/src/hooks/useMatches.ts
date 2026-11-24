import { useMutation } from "@tanstack/react-query";
import { toast } from "sonner";
import { isAxiosError } from "axios";
import { computeMatch } from "@/api/matches";

export function useComputeMatch() {
  return useMutation({
    mutationFn: ({ resumeId, jdId }: { resumeId: string; jdId: string }) =>
      computeMatch(resumeId, jdId),
    onError: (error) => {
      const message = isAxiosError(error)
        ? error.response?.data?.message ?? "Failed to compute match score"
        : "Failed to compute match score";
      toast.error(message);
    },
  });
}
