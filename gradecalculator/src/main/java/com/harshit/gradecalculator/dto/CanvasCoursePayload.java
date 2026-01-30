package com.harshit.gradecalculator.dto;

import java.util.List;

public class CanvasCoursePayload {

    private String name;
    private String code;
    private String status;
    private Integer credits;
    private Double currentScore;
    private Boolean includeInGpa;
    private List<CanvasAssignmentPayload> assignments;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public Double getCurrentScore() { return currentScore; }
    public void setCurrentScore(Double currentScore) { this.currentScore = currentScore; }

    public Boolean getIncludeInGpa() { return includeInGpa; }
    public void setIncludeInGpa(Boolean includeInGpa) { this.includeInGpa = includeInGpa; }

    public List<CanvasAssignmentPayload> getAssignments() { return assignments; }
    public void setAssignments(List<CanvasAssignmentPayload> assignments) { this.assignments = assignments; }
}
