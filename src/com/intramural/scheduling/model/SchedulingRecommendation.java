package com.intramural.scheduling.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchedulingRecommendation {
    private Employee employee;
    private double score;
    private Map<String, Double> scoreBreakdown;
    private List<String> violations;
    private List<String> warnings;
    
    public SchedulingRecommendation(Employee employee) {
        this.employee = employee;
        this.score = 0.0;
        this.scoreBreakdown = new HashMap<>();
        this.violations = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }
    
    public void addScoreComponent(String component, double value) {
        scoreBreakdown.put(component, value);
        recalculateScore();
    }
    
    public void addViolation(String violation) {
        violations.add(violation);
        score = -1000.0;
    }
    
    public void addWarning(String warning) {
        warnings.add(warning);
    }
    
    private void recalculateScore() {
        if (violations.isEmpty()) {
            score = scoreBreakdown.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        }
    }
    
    public boolean hasViolations() {
        return !violations.isEmpty();
    }
    
    public boolean isValid() {
        return violations.isEmpty();
    }
    
    public String getScoreSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Score: %.2f\n", score));
        scoreBreakdown.forEach((key, value) -> 
            sb.append(String.format("  %s: %.2f\n", key, value))
        );
        return sb.toString();
    }
    
    // Getters
    public Employee getEmployee() { return employee; }
    public double getScore() { return score; }
    public Map<String, Double> getScoreBreakdown() { return scoreBreakdown; }
    public List<String> getViolations() { return violations; }
    public List<String> getWarnings() { return warnings; }
}


