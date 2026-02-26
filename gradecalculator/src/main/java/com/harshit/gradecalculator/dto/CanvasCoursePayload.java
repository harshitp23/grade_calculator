package com.harshit.gradecalculator.dto;

import java.util.List;

public class CanvasCoursePayload {

    private String name;
    private String code;
    private String status;
    private Integer credits;
    private Double currentScore;
    private String status;
    private String letterGrade;
    private String termSeason;
    private Integer termYear;
    private Integer credits;
    
    // 👇 CHANGED: Now holds Components (Groups) instead of flat assignments
    private List<CanvasComponentPayload> components;

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

    public List<CanvasComponentPayload> getComponents() { return components; }
    public void setComponents(List<CanvasComponentPayload> components) { this.components = components; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLetterGrade() { return letterGrade; }
    public void setLetterGrade(String letterGrade) { this.letterGrade = letterGrade; }

    public String getTermSeason() { return termSeason; }
    public void setTermSeason(String termSeason) { this.termSeason = termSeason; }

    public Integer getTermYear() { return termYear; }
    public void setTermYear(Integer termYear) { this.termYear = termYear; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
}
