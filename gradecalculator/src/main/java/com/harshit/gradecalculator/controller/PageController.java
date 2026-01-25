package com.harshit.gradecalculator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/", "/index.html"})
    public String dashboard() {
        return "index";
    }

    @GetMapping("/login.html")
    public String login() {
        return "login";
    }

    @GetMapping("/register.html")
    public String register() {
        return "register";
    }

    @GetMapping("/add-subject.html")
    public String addSubject() {
        return "add-subject";
    }

    @GetMapping("/subject-details.html")
    public String subjectDetails() {
        return "subject-details";
    }
}
