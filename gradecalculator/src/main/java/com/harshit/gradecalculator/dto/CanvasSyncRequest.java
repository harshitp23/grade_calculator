package com.harshit.gradecalculator.dto;

import java.util.List;

public class CanvasSyncRequest {

    private List<CanvasCoursePayload> courses;

    public List<CanvasCoursePayload> getCourses() { return courses; }
    public void setCourses(List<CanvasCoursePayload> courses) { this.courses = courses; }
}
