package com.careercompass.dto;

import java.util.List;
import java.util.UUID;

public record ChatResponse(
        UUID roadmapId,
        List<ChatMessageDto> messages
) {}
