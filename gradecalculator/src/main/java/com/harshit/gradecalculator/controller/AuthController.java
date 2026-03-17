package com.harshit.gradecalculator.controller;

import com.harshit.gradecalculator.model.User;
import com.harshit.gradecalculator.repository.UserRepository;
import com.harshit.gradecalculator.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // In-memory OTP store: email -> { otp, expiresAt }
    // For production at scale, use Redis or a database table instead
    private static final ConcurrentHashMap<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    // ===== SEND OTP =====
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");

        if (username == null || email == null || username.isBlank() || email.isBlank()) {
            return ResponseEntity.badRequest().body("Username and email are required.");
        }

        // Check if email already registered
        if (userRepository.existsByEmail(email.trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Email already in use!");
        }

        // Check if username already taken
        if (userRepository.findByUsername(username.trim()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Username is already taken! Please choose another.");
        }

        // Rate limit: don't send a new OTP if one was sent less than 30 seconds ago
        OtpEntry existing = otpStore.get(email.trim().toLowerCase());
        if (existing != null && existing.createdAt.plusSeconds(30).isAfter(Instant.now())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Please wait before requesting a new code.");
        }

        // Generate 6-digit OTP
        String otp = String.valueOf(100000 + (int)(Math.random() * 900000));

        // Store OTP with 10-minute expiry
        otpStore.put(email.trim().toLowerCase(), new OtpEntry(otp, Instant.now().plusSeconds(600), Instant.now()));

        // Send email
        try {
            log.info("Attempting to send OTP to: {}", email.trim());
            emailService.sendOtpEmail(email.trim(), otp);
            log.info("OTP email sent successfully to: {}", email.trim());
        } catch (Exception e) {
            log.error("=== OTP EMAIL FAILED ===", e);
            log.error("Error type: {} | Message: {}", e.getClass().getName(), e.getMessage());
            if (e.getCause() != null) {
                log.error("Cause: {} | {}", e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            otpStore.remove(email.trim().toLowerCase());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send verification email. Please try again.");
        }

        return ResponseEntity.ok("OTP sent successfully.");
    }

    // ===== VERIFY OTP & REGISTER =====
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtpAndRegister(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");
        String otp = body.get("otp");

        if (username == null || email == null || password == null || otp == null) {
            return ResponseEntity.badRequest().body("All fields are required.");
        }

        String emailKey = email.trim().toLowerCase();

        // Look up OTP
        OtpEntry entry = otpStore.get(emailKey);
        if (entry == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No verification code found. Please request a new one.");
        }

        // Check expiry
        if (Instant.now().isAfter(entry.expiresAt)) {
            otpStore.remove(emailKey);
            return ResponseEntity.status(HttpStatus.GONE).body("Verification code expired. Please request a new one.");
        }

        // Check OTP value
        if (!entry.otp.equals(otp.trim())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid verification code. Please try again.");
        }

        // OTP valid — remove it so it can't be reused
        otpStore.remove(emailKey);

        // Double-check uniqueness (race condition protection)
        if (userRepository.existsByEmail(email.trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Email already in use!");
        }
        if (userRepository.findByUsername(username.trim()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Username is already taken!");
        }

        // Create the user
        User newUser = new User();
        newUser.setUsername(username.trim());
        newUser.setEmail(email.trim());
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setApiToken(UUID.randomUUID().toString());

        userRepository.save(newUser);

        // Auto-login
        try {
            request.login(username.trim(), password);
            return ResponseEntity.ok("Success");
        } catch (ServletException e) {
            return ResponseEntity.ok("Success-NoLogin");
        }
    }

    // ===== LEGACY REGISTER (keep for backward compatibility, can remove later) =====
    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               HttpServletRequest request) {
        if (userRepository.existsByEmail(email)) {
            return "Error: Email already in use!";
        }
        if (userRepository.findByUsername(username).isPresent()) {
            return "Error: Username is already taken! Please choose another.";
        }

        User newUser = new User();
        newUser.setUsername(username.trim());
        newUser.setEmail(email.trim());
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setApiToken(UUID.randomUUID().toString());

        userRepository.save(newUser);

        try {
            request.login(username, password);
        } catch (ServletException e) {
            return "Success-NoLogin";
        }
        return "Success";
    }

    // ===== FORGOT PASSWORD: SEND OTP =====
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }

        String emailKey = email.trim().toLowerCase();

        // Check if email exists
        if (!userRepository.existsByEmail(email.trim())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No account found with this email address.");
        }

        // Rate limit
        OtpEntry existing = otpStore.get("reset:" + emailKey);
        if (existing != null && existing.createdAt.plusSeconds(30).isAfter(Instant.now())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Please wait before requesting a new code.");
        }

        // Generate OTP
        String otp = String.valueOf(100000 + (int)(Math.random() * 900000));
        otpStore.put("reset:" + emailKey, new OtpEntry(otp, Instant.now().plusSeconds(600), Instant.now()));

        try {
            log.info("Sending password reset OTP to: {}", email.trim());
            emailService.sendOtpEmail(email.trim(), otp);
            log.info("Password reset OTP sent successfully to: {}", email.trim());
        } catch (Exception e) {
            log.error("Failed to send password reset OTP", e);
            otpStore.remove("reset:" + emailKey);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send verification email. Please try again.");
        }

        return ResponseEntity.ok("Code sent successfully.");
    }

    // ===== FORGOT PASSWORD: VERIFY OTP ONLY =====
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<String> verifyResetOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");

        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body("Email and code are required.");
        }

        String emailKey = "reset:" + email.trim().toLowerCase();
        OtpEntry entry = otpStore.get(emailKey);

        if (entry == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No verification code found. Please request a new one.");
        }
        if (Instant.now().isAfter(entry.expiresAt)) {
            otpStore.remove(emailKey);
            return ResponseEntity.status(HttpStatus.GONE).body("Code expired. Please request a new one.");
        }
        if (!entry.otp.equals(otp.trim())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid code. Please try again.");
        }

        // OTP is valid — DON'T remove it yet (we need it for the reset step)
        return ResponseEntity.ok("Code verified.");
    }

    // ===== FORGOT PASSWORD: RESET PASSWORD =====
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        String newPassword = body.get("newPassword");

        if (email == null || otp == null || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body("All fields are required.");
        }

        String emailKey = "reset:" + email.trim().toLowerCase();
        OtpEntry entry = otpStore.get(emailKey);

        if (entry == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No verification code found. Please start over.");
        }
        if (Instant.now().isAfter(entry.expiresAt)) {
            otpStore.remove(emailKey);
            return ResponseEntity.status(HttpStatus.GONE).body("Code expired. Please start over.");
        }
        if (!entry.otp.equals(otp.trim())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid code.");
        }

        // OTP valid — remove it
        otpStore.remove(emailKey);

        // Find user and update password
        var userOpt = userRepository.findByEmail(email.trim());
        if (userOpt == null || userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No account found with this email.");
        }

        User user = userOpt.get();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password reset successfully for: {}", email.trim());
        return ResponseEntity.ok("Password reset successfully.");
    }

    // ===== OTP ENTRY HELPER CLASS =====
    private static class OtpEntry {
        final String otp;
        final Instant expiresAt;
        final Instant createdAt;

        OtpEntry(String otp, Instant expiresAt, Instant createdAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
            this.createdAt = createdAt;
        }
    }
}
