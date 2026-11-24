import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Send, Loader2, MessageCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useSendChatMessage } from "@/hooks/useChat";
import type { ChatMessageDto } from "@/types/roadmap";
import { cn } from "@/utils/cn";

interface ChatPanelProps {
  roadmapId: string;
}

export function ChatPanel({ roadmapId }: ChatPanelProps) {
  const [messages, setMessages] = useState<ChatMessageDto[]>([]);
  const [input, setInput] = useState("");
  const sendMessage = useSendChatMessage();

  function handleSend() {
    const trimmed = input.trim();
    if (!trimmed || sendMessage.isPending) return;

    setInput("");
    sendMessage.mutate(
      { roadmapId, message: trimmed },
      {
        onSuccess: (data) => setMessages(data.messages),
      }
    );
  }

  return (
    <div className="flex flex-col rounded-lg border border-border bg-card">
      <div className="flex items-center gap-2 border-b border-border px-4 py-3">
        <MessageCircle className="h-4 w-4 text-primary" />
        <p className="text-sm font-medium">Ask about your roadmap</p>
      </div>

      <div className="flex max-h-80 min-h-[120px] flex-col gap-3 overflow-y-auto p-4">
        {messages.length === 0 && (
          <p className="text-center text-xs text-muted-foreground">
            Ask a follow-up question — e.g. "Which of these should I focus on first?"
          </p>
        )}
        <AnimatePresence initial={false}>
          {messages.map((msg, i) => (
            <motion.div
              key={i}
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              className={cn(
                "max-w-[85%] rounded-lg px-3 py-2 text-sm",
                msg.role === "user"
                  ? "ml-auto bg-primary text-primary-foreground"
                  : "mr-auto bg-muted text-foreground"
              )}
            >
              {msg.content}
            </motion.div>
          ))}
        </AnimatePresence>
        {sendMessage.isPending && (
          <div className="mr-auto flex items-center gap-2 rounded-lg bg-muted px-3 py-2 text-sm text-muted-foreground">
            <Loader2 className="h-3.5 w-3.5 animate-spin" />
            Thinking...
          </div>
        )}
      </div>

      <div className="flex items-center gap-2 border-t border-border p-3">
        <Input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleSend()}
          placeholder="Type your question..."
          disabled={sendMessage.isPending}
        />
        <Button
          size="icon"
          onClick={handleSend}
          disabled={sendMessage.isPending || !input.trim()}
        >
          <Send className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
}
