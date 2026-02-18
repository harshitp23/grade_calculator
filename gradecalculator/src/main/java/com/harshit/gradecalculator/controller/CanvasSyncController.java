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

        int subjectsUpdated = 0, componentsUpdated = 0;

        for (CanvasCoursePayload coursePayload : request.getCourses()) {
            Optional<Subject> existing = subjectRepository.findByUserAndSubjectCode(user, coursePayload.getCode());
            Subject subject;

            if (existing.isPresent()) {
                subject = existing.get();
                // Store previous score to show ↑/↓ on dashboard
                if (subject.getCurrentScore() != null) subject.setPreviousScore(subject.getCurrentScore());
                subject.setCurrentScore(BigDecimal.valueOf(coursePayload.getCurrentScore()));
            } else {
                subject = new Subject();
                subject.setUser(user);
                subject.setSubjectName(coursePayload.getName());
                subject.setSubjectCode(coursePayload.getCode());
                subject.setCredits(3);
                subject.setStatus("In Progress");
                subject.setTermSeason("Spring"); // Default for new syncs, user can edit later
                subject.setTermYear(2026);
                subject.setCurrentScore(BigDecimal.valueOf(coursePayload.getCurrentScore()));
                subject.setIncludeInGpa(true);
                subjectRepository.save(subject);
            }
            subjectsUpdated++;

            componentRepository.deleteBySubject(subject);

            if (coursePayload.getComponents() != null) {
                for (CanvasComponentPayload compPayload : coursePayload.getComponents()) {
                    Component comp = new Component();
                    comp.setSubject(subject);
                    comp.setName(compPayload.getName());
                    comp.setWeight(compPayload.getWeight());
                    comp.setScore(compPayload.getScore());
                    comp.setTotalPoints(compPayload.getTotalPoints());
                    comp.setDropLowest(compPayload.getDropLowest());
                    comp = componentRepository.save(comp);
                    componentsUpdated++;

                    if (compPayload.getAssignments() != null) {
                        for (CanvasAssignmentPayload assignPayload : compPayload.getAssignments()) {
                            Assignment assign = new Assignment();
                            assign.setComponent(comp);
                            assign.setName(assignPayload.getName());
                            assign.setScore(assignPayload.getScore());
                            assign.setTotalPoints(assignPayload.getTotalPoints());
                            comp.getAssignments().add(assign);
                        }
                        componentRepository.save(comp);
                    }
                }
            }
        }
        return new CanvasSyncResponse(subjectsUpdated, componentsUpdated);
    }
}
