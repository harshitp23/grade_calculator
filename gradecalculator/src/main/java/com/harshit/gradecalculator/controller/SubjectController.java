package com.harshit.gradecalculator.controller;

import com.harshit.gradecalculator.model.Subject;
import com.harshit.gradecalculator.repository.SubjectRepository;
import com.harshit.gradecalculator.model.User;
import com.harshit.gradecalculator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal; // Import this!
import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/list")
    public List<Subject> getSubjects(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return subjectRepository.findByUser(user);
    }

    @PostMapping("/add") 
    public Subject addSubject(
            @RequestParam String name, 
            @RequestParam String code,
            @RequestParam int credits,
            @RequestParam String status,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        Subject s = new Subject();
        s.setSubjectName(name);
        s.setSubjectCode(code);
        s.setCredits(credits);
        s.setStatus(status);
        s.setUser(user);

        return subjectRepository.save(s);
    }

    @DeleteMapping("/delete")
    public String deleteSubject(@RequestParam Integer id) {
        subjectRepository.deleteById(id);
        return "Subject Deleted!";
    }

    // 👇 FIXED THIS METHOD 👇
    @PostMapping("/update-score")
    public String updateScore(@RequestParam Integer id, @RequestParam Double score) {
        Subject s = subjectRepository.findById(id).orElseThrow();
        // Convert Double to BigDecimal
        s.setCurrentScore(BigDecimal.valueOf(score)); 
        subjectRepository.save(s);
        return "Score Updated!";
    }
}
