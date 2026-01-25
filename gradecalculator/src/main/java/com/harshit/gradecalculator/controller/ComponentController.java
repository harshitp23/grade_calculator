package com.harshit.gradecalculator.controller;

import com.harshit.gradecalculator.model.Component;
import com.harshit.gradecalculator.model.Subject;
import com.harshit.gradecalculator.repository.ComponentRepository;
import com.harshit.gradecalculator.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/components")
public class ComponentController {

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    // TEST URL: 
    // http://localhost:8080/api/components/add?subjectId=1&name=Midterm&weight=20&score=85&total=100
    @GetMapping("/add")
    public String addTestComponent(
            @RequestParam Integer subjectId,
            @RequestParam String name,
            @RequestParam Double weight,
            @RequestParam Double score,
            @RequestParam Double total
    ) {
        // 1. Find the Subject (e.g., Calculus)
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        
        if (subject == null) {
            return "Error: Subject with ID " + subjectId + " not found.";
        }

        // 2. Create the Component
        Component comp = new Component();
        comp.setComponentName(name);
        comp.setWeightage(BigDecimal.valueOf(weight));
        comp.setPointsScored(BigDecimal.valueOf(score));
        comp.setTotalPoints(BigDecimal.valueOf(total));
        comp.setSubject(subject); // Link it!

        // 3. Save it
        componentRepository.save(comp);

        return "Success! Added " + name + " to " + subject.getSubjectName();
    }
    
    // Check what components a subject has
    // http://localhost:8080/api/components/list?subjectId=1
    @GetMapping("/list")
    public List<Component> getComponents(@RequestParam Integer subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        if (subject == null) return null;
        return subject.getComponents();
    }
}