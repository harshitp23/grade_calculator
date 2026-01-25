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

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, 
                               @RequestParam String email, 
                               @RequestParam String password,
                               HttpServletRequest request) {
        
        // 1. Check if EMAIL exists
        if (userRepository.existsByEmail(email)) {
            return "Error: Email already in use!";
        }

        // 👇 2. ADD THIS CHECK: Check if USERNAME exists
        if (userRepository.findByUsername(username).isPresent()) {
            return "Error: Username is already taken! Please choose another.";
        }

        // 3. Create User
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        String hashedPwd = passwordEncoder.encode(password);
        newUser.setPasswordHash(hashedPwd);

        // 4. Save
        userRepository.save(newUser);

        // 5. Auto-Login
        try {
            request.login(username, password);
        } catch (ServletException e) {
            return "Success (But Auto-Login failed. Please login manually)";
        }

        return "Success";
    }

}

