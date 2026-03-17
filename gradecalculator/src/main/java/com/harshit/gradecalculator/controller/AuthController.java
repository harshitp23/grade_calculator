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

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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
            emailService.sendOtpEmail(email.trim(), otp);
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
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
