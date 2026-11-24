package com.careercompass.controller;

import com.careercompass.dto.ChatRequest;
import com.careercompass.dto.ChatResponse;
import com.careercompass.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/roadmaps/{roadmapId}/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Sends a follow-up question about a previously generated roadmap and
     * returns the full, updated conversation history.
     */
    @PostMapping
    public ResponseEntity<ChatResponse> sendMessage(
            @PathVariable UUID roadmapId,
            @RequestBody ChatRequest request
    ) {
        ChatResponse response = chatService.sendMessage(roadmapId, request.message());
        return ResponseEntity.ok(response);
    }
}
