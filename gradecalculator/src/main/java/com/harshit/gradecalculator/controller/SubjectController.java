package com.harshit.gradecalculator.controller;
import java.security.Principal;

import com.harshit.gradecalculator.model.Subject;
import com.harshit.gradecalculator.model.User;
import com.harshit.gradecalculator.repository.SubjectRepository;
import com.harshit.gradecalculator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Tells Spring: "This class handles web URLs"
@RequestMapping("/api/subjects") // All URLs here start with /api/subjects
public class SubjectController {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    // TEST URL: http://localhost:8080/api/subjects/add?name=Maths&code=MATH101
    @GetMapping("/add")
    // Add "Principal principal" to the arguments. This holds the logged-in user info.
    public String addTestSubject(@RequestParam String name, @RequestParam String code, Principal principal) {
        
        // 1. Get the username of the logged-in person
        String username = principal.getName();
        
        // 2. Find their User object in the DB
        User user = userRepository.findByUsername(username).orElse(null);
        if(user == null) return "Error: User not found.";

        // 3. Create Subject linked to THIS user (not just ID 1)
        Subject subject = new Subject();
        subject.setSubjectName(name);
        subject.setSubjectCode(code);
        subject.setCredits(3);
        subject.setUser(user); // Link to logged-in user!
        
        subjectRepository.save(subject);
        return "Success! Saved subject: " + name;
    }

    // TEST URL: http://localhost:8080/api/subjects/list
    @GetMapping("/list")
    public List<Subject> getAllSubjects(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if(user == null) return null;

        // Fetch subjects only for THIS user
        return subjectRepository.findByUser_UserId(user.getUserId());
    }
    
    @DeleteMapping("/delete")
    public String deleteSubject(@RequestParam Integer id) {
        // Check if it exists first
        if (!subjectRepository.existsById(id)) {
            return "Error: Subject not found!";
        }
        
        // Delete it (The database will auto-delete linked components too!)
        subjectRepository.deleteById(id);
        return "Subject deleted successfully.";
    }
    
    @Autowired
    private com.harshit.gradecalculator.service.GradeService gradeService;

    // URL: /api/subjects/calculate?id=1&target=93
    @GetMapping("/calculate")
    public String calculateGrade(@RequestParam Integer id, @RequestParam Double target) {
        return gradeService.calculateTarget(id, target);
    }
}