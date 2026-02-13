package com.harshit.gradecalculator.controller;

import com.harshit.gradecalculator.model.Subject;
import com.harshit.gradecalculator.model.User;
import com.harshit.gradecalculator.repository.SubjectRepository;
import com.harshit.gradecalculator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class TargetCalcController {

    @Autowired private UserRepository userRepository;
    @Autowired private SubjectRepository subjectRepository;

    @GetMapping("/target-calculator")
    public String showTargetPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        // 🔥 Filter: Only show subjects that are currently "In Progress"
        List<Subject> currentSubjects = subjectRepository.findByUserAndStatus(user, "In Progress");
        
        model.addAttribute("subjects", currentSubjects);
        return "target-calculator";
    }
}
