package com.harshit.gradecalculator.controller;

import com.harshit.gradecalculator.model.Component;
import com.harshit.gradecalculator.model.Subject;
import com.harshit.gradecalculator.repository.ComponentRepository;
import com.harshit.gradecalculator.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/components")
public class ComponentController {

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    // 1. LIST Components for a Subject
    @GetMapping("/list")
    public List<Component> getComponents(@RequestParam Integer subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        return componentRepository.findBySubject(subject);
    }

    // 2. ADD a Component
    @PostMapping("/add")
    public Component addComponent(
            @RequestParam Integer subjectId,
            @RequestParam String name,
            @RequestParam Double weight,
            @RequestParam Double score,
            @RequestParam Double total) {

        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        
        Component c = new Component();
        c.setSubject(subject);
        c.setName(name);
        c.setWeight(weight);
        c.setScore(score);
        c.setTotalPoints(total);

        return componentRepository.save(c);
    }

    // 3. DELETE a Component
    @DeleteMapping("/delete")
    public String deleteComponent(@RequestParam Integer id) {
        componentRepository.deleteById(id);
        return "Deleted";
    }
}
