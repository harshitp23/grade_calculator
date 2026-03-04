package com.harshit.gradecalculator.controller;

import com.harshit.gradecalculator.model.User;
import com.harshit.gradecalculator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.ServletException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/token")
    public Map<String, String> getApiToken(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        if (user.getApiToken() == null || user.getApiToken().isBlank()) {
            user.setApiToken(UUID.randomUUID().toString());
            userRepository.save(user);
        }
        return Map.of("token", user.getApiToken());
    }

    @PostMapping("/token/reset")
    public Map<String, String> resetApiToken(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        user.setApiToken(UUID.randomUUID().toString());
        userRepository.save(user);
        return Map.of("token", user.getApiToken());
    }

    @DeleteMapping("/delete")
    public Map<String, String> deleteAccount(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        userRepository.delete(user);
        try {
            request.logout();
        } catch (ServletException e) {
            // User is deleted, logout failure is non-critical
        }
        return Map.of("message", "Account deleted successfully");
    }
}
