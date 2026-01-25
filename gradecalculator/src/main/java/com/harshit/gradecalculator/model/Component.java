package com.harshit.gradecalculator.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Components")
public class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "component_id")
    private Integer componentId;

    @Column(name = "component_name", nullable = false)
    private String componentName;

    // Default 0.00 for Total Points mode
    @Column(nullable = false)
    private BigDecimal weightage = BigDecimal.ZERO; 

    @Column(name = "points_scored")
    private BigDecimal pointsScored;

    @Column(name = "total_points")
    private BigDecimal totalPoints;

    // Link back to Subject
    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    @JsonIgnore
    private Subject subject;

    // --- Getters and Setters ---
    public Integer getComponentId() { return componentId; }
    public void setComponentId(Integer componentId) { this.componentId = componentId; }

    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = componentName; }

    public BigDecimal getWeightage() { return weightage; }
    public void setWeightage(BigDecimal weightage) { this.weightage = weightage; }

    public BigDecimal getPointsScored() { return pointsScored; }
    public void setPointsScored(BigDecimal pointsScored) { this.pointsScored = pointsScored; }

    public BigDecimal getTotalPoints() { return totalPoints; }
    public void setTotalPoints(BigDecimal totalPoints) { this.totalPoints = totalPoints; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
}