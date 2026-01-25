package com.harshit.gradecalculator.service;

import com.harshit.gradecalculator.model.Component;
import com.harshit.gradecalculator.model.Subject;
import com.harshit.gradecalculator.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class GradeService {

    @Autowired
    private SubjectRepository subjectRepository;

    public String calculateTarget(Integer subjectId, double targetGrade) {
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        if (subject == null) return "Error: Subject not found.";

        List<Component> components = subject.getComponents();
        
        // 1. Calculate Current Totals
        double totalWeightUsed = 0.0;
        double totalWeightedScore = 0.0;

        for (Component c : components) {
            // Only count graded items
            if (c.getPointsScored() != null) {
                double score = c.getPointsScored().doubleValue();
                double total = c.getTotalPoints().doubleValue();
                double weight = c.getWeightage().doubleValue();

                double percentage = (score / total) * 100;
                double weightContribution = (percentage * weight) / 100;

                totalWeightedScore += weightContribution;
                totalWeightUsed += weight;
            }
        }

        // 2. Check if target is already impossible or achieved
        double remainingWeight = 100.0 - totalWeightUsed;
        
        // Current percentage normalized to 100%
        double currentPercentage = (totalWeightUsed == 0) ? 100.0 : (totalWeightedScore / totalWeightUsed) * 100;

        if (remainingWeight <= 0) {
            return "Course Complete. Final Grade: " + String.format("%.2f", totalWeightedScore) + "%";
        }

        // 3. The Algebra Formula:
        // (CurrentScore + RequiredScore) = Target
        // RequiredScore = Target - CurrentScore
        // Required % on Remaining = (RequiredScore / RemainingWeight) * 100
        
        double requiredScoreForTarget = targetGrade - totalWeightedScore;
        double requiredPercentage = (requiredScoreForTarget / remainingWeight) * 100;

        if (requiredPercentage > 100) {
            return "Impossible! You need " + String.format("%.2f", requiredPercentage) + "% on remaining assignments.";
        } else if (requiredPercentage < 0) {
            return "You already have this grade secured!";
        } else {
            return "You need to average " + String.format("%.2f", requiredPercentage) + "% on your remaining assignments.";
        }
    }
}