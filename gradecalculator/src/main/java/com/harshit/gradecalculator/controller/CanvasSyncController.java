package com.harshit.gradecalculator.controller;

import com.harshit.gradecalculator.dto.*;
import com.harshit.gradecalculator.model.*;
import com.harshit.gradecalculator.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/canvas")
public class CanvasSyncController {

    @Autowired private UserRepository userRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private ComponentRepository componentRepository;

    @PostMapping("/sync")
    @Transactional
    public CanvasSyncResponse syncCanvasData(
            @RequestHeader("X-Api-Token") String apiToken,
            @RequestBody CanvasSyncRequest request) {

        User user = userRepository.findByApiToken(apiToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token"));

        int subjectsUpdated = 0;
        int componentsUpdated = 0;

        for (CanvasCoursePayload coursePayload : request.getCourses()) {
            
            // 1. Find or Create Subject
            Optional<Subject> existing = subjectRepository.findByUserAndSubjectCode(user, coursePayload.getCode());
            Subject subject;

            if (existing.isPresent()) {
                subject = existing.get();
                subject.setCurrentScore(BigDecimal.valueOf(coursePayload.getCurrentScore()));
            } else {
                subject = new Subject();
                subject.setUser(user);
                subject.setSubjectName(coursePayload.getName());
                subject.setSubjectCode(coursePayload.getCode());
                subject.setCredits(3);
                subject.setStatus("In Progress");
                subject.setCurrentScore(BigDecimal.valueOf(coursePayload.getCurrentScore()));
                subject.setIncludeInGpa(true);
                subjectRepository.save(subject);
            }
            subjectsUpdated++;

            // 2. Clear old components to perform a fresh sync
            componentRepository.deleteBySubject(subject);

            // 3. Add New Components (Groups) & Assignments
            if (coursePayload.getComponents() != null) {
                for (CanvasComponentPayload compPayload : coursePayload.getComponents()) {
                    
                    Component comp = new Component();
                    comp.setSubject(subject);
                    comp.setName(compPayload.getName());
                    comp.setWeight(compPayload.getWeight());
                    comp.setScore(compPayload.getScore());
                    comp.setTotalPoints(compPayload.getTotalPoints());
                    
                    // Save component first to get ID
                    comp = componentRepository.save(comp);
                    componentsUpdated++;

                    // Add assignments if present
                    if (compPayload.getAssignments() != null) {
                        for (CanvasAssignmentPayload assignPayload : compPayload.getAssignments()) {
                            Assignment assign = new Assignment();
                            assign.setComponent(comp); // Link to parent
                            assign.setName(assignPayload.getName());
                            assign.setScore(assignPayload.getScore());
                            assign.setTotalPoints(assignPayload.getTotalPoints());
                            
                            comp.getAssignments().add(assign);
                        }
                        // Save again to cascade assignments
                        componentRepository.save(comp);
                    }
                }
            }
        }
        return new CanvasSyncResponse(subjectsUpdated, componentsUpdated);
    }
}
