package com.careercompass.service;

import com.careercompass.dto.ChatMessageDto;
import com.careercompass.dto.ChatResponse;
import com.careercompass.entity.ChatMessage;
import com.careercompass.entity.Roadmap;
import com.careercompass.repository.ChatMessageRepository;
import com.careercompass.repository.RoadmapRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles follow-up conversation about a generated roadmap. The roadmap
 * content is injected as system context on every turn, and prior messages
 * are replayed as real conversation history — not just concatenated text —
 * so the AI has proper multi-turn context, not a one-shot Q&A.
 */
@Service
public class ChatService {

    private final RoadmapRepository roadmapRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatClient chatClient;

    public ChatService(RoadmapRepository roadmapRepository,
                        ChatMessageRepository chatMessageRepository,
                        ChatClient.Builder chatClientBuilder) {
        this.roadmapRepository = roadmapRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatClient = chatClientBuilder.build();
    }

    private static final String SYSTEM_TEMPLATE = """
            You are a helpful career preparation assistant. The user has already been given the following
            personalized learning roadmap. Answer their follow-up questions about it clearly and concisely.
            Stay grounded in the roadmap content below — if asked about something outside its scope, say so
            honestly rather than inventing information.

            ROADMAP:
            %s
            """;

    @Transactional
    public ChatResponse sendMessage(UUID roadmapId, String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new IllegalArgumentException("Roadmap not found: " + roadmapId));

        List<ChatMessage> history = chatMessageRepository.findByRoadmapIdOrderByCreatedAtAsc(roadmapId);

        // Save the user's message first
        ChatMessage userMsg = new ChatMessage();
        userMsg.setRoadmapId(roadmapId);
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        chatMessageRepository.save(userMsg);

        // Replay prior turns as real conversation messages, not flattened text
        List<Message> conversation = new ArrayList<>();
        for (ChatMessage m : history) {
            if ("user".equals(m.getRole())) {
                conversation.add(new UserMessage(m.getContent()));
            } else {
                conversation.add(new AssistantMessage(m.getContent()));
            }
        }

        String systemPrompt = SYSTEM_TEMPLATE.formatted(roadmap.getContent());

        String reply = chatClient.prompt()
                .system(systemPrompt)
                .messages(conversation)
                .user(userMessage)
                .call()
                .content();

        if (reply == null || reply.isBlank()) {
            throw new IllegalStateException("AI chat response was empty");
        }

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setRoadmapId(roadmapId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(reply);
        chatMessageRepository.save(assistantMsg);

        List<ChatMessage> fullHistory = chatMessageRepository.findByRoadmapIdOrderByCreatedAtAsc(roadmapId);
        List<ChatMessageDto> messageDtos = fullHistory.stream()
                .map(m -> new ChatMessageDto(m.getRole(), m.getContent(), m.getCreatedAt()))
                .toList();

        return new ChatResponse(roadmapId, messageDtos);
    }
}
