package com.harshit.gradecalculator.controller;

import com.harshit.gradecalculator.model.Subject;
import com.harshit.gradecalculator.model.Component;
import com.harshit.gradecalculator.model.User;
import com.harshit.gradecalculator.repository.SubjectRepository;
import com.harshit.gradecalculator.repository.UserRepository;
import com.harshit.gradecalculator.repository.ComponentRepository;
import com.harshit.gradecalculator.dto.SubjectSaveRequest;
import com.harshit.gradecalculator.dto.ComponentSaveRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Autowired private SubjectRepository subjectRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ComponentRepository componentRepository;

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
            @RequestParam String termSeason,
            @RequestParam Integer termYear,
            @RequestParam(required = false) String letterGrade,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        Subject s = new Subject();
        s.setSubjectName(name);
        s.setSubjectCode(code);
        s.setCredits(credits);
        s.setStatus(status);
        s.setTermSeason(termSeason);
        s.setTermYear(termYear);
        s.setIncludeInGpa(true);
        s.setUser(user);

        // If it's an old class, apply the letter grade directly
        if ("Completed".equals(status) && letterGrade != null && !letterGrade.isBlank()) {
            s.setLetterGrade(letterGrade);
        }

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
        // Track previous score for the arrow indicator
        if (s.getCurrentScore() != null) s.setPreviousScore(s.getCurrentScore());
        s.setCurrentScore(BigDecimal.valueOf(score));
        subjectRepository.save(s);
        return "Score Updated!";
    }

    @PostMapping("/update-settings")
    public String updateSettings(
            @RequestParam Integer id,
            @RequestParam String name,
            @RequestParam String code,
            @RequestParam String termSeason,
            @RequestParam Integer termYear,
            @RequestParam(required = false) String gradingScale,
            @RequestParam String status,
            @RequestParam(required = false) Boolean includeInGpa,
            @AuthenticationPrincipal UserDetails userDetails) {

        Subject s = loadOwnedSubject(id, userDetails);
        s.setSubjectName(name);
        s.setSubjectCode(code);
        s.setTermSeason(termSeason);
        s.setTermYear(termYear);
        s.setGradingScale(gradingScale);
        s.setStatus(status);
        if (includeInGpa != null) s.setIncludeInGpa(includeInGpa);
        subjectRepository.save(s);
        return "Settings Updated!";
    }

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

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Subject subject = subjectRepository.findById(payload.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        if (!subject.getUser().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }

        if (payload.getUseTotalPoints() != null) subject.setUseTotalPoints(payload.getUseTotalPoints());

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

        // Track score change
        if (subject.getCurrentScore() != null) subject.setPreviousScore(subject.getCurrentScore());
        subject.setCurrentScore(computeSubjectPercent(subject));

        subjectRepository.save(subject);
        return ResponseEntity.ok().build();
    }

    private BigDecimal computeSubjectPercent(Subject subject) {
        boolean useTotalPoints = subject.isUseTotalPoints();
        List<Component> comps = componentRepository.findBySubject(subject);
        if (comps == null || comps.isEmpty()) return null;

        double percent;
        if (useTotalPoints) {
            double sumScore = 0.0, sumTotal = 0.0;
            for (Component c : comps) {
                sumScore += (c.getScore() != null ? c.getScore() : 0.0);
                sumTotal += (c.getTotalPoints() != null ? c.getTotalPoints() : 0.0);
            }
            if (sumTotal <= 0.0) return null;
            percent = (sumScore / sumTotal) * 100.0;
        } else {
            double totalWeight = 0.0, weighted = 0.0;
            for (Component c : comps) {
                double w = c.getWeight() != null ? c.getWeight() : 0.0;
                double s = c.getScore() != null ? c.getScore() : 0.0;
                double t = c.getTotalPoints() != null ? c.getTotalPoints() : 0.0;
                totalWeight += w;
                if (t > 0.0) weighted += (s / t) * w;
                else if (s > 0.0) weighted += s;
            }
            if (totalWeight <= 0.0 && weighted == 0.0) return null;
            percent = weighted;
        }
        return BigDecimal.valueOf(percent).setScale(2, RoundingMode.HALF_UP);
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
