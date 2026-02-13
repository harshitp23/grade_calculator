package com.harshit.gradecalculator.dto;

import java.util.List;

public class SubjectSaveRequest {
    private Integer id;
    private Boolean useTotalPoints;
    
    // 👇 CHANGED: Use the standalone ComponentSaveRequest class
    private List<ComponentSaveRequest> components; 

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Boolean getUseTotalPoints() { return useTotalPoints; }
    public void setUseTotalPoints(Boolean useTotalPoints) { this.useTotalPoints = useTotalPoints; }

    public List<ComponentSaveRequest> getComponents() { return components; }
    public void setComponents(List<ComponentSaveRequest> components) { this.components = components; }
}
