package com.harshit.gradecalculator.dto;

public class CanvasSyncResponse {

    private int subjectsUpdated;
    private int componentsUpdated;

    public CanvasSyncResponse() {}

    public CanvasSyncResponse(int subjectsUpdated, int componentsUpdated) {
        this.subjectsUpdated = subjectsUpdated;
        this.componentsUpdated = componentsUpdated;
    }

    public int getSubjectsUpdated() { return subjectsUpdated; }
    public void setSubjectsUpdated(int subjectsUpdated) { this.subjectsUpdated = subjectsUpdated; }

    public int getComponentsUpdated() { return componentsUpdated; }
    public void setComponentsUpdated(int componentsUpdated) { this.componentsUpdated = componentsUpdated; }
}
