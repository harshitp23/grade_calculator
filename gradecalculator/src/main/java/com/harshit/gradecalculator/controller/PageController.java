package com.harshit.gradecalculator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "index"; // Serves index.html
    }

    @GetMapping("/index.html")
    public String index() {
        return "index"; // Serves index.html
    }

    @GetMapping("/login.html")
    public String login() {
        return "login"; // Serves login.html
    }

    @GetMapping("/register.html")
    public String register() {
        return "register"; // Serves register.html
    }
    
    // If you have this file, keep this line. If not, delete it.
    @GetMapping("/subject-details.html")
    public String subjectDetails() {
        return "subject-details"; 
    }
}
