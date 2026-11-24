package com.careercompass.dto;

public record ResetPasswordRequest(String token, String newPassword) {}