package com.harshit.gradecalculator.controller;

import com.harshit.gradecalculator.model.Component;
import com.harshit.gradecalculator.model.Subject;
import com.harshit.gradecalculator.model.User;
import com.harshit.gradecalculator.repository.ComponentRepository;
import com.harshit.gradecalculator.repository.SubjectRepository;
import com.harshit.gradecalculator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Controller
public class PageController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ComponentRepository componentRepository;

    @GetMapping({"/", "/index.html"})
    public String dashboard() {
        return "index";
    }

    @GetMapping({"/login", "/login.html"})
    public String login() {
        return "login";
    }

    @GetMapping({"/register", "/register.html"})
    public String register() {
        return "register";
    }

    @GetMapping({"/add-subject", "/add-subject.html"})
    public String addSubject() {
        return "add-subject";
    }


    @Transactional(readOnly = true)
    @GetMapping({"/subject-details", "/subject-details.html"})
    public String subjectDetails(
            @RequestParam("id") Integer id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        // Ownership check (critical)
        if (subject.getUser() == null || subject.getUser().getUserId() == null
                || !subject.getUser().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }


        model.addAttribute("subject", subject);

        return "subject-details";
    }

}
