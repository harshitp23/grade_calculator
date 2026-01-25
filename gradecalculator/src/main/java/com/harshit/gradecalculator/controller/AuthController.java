package com.harshit.gradecalculator.controller;

import com.harshit.gradecalculator.model.User;
import com.harshit.gradecalculator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest; 
import jakarta.servlet.ServletException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // POST /api/auth/register
    @PostMapping("/register")
    public String registerUser(@RequestParam String username, 
                               @RequestParam String email, 
                               @RequestParam String password,
                               HttpServletRequest request) { // 👈 1. Add Request
        
        // 1. Check if user exists
        if (userRepository.existsByEmail(email)) {
            return "Error: Email already in use!";
        }

        // 2. Create User
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        String hashedPwd = passwordEncoder.encode(password);
        newUser.setPasswordHash(hashedPwd);

        // 3. Save
        userRepository.save(newUser);

        // 4. 👇 AUTO-LOGIN MAGIC 👇
        try {
            request.login(username, password); // Logs the user in immediately
        } catch (ServletException e) {
            return "Success (But Auto-Login failed. Please login manually)";
        }

        return "Success";
    }

}
