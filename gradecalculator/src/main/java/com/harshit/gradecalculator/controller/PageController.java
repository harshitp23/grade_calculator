package com.harshit.gradecalculator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    // 1. Dashboard (Handle both "/" and "/index.html")
    @GetMapping({"/", "/index.html"})
    public String dashboard() {
        return "index"; // Looks for index.html in templates
    }

    // 2. Login Page
    @GetMapping("/login.html")
    public String login() {
        return "login";
    }

    // 3. Register Page
    @GetMapping("/register.html")
    public String register() {
        return "register";
    }

    // 4. 👇 THIS IS THE MISSING LINK 👇
    @GetMapping("/add-subject.html")
    public String addSubject() {
        return "add-subject"; // Looks for add-subject.html in templates
    }

    // 5. Subject Details (We will build this next!)
    @GetMapping("/subject-details.html")
    public String subjectDetails() {
        return "subject-details";
    }

    // 5. 👇 THIS IS THE NEW PART 👇
    @GetMapping("/subject-details.html")
    public String subjectDetails() {
        return "subject-details";
    }
}
