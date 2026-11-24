import { useMutation } from "@tanstack/react-query";
import { toast } from "sonner";
import { isAxiosError } from "axios";
import { generateRoadmap } from "@/api/roadmaps";

export function useGenerateRoadmap() {
  return useMutation({
    mutationFn: (matchId: string) => generateRoadmap(matchId),
    onSuccess: (data) => {
      if (data.emailSent) {
        toast.success("Your roadmap has been emailed to you.");
      }
    },
    onError: (error) => {
      const message = isAxiosError(error)
        ? error.response?.data?.message ?? "Failed to generate roadmap"
        : "Failed to generate roadmap";
      toast.error(message);
    },
  });
}
