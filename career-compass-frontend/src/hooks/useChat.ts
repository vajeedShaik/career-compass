import { useMutation } from "@tanstack/react-query";
import { toast } from "sonner";
import { isAxiosError } from "axios";
import { sendChatMessage } from "@/api/chat";

export function useSendChatMessage() {
  return useMutation({
    mutationFn: ({ roadmapId, message }: { roadmapId: string; message: string }) =>
      sendChatMessage(roadmapId, message),
    onError: (error) => {
      const message = isAxiosError(error)
        ? error.response?.data?.message ?? "Failed to send message"
        : "Failed to send message";
      toast.error(message);
    },
  });
}
