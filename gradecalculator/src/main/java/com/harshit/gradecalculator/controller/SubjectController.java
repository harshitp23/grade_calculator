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

    // 1. GET List of Subjects
    @GetMapping("/list")
    public List<Subject> getSubjects(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return subjectRepository.findByUser(user);
    }

    // 2. ADD a Subject (Updated to POST + New Fields)
    @PostMapping("/add") 
    public Subject addSubject(
            @RequestParam String name, 
            @RequestParam String code,
            @RequestParam int credits,   // 👈 New Field
            @RequestParam String status, // 👈 New Field
            @AuthenticationPrincipal UserDetails userDetails) {
            
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        Subject s = new Subject();
        s.setSubjectName(name); // e.g. "PHYSICS"
        s.setSubjectCode(code); // e.g. "101"
        s.setCredits(credits);
        s.setStatus(status);
        s.setUser(user);

        // We return the SAVED object. This allows the frontend to see the new ID!
        return subjectRepository.save(s);
    }

    // 3. DELETE a Subject
    @DeleteMapping("/delete")
    public String deleteSubject(@RequestParam Integer id) {
        subjectRepository.deleteById(id);
        return "Subject Deleted!";
    }

    // 4. UPDATE Subject Score
    @PostMapping("/update-score")
    public String updateScore(@RequestParam Integer id, @RequestParam Double score) {
        Subject s = subjectRepository.findById(id).orElseThrow();
        s.setCurrentScore(score);
        subjectRepository.save(s);
        return "Score Updated!";
    }
}

