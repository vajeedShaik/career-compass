package com.careercompass.service;

import com.careercompass.dto.BrevoEmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;

/**
 * Sends the roadmap report via Brevo's HTTPS transactional email API,
 * rather than raw SMTP. Render's free tier blocks all outbound SMTP ports
 * (25/465/587) as of September 2025 — this switch keeps email delivery
 * working on the free tier by using plain HTTPS instead, which isn't
 * blocked. Same Brevo account, different endpoint.
 */
@Service
public class EmailService {

    private final RestClient restClient;
    private final String fromAddress;

    public EmailService(@Value("${app.brevo.api-key}") String apiKey,
                        @Value("${app.mail.from}") String fromAddress) {
        this.fromAddress = fromAddress;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        BrevoEmailRequest request = new BrevoEmailRequest(
                new BrevoEmailRequest.Sender(fromAddress, "Career Compass"),
                List.of(new BrevoEmailRequest.Recipient(toEmail)),
                "Career Compass – Reset your password",
                """
                <p>Hi,</p>
                <p>We received a request to reset your Career Compass password. Click the link below to choose a new one:</p>
                <p><a href="%s">%s</a></p>
                <p>This link expires in 1 hour. If you didn't request this, you can safely ignore this email.</p>
                <p>Best,<br/>Career Compass</p>
                """.formatted(resetLink, resetLink),
                null
        );

        restClient.post()
                .uri("/smtp/email")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void sendReport(String toEmail, byte[] pdfBytes) {
        String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);

        BrevoEmailRequest request = new BrevoEmailRequest(
                new BrevoEmailRequest.Sender(fromAddress, "Career Compass"),
                List.of(new BrevoEmailRequest.Recipient(toEmail)),
                "Career Compass – Your Personalized Career Assessment Report",
                """
                <p>Hi,</p>
                <p>Your personalized AI career assessment report has been generated successfully and is attached to this email.</p>
                <p>It includes your resume analysis, job match score, skill gap breakdown, and a personalized preparation plan.</p>
                <p>Best,<br/>Career Compass</p>
                """,
                List.of(new BrevoEmailRequest.Attachment(base64Pdf, "Career_Compass_Report.pdf"))
        );


        restClient.post()
                .uri("/smtp/email")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}