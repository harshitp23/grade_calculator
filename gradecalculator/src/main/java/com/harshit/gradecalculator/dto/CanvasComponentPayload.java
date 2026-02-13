package com.harshit.gradecalculator.dto;

import java.util.List;

public class CanvasComponentPayload {
    private String name;       // e.g. "Assignments", "Exams"
    private Double weight;     // e.g. 40.0
    private Double score;      // Calculated score for the group
    private Double totalPoints;
    
    private List<CanvasAssignmentPayload> assignments;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Double getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Double totalPoints) { this.totalPoints = totalPoints; }

    public List<CanvasAssignmentPayload> getAssignments() { return assignments; }
    public void setAssignments(List<CanvasAssignmentPayload> assignments) { this.assignments = assignments; }
}
