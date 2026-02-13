package com.harshit.gradecalculator.controller;

import com.harshit.gradecalculator.model.Component;
import com.harshit.gradecalculator.model.Subject;
import com.harshit.gradecalculator.model.User; // Import User
import com.harshit.gradecalculator.repository.ComponentRepository;
import com.harshit.gradecalculator.repository.SubjectRepository;
import com.harshit.gradecalculator.repository.UserRepository; // 👈 THIS WAS MISSING
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/components")
public class ComponentController {

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. LIST Components for a Subject
    @GetMapping("/list")
    public List<Component> getComponents(@RequestParam Integer subjectId,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        Subject subject = loadOwnedSubject(subjectId, userDetails);
        return componentRepository.findBySubject(subject);
    }


    // 2. ADD a Component
    @PostMapping("/add")
    public Component addComponent(
            @RequestParam Integer subjectId,
            @RequestParam String name,
            @RequestParam Double weight,
            @RequestParam Double score,
            @RequestParam Double total,
            @AuthenticationPrincipal UserDetails userDetails) {

        Subject subject = loadOwnedSubject(subjectId, userDetails);

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
    public String deleteComponent(@RequestParam Integer id,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        Component c = componentRepository.findById(id).orElseThrow();
        Subject subject = c.getSubject();
        loadOwnedSubject(subject.getId(), userDetails); // will throw if not allowed
        componentRepository.deleteById(id);
        return "Deleted";
    }

     private Subject loadOwnedSubject(Integer subjectId, UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        if (subject.getUser() == null || !subject.getUser().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }
        return subject;
    }

}
