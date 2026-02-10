package com.harshit.gradecalculator.dto;

import java.util.List;

public class SubjectSaveRequest {
    private Integer id;
    private Boolean useTotalPoints;
    private List<ComponentPayload> components;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Boolean getUseTotalPoints() { return useTotalPoints; }
    public void setUseTotalPoints(Boolean useTotalPoints) { this.useTotalPoints = useTotalPoints; }

    public List<ComponentPayload> getComponents() { return components; }
    public void setComponents(List<ComponentPayload> components) { this.components = components; }

    public static class ComponentPayload {
        private Integer id;
        private String name;
        private Double weight;
        private Double score;
        private Double totalPoints;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }

        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }

        public Double getTotalPoints() { return totalPoints; }
        public void setTotalPoints(Double totalPoints) { this.totalPoints = totalPoints; }
    }
}
