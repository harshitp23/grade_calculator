package com.harshit.gradecalculator.controller;

import com.harshit.gradecalculator.model.User;
import com.harshit.gradecalculator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ===== PROFILE =====
    @GetMapping("/profile")
    public Map<String, String> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return Map.of(
            "username", user.getUsername(),
            "email", user.getEmail()
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestBody Map<String, String> body) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        String newUsername = body.get("username");
        String newEmail = body.get("email");

        if (newUsername != null && !newUsername.isBlank()) {
            // Check if username is taken by someone else
            var existing = userRepository.findByUsername(newUsername.trim());
            if (existing.isPresent() && !existing.get().getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Username is already taken."));
            }
            user.setUsername(newUsername.trim());
        }

        if (newEmail != null && !newEmail.isBlank()) {
            if (userRepository.existsByEmail(newEmail.trim())) {
                var existingByEmail = userRepository.findByUsername(user.getUsername());
                if (existingByEmail.isPresent() && !existingByEmail.get().getEmail().equals(newEmail.trim())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("error", "Email is already in use."));
                }
            }
            user.setEmail(newEmail.trim());
        }

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully!"));
    }

    // ===== PASSWORD =====
    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody Map<String, String> body) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Both fields are required."));
        }

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Current password is incorrect."));
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully!"));
    }

    // ===== GPA GOAL =====
    @GetMapping("/gpa-goal")
    public Map<String, Object> getGpaGoal(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        BigDecimal goal = user.getTargetGpa();
        return Map.of(
            "targetGpa", goal != null ? goal : BigDecimal.ZERO
        );
    }

    @PostMapping("/gpa-goal")
    public ResponseEntity<?> setGpaGoal(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestBody Map<String, Object> body) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        Object val = body.get("targetGpa");
        if (val == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "targetGpa is required."));
        }

        try {
            BigDecimal target = new BigDecimal(val.toString());
            if (target.compareTo(BigDecimal.ZERO) < 0 || target.compareTo(new BigDecimal("4.0")) > 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "GPA must be between 0.0 and 4.0"));
            }
            user.setTargetGpa(target);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "GPA goal updated!", "targetGpa", target));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid number format."));
        }
    }
}
