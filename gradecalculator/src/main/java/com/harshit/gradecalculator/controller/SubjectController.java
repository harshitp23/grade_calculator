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

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import com.harshit.gradecalculator.dto.SubjectSaveRequest;
import com.harshit.gradecalculator.dto.ComponentSaveRequest;
import com.harshit.gradecalculator.model.Component;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;


@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComponentRepository componentRepository;


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
        s.setIncludeInGpa(true);
        s.setUser(user);

        return subjectRepository.save(s);
    }

    @DeleteMapping("/delete")
    public String deleteSubject(@RequestParam Integer id, @AuthenticationPrincipal UserDetails userDetails) {
        Subject s = loadOwnedSubject(id, userDetails);
        subjectRepository.delete(s);
        return "Subject Deleted!";
    }


    @PostMapping("/update-score")
    public String updateScore(@RequestParam Integer id, @RequestParam Double score,
                          @AuthenticationPrincipal UserDetails userDetails) {
        Subject s = loadOwnedSubject(id, userDetails);
        s.setCurrentScore(java.math.BigDecimal.valueOf(score));
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
            @RequestParam String status,
            @RequestParam(required = false) Boolean includeInGpa,
            @AuthenticationPrincipal UserDetails userDetails) {

        Subject s = loadOwnedSubject(id, userDetails);
        s.setSubjectName(name);
        s.setSubjectCode(code);
        s.setGradingScale(gradingScale);
        s.setStatus(status);
        if (includeInGpa != null) s.setIncludeInGpa(includeInGpa);
        subjectRepository.save(s);
        return "Settings Updated!";
    }


    // 6. UPDATE Curved Grade
    @PostMapping("/update-curve")
    public String updateCurve(@RequestParam Integer id, @RequestParam String letter,
                          @AuthenticationPrincipal UserDetails userDetails) {
        Subject s = loadOwnedSubject(id, userDetails);
        s.setLetterGrade(letter);
        subjectRepository.save(s);
        return "Curve Updated!";
    }


    @PostMapping("/save")
    @Transactional
    public ResponseEntity<?> saveSubjectAndComponents(
            @RequestBody SubjectSaveRequest payload,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (payload == null || payload.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing subject id");
        }

    User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    Subject subject = subjectRepository.findById(payload.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

    // Ownership check (critical)
    if (subject.getUser() == null || subject.getUser().getUserId() == null
            || !subject.getUser().getUserId().equals(user.getUserId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
    }

    // Update allowed fields (keep tight)
    if (payload.getUseTotalPoints() != null) {
        subject.setUseTotalPoints(payload.getUseTotalPoints());
    }

    // Replace components
    componentRepository.deleteBySubject(subject);

    if (payload.getComponents() != null) {
        for (ComponentSaveRequest cReq : payload.getComponents()) {
            if (cReq == null || !StringUtils.hasText(cReq.getName())) continue;

            Component c = new Component();
            c.setSubject(subject);
            c.setName(cReq.getName().trim());
            c.setWeight(cReq.getWeight() != null ? cReq.getWeight() : 0.0);
            c.setScore(cReq.getScore() != null ? cReq.getScore() : 0.0);
            c.setTotalPoints(cReq.getTotalPoints() != null ? cReq.getTotalPoints() : 0.0);

            componentRepository.save(c);
        }
    }

    // Recompute currentScore server-side
    BigDecimal computed = computeSubjectPercent(subject);
    subject.setCurrentScore(computed);

    subjectRepository.save(subject);

    return ResponseEntity.ok().build();
    }


    @PostMapping("/save")
    @Transactional
    public ResponseEntity<?> saveSubjectAndComponents(
            @RequestBody SubjectSaveRequest payload,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (payload == null || payload.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing subject id");
        }

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Subject subject = subjectRepository.findById(payload.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        // Ownership check (critical)
        if (subject.getUser() == null || subject.getUser().getUserId() == null
                || !subject.getUser().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }

        // Update allowed fields (keep tight)
        if (payload.getUseTotalPoints() != null) {
            subject.setUseTotalPoints(payload.getUseTotalPoints());
        }

        // Replace components
        componentRepository.deleteBySubject(subject);

        if (payload.getComponents() != null) {
            for (ComponentSaveRequest cReq : payload.getComponents()) {
                if (cReq == null || !StringUtils.hasText(cReq.getName())) continue;

                Component c = new Component();
                c.setSubject(subject);
                c.setName(cReq.getName().trim());
                c.setWeight(cReq.getWeight() != null ? cReq.getWeight() : 0.0);
                c.setScore(cReq.getScore() != null ? cReq.getScore() : 0.0);
                c.setTotalPoints(cReq.getTotalPoints() != null ? cReq.getTotalPoints() : 0.0);

                componentRepository.save(c);
            }
        }

        // Recompute currentScore server-side
        BigDecimal computed = computeSubjectPercent(subject);
        subject.setCurrentScore(computed);

        subjectRepository.save(subject);

        return ResponseEntity.ok().build();
    }


    private BigDecimal computeSubjectPercent(Subject subject) {
        boolean useTotalPoints = subject.isUseTotalPoints();
        List<Component> comps = componentRepository.findBySubject(subject);

        if (comps == null || comps.isEmpty()) {
            return null;
        }

        double percent;

        if (useTotalPoints) {
            double sumScore = 0.0;
            double sumTotal = 0.0;
            for (Component c : comps) {
                double s = c.getScore() != null ? c.getScore() : 0.0;
                double t = c.getTotalPoints() != null ? c.getTotalPoints() : 0.0;
                sumScore += s;
                sumTotal += t;
            }
            if (sumTotal <= 0.0) return null;
            percent = (sumScore / sumTotal) * 100.0;
        } else {
            double totalWeight = 0.0;
            double weighted = 0.0;

            for (Component c : comps) {
                double w = c.getWeight() != null ? c.getWeight() : 0.0;
                double s = c.getScore() != null ? c.getScore() : 0.0;
                double t = c.getTotalPoints() != null ? c.getTotalPoints() : 0.0;

                totalWeight += w;

                if (t > 0.0) {
                    weighted += (s / t) * w;
                } else if (s > 0.0) {
                    // Extra credit behavior (matches your JS intent)
                    weighted += s;
                }
            }

            // If no weights entered, treat as null
            if (totalWeight <= 0.0 && weighted == 0.0) return null;

            percent = weighted;
        }

        // store with 2 decimal places
        return BigDecimal.valueOf(percent).setScale(2, RoundingMode.HALF_UP);
    }

    private Subject loadOwnedSubject(Integer subjectId, UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        if (subject.getUser() == null || !subject.getUser().getUserId().equals(user.getUserId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Not allowed");
        }
        return subject;
    }



    
}



