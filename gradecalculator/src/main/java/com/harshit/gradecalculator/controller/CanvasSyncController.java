package com.harshit.gradecalculator.controller;

import com.harshit.gradecalculator.dto.CanvasAssignmentPayload;
import com.harshit.gradecalculator.dto.CanvasCoursePayload;
import com.harshit.gradecalculator.dto.CanvasSyncRequest;
import com.harshit.gradecalculator.dto.CanvasSyncResponse;
import com.harshit.gradecalculator.model.Component;
import com.harshit.gradecalculator.model.Subject;
import com.harshit.gradecalculator.model.User;
import com.harshit.gradecalculator.repository.ComponentRepository;
import com.harshit.gradecalculator.repository.SubjectRepository;
import com.harshit.gradecalculator.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/canvas")
public class CanvasSyncController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ComponentRepository componentRepository;

    @PostMapping("/sync")
    @Transactional
    public ResponseEntity<CanvasSyncResponse> syncCanvasGrades(
            @RequestHeader(value = "X-Api-Token", required = false) String apiToken,
            @RequestBody CanvasSyncRequest request) {

        if (!StringUtils.hasText(apiToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByApiToken(apiToken).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<CanvasCoursePayload> courses = request.getCourses();
        if (courses == null || courses.isEmpty()) {
            return ResponseEntity.ok(new CanvasSyncResponse(0, 0));
        }

        int subjectsUpdated = 0;
        int componentsUpdated = 0;

        for (CanvasCoursePayload course : courses) {
            String courseCode = StringUtils.hasText(course.getCode()) ? course.getCode() : course.getName();
            if (!StringUtils.hasText(courseCode)) {
                continue;
            }

            Subject subject = subjectRepository.findByUserAndSubjectCode(user, courseCode)
                    .orElseGet(Subject::new);

            subject.setUser(user);
            subject.setSubjectName(StringUtils.hasText(course.getName()) ? course.getName() : courseCode);
            subject.setSubjectCode(courseCode);
            if (StringUtils.hasText(course.getStatus())) {
                subject.setStatus(course.getStatus());
            } else if (subject.getStatus() == null) {
                subject.setStatus("In Progress");
            }

            if (course.getCredits() != null) {
                subject.setCredits(course.getCredits());
            } else if (subject.getId() == null) {
                subject.setCredits(0);
            }

            if (course.getCurrentScore() != null) {
                subject.setCurrentScore(BigDecimal.valueOf(course.getCurrentScore()));
            }

            if (course.getIncludeInGpa() != null) {
                subject.setIncludeInGpa(course.getIncludeInGpa());
            } else if (subject.getId() == null) {
                subject.setIncludeInGpa(true);
            }

            Boolean useTotalPoints = course.getUseTotalPoints();
            if (useTotalPoints == null && course.getAssignments() != null && !course.getAssignments().isEmpty()) {
                useTotalPoints = course.getAssignments().stream()
                        .noneMatch(assignment -> assignment.getWeight() != null && assignment.getWeight() > 0);
            }
            if (useTotalPoints != null) {
                subject.setUseTotalPoints(useTotalPoints);
            }

            Subject savedSubject = subjectRepository.save(subject);
            subjectsUpdated += 1;

            if (course.getAssignments() != null) {
                componentRepository.deleteBySubject(savedSubject);
                for (CanvasAssignmentPayload assignment : course.getAssignments()) {
                    if (!StringUtils.hasText(assignment.getName())) {
                        continue;
                    }
                    Component component = new Component();
                    component.setSubject(savedSubject);
                    component.setName(assignment.getName());
                    component.setScore(assignment.getScore());
                    component.setTotalPoints(assignment.getTotalPoints());
                    component.setWeight(assignment.getWeight() != null ? assignment.getWeight() : 0.0);
                    componentRepository.save(component);
                    componentsUpdated += 1;
                }
            }
        }

        return ResponseEntity.ok(new CanvasSyncResponse(subjectsUpdated, componentsUpdated));
    }
}
