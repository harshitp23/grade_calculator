package com.harshit.gradecalculator.dto;

public class ComponentSaveRequest {
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
