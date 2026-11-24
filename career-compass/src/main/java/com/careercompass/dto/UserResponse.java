package com.careercompass.dto;

import java.util.UUID;

public record UserResponse(UUID userId, String email, String username) {}