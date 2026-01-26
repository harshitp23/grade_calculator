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

    @PostMapping("/update-score")
    public String updateScore(@RequestParam Integer id, @RequestParam Double score) {
        Subject s = subjectRepository.findById(id).orElseThrow();
        s.setCurrentScore(java.math.BigDecimal.valueOf(score)); // Make sure this conversion is here
        subjectRepository.save(s);
        return "Score Updated!";
    }

    // 5. UPDATE Settings (Name, Code, Scale, AND STATUS)
    @PostMapping("/update-settings")
    public String updateSettings(
            @RequestParam Integer id,
            @RequestParam String name,
            @RequestParam String code,
            @RequestParam(required = false) String gradingScale,
            @RequestParam String status) { // 👈 New Field
            
        Subject s = subjectRepository.findById(id).orElseThrow();
        s.setSubjectName(name);
        s.setSubjectCode(code);
        s.setGradingScale(gradingScale);
        s.setStatus(status); // 👈 Save the new status
        subjectRepository.save(s);
        return "Settings Updated!";
    }

    // 6. UPDATE Curved Grade
    @PostMapping("/update-curve")
    public String updateCurve(@RequestParam Integer id, @RequestParam String letter) {
        Subject s = subjectRepository.findById(id).orElseThrow();
        s.setLetterGrade(letter); // Force this letter grade
        subjectRepository.save(s);
        return "Curve Updated!";
    }
}



