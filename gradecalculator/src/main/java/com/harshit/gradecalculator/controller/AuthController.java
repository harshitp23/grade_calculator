package com.harshit.gradecalculator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${brevo.sender.email:gradecalc.noreply@gmail.com}")
    private String senderEmail;

    @Value("${brevo.sender.name:GradeCalc}")
    private String senderName;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public void sendOtpEmail(String toEmail, String otp) throws Exception {
        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            throw new RuntimeException("Brevo API key is not configured. Set BREVO_API_KEY environment variable.");
        }

        String htmlContent = "<div style=\"font-family: 'Helvetica Neue', Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 40px 20px;\">"
            + "<div style=\"text-align: center; margin-bottom: 32px;\">"
            + "<div style=\"display: inline-block; background: #4f46e5; border-radius: 12px; padding: 12px 16px; margin-bottom: 16px;\">"
            + "<span style=\"color: white; font-size: 20px; font-weight: 800;\">GradeCalc</span>"
            + "</div>"
            + "<h1 style=\"margin: 0; font-size: 24px; color: #111827;\">Verify Your Email</h1>"
            + "<p style=\"color: #6b7280; font-size: 14px; margin-top: 8px;\">Enter this code to complete your registration:</p>"
            + "</div>"
            + "<div style=\"background: #f8fafc; border: 2px solid #e2e8f0; border-radius: 12px; padding: 24px; text-align: center; margin-bottom: 24px;\">"
            + "<div style=\"font-size: 36px; font-weight: 800; letter-spacing: 8px; color: #4f46e5; font-family: 'Courier New', monospace;\">"
            + otp
            + "</div>"
            + "</div>"
            + "<p style=\"color: #6b7280; font-size: 13px; text-align: center; margin-bottom: 8px;\">"
            + "This code expires in <strong style=\"color: #111827;\">10 minutes</strong>."
            + "</p>"
            + "<p style=\"color: #9ca3af; font-size: 12px; text-align: center;\">"
            + "If you didn't request this, you can safely ignore this email."
            + "</p>"
            + "<hr style=\"border: none; border-top: 1px solid #e5e7eb; margin: 32px 0 16px;\">"
            + "<p style=\"color: #9ca3af; font-size: 11px; text-align: center;\">"
            + "GradeCalc - Smart Grade Tracking for Students"
            + "</p>"
            + "</div>";

        // Build JSON manually to avoid escaping issues
        String jsonBody = "{"
            + "\"sender\":{\"name\":\"" + senderName + "\",\"email\":\"" + senderEmail + "\"},"
            + "\"to\":[{\"email\":\"" + toEmail + "\"}],"
            + "\"subject\":\"GradeCalc - Verify Your Email\","
            + "\"htmlContent\":\"" + htmlContent.replace("\"", "\\\"") + "\""
            + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("api-key", brevoApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(15))
                .build();

        log.info("Sending OTP email to {} via Brevo API...", toEmail);

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            log.info("Brevo API success: {}", response.body());
        } else {
            log.error("Brevo API failed with status {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("Brevo API error (status " + response.statusCode() + "): " + response.body());
        }
    }
}
