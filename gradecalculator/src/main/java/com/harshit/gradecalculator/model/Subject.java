package com.harshit.gradecalculator.model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import java.math.BigDecimal; // Import for Score

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Changed from subjectId to id for consistency

    @Column(nullable = false)
    private String subjectName;

    @Column(nullable = false)
    private String subjectCode;

    private int credits;
    private String status;

    // Score can be null initially, use BigDecimal for precision
    private BigDecimal currentScore;

    // Custom Settings
    private String gradingScale;
    private String letterGrade;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // --- 👇 GETTERS AND SETTERS 👇 ---

    // Note: The frontend expects 'subjectId' sometimes, we map 'id' to it.
    public Integer getSubjectId() { return id; } 
    public void setSubjectId(Integer id) { this.id = id; }
    
    // Standard Getters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getCurrentScore() { return currentScore; }
    public void setCurrentScore(BigDecimal currentScore) { this.currentScore = currentScore; }

    public String getGradingScale() { return gradingScale; }
    public void setGradingScale(String gradingScale) { this.gradingScale = gradingScale; }

    public String getLetterGrade() { return letterGrade; }
    public void setLetterGrade(String letterGrade) { this.letterGrade = letterGrade; }

    @JsonIgnore
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}

