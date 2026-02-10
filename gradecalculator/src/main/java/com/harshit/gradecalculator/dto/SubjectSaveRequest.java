package com.harshit.gradecalculator.dto;

import java.util.List;

public class SubjectSaveRequest {
    private Integer id;
    private String subjectName;
    private String subjectCode;
    private Integer credits;
    private String status;
    private Boolean useTotalPoints;
    private List<ComponentSaveRequest> components;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getUseTotalPoints() { return useTotalPoints; }
    public void setUseTotalPoints(Boolean useTotalPoints) { this.useTotalPoints = useTotalPoints; }

    public List<ComponentSaveRequest> getComponents() { return components; }
    public void setComponents(List<ComponentSaveRequest> components) { this.components = components; }
}
