package com.harshit.gradecalculator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Fix: Added this import
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String subjectName;

    @Column(nullable = false)
    private String subjectCode;

    private int credits;
    private String status;

    private BigDecimal currentScore;

    // Custom Settings
    private String gradingScale;
    private String letterGrade;

    private boolean useTotalPoints;
    private Boolean includeInGpa = true;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("subject")
    private List<Component> components = new ArrayList<>();

    // --- GETTERS AND SETTERS ---

    // Map 'id' to 'subjectId' for Frontend compatibility
    @JsonProperty("subjectId")
    public Integer getSubjectId() { return id; }
    public void setSubjectId(Integer id) { this.id = id; }

    // Standard Getters
    @JsonIgnore
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

    public boolean isUseTotalPoints() { return useTotalPoints; }
    public void setUseTotalPoints(boolean useTotalPoints) { this.useTotalPoints = useTotalPoints; }

    public Boolean getIncludeInGpa() { return includeInGpa == null ? Boolean.TRUE : includeInGpa; }
    public void setIncludeInGpa(Boolean includeInGpa) { this.includeInGpa = includeInGpa; }

    public List<Component> getComponents() { return components; }
    public void setComponents(List<Component> components) { this.components = components; }
}
