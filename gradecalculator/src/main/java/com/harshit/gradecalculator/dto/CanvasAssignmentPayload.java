package com.harshit.gradecalculator.dto;

public class CanvasAssignmentPayload {

    private String name;
    private Double score;
    private Double totalPoints;
    private Double weight;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Double getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Double totalPoints) { this.totalPoints = totalPoints; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
}
