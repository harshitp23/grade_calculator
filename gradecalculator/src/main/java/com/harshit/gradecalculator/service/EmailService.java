package com.harshit.gradecalculator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("GradeCalc - Verify Your Email");

        String html = """
            <div style="font-family: 'Helvetica Neue', Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 40px 20px;">
                <div style="text-align: center; margin-bottom: 32px;">
                    <div style="display: inline-block; background: #4f46e5; border-radius: 12px; padding: 12px 16px; margin-bottom: 16px;">
                        <span style="color: white; font-size: 20px; font-weight: 800;">🎓 GradeCalc</span>
                    </div>
                    <h1 style="margin: 0; font-size: 24px; color: #111827;">Verify Your Email</h1>
                    <p style="color: #6b7280; font-size: 14px; margin-top: 8px;">Enter this code to complete your registration:</p>
                </div>
                
                <div style="background: #f8fafc; border: 2px solid #e2e8f0; border-radius: 12px; padding: 24px; text-align: center; margin-bottom: 24px;">
                    <div style="font-size: 36px; font-weight: 800; letter-spacing: 8px; color: #4f46e5; font-family: 'Courier New', monospace;">
                        %s
                    </div>
                </div>
                
                <p style="color: #6b7280; font-size: 13px; text-align: center; margin-bottom: 8px;">
                    This code expires in <strong style="color: #111827;">10 minutes</strong>.
                </p>
                <p style="color: #9ca3af; font-size: 12px; text-align: center;">
                    If you didn't request this, you can safely ignore this email.
                </p>
                
                <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 32px 0 16px;">
                <p style="color: #9ca3af; font-size: 11px; text-align: center;">
                    GradeCalc — Smart Grade Tracking for Students
                </p>
            </div>
            """.formatted(otp);

        helper.setText(html, true);
        mailSender.send(message);
    }
}
