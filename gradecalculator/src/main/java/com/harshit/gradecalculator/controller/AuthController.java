package com.harshit.gradecalculator.controller;

import com.harshit.gradecalculator.model.User;
import com.harshit.gradecalculator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
                               @RequestParam String password) {
        
        // 1. Check if user already exists
        if (userRepository.existsByEmail(email)) {
            return "Error: Email already in use!";
        }

        // 2. Create User
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        
        // 3. HASH the password (Security Best Practice)
        String hashedPwd = passwordEncoder.encode(password);
        newUser.setPasswordHash(hashedPwd);

        // 4. Save
        userRepository.save(newUser);

        return "Success";
    }
}