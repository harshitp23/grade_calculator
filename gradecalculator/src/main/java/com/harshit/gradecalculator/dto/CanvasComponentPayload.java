package com.harshit.gradecalculator.dto;

import java.util.List;

public class CanvasComponentPayload {
    private String name;
    private Double weight;
    private Double score;
    private Double totalPoints;
    private int dropLowest; // New field
    
    private List<CanvasAssignmentPayload> assignments;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Double getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Double totalPoints) { this.totalPoints = totalPoints; }

    public int getDropLowest() { return dropLowest; }
    public void setDropLowest(int dropLowest) { this.dropLowest = dropLowest; }

    public List<CanvasAssignmentPayload> getAssignments() { return assignments; }
    public void setAssignments(List<CanvasAssignmentPayload> assignments) { this.assignments = assignments; }
}
