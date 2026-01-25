package com.harshit.gradecalculator.model;

import jakarta.persistence.*;
import java.math.BigDecimal; 
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    private Integer subjectId;

    // Link to the User who owns this subject
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "subject_name", nullable = false)
    private String subjectName;

    @Column(name = "subject_code")
    private String subjectCode;

    @Column(nullable = false)
    private Integer credits = 3;

    @Column(nullable = false)
    private String status = "Current";

    @Column(name = "grading_mode", nullable = false)
    private String gradingMode = "WEIGHTED";

    @Column(name = "current_score")
    private BigDecimal currentScore;

    @Column(name = "current_grade")
    private String currentGrade;

    // --- Getters and Setters ---

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGradingMode() { return gradingMode; }
    public void setGradingMode(String gradingMode) { this.gradingMode = gradingMode; }

    public BigDecimal getCurrentScore() { return currentScore; }
    public void setCurrentScore(BigDecimal currentScore) { this.currentScore = currentScore; }

    public String getCurrentGrade() { return currentGrade; }
    public void setCurrentGrade(String currentGrade) { this.currentGrade = currentGrade; }
    
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Component> components;

    public java.util.List<Component> getComponents() { return components; }
    public void setComponents(java.util.List<Component> components) { this.components = components; }
}