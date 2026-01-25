package com.harshit.gradecalculator.controller;

import com.harshit.gradecalculator.model.Subject;
import com.harshit.gradecalculator.repository.SubjectRepository;
import com.harshit.gradecalculator.model.User;
import com.harshit.gradecalculator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private UserRepository userRepository;

    // 1. GET List of Subjects for the Logged-in User
    @GetMapping("/list")
    public List<Subject> getSubjects(@AuthenticationPrincipal UserDetails userDetails) {
        // Find the user in the DB using the logged-in email/username
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return subjectRepository.findByUser(user);
    }

    // 2. ADD a Subject (Matches your HTML's GET request pattern)
    @GetMapping("/add") 
    public String addSubject(
            @RequestParam String name, 
            @RequestParam String code,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        Subject s = new Subject();
        s.setSubjectName(name);
        s.setSubjectCode(code);
        s.setCredits(3); // Default to 3, or add an input for this!
        s.setStatus("In Progress");
        s.setUser(user); // Link subject to the logged-in user!

        subjectRepository.save(s);
        return "Subject Added Successfully!";
    }

    // 3. DELETE a Subject
    @DeleteMapping("/delete")
    public String deleteSubject(@RequestParam Long id) {
        subjectRepository.deleteById(id);
        return "Subject Deleted!";
    }
}
