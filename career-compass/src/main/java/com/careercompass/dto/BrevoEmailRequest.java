package com.careercompass.dto;

import java.util.List;

public record BrevoEmailRequest(
        Sender sender,
        List<Recipient> to,
        String subject,
        String htmlContent,
        List<Attachment> attachment
) {
    public record Sender(String email, String name) {}
    public record Recipient(String email) {}
    public record Attachment(String content, String name) {}
}