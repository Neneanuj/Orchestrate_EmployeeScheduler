package com.intramural.scheduling.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Employee {
    
    public enum ExpertiseLevel {
        BEGINNER(1), INTERMEDIATE(2), EXPERT(3);
        
        private final int value;
        ExpertiseLevel(int value) { this.value = value; }
        public int getValue() { return value; }
    }
    
    public static class SportExpertise {
        private int expertiseId;
        private int sportId;
        private ExpertiseLevel level;
        private LocalDate certificationDate;
        
        public SportExpertise(int expertiseId, int sportId, ExpertiseLevel level) {
            this.expertiseId = expertiseId;
            this.sportId = sportId;
            this.level = level;
        }
        
        public int getExpertiseId() { return expertiseId; }
        public int getSportId() { return sportId; }
        public ExpertiseLevel getLevel() { return level; }
        public void setLevel(ExpertiseLevel level) { this.level = level; }
        public LocalDate getCertificationDate() { return certificationDate; }
        public void setCertificationDate(LocalDate date) { this.certificationDate = date; }
    }
    
    private int employeeId;
    private int userId;
    private String firstName;
    private String lastName;
    private String phone;
    private int maxHoursPerWeek;
    private double performanceRating;
    private boolean supervisorEligible;
    private boolean activeStatus;
    private List<SportExpertise> expertiseList;
    
    public Employee(int employeeId, int userId, String firstName, String lastName) {
        this.employeeId = employeeId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.maxHoursPerWeek = 20;
        this.performanceRating = 0.0;
        this.supervisorEligible = false;
        this.activeStatus = true;
        this.expertiseList = new ArrayList<>();
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public void addExpertise(SportExpertise expertise) {
        this.expertiseList.add(expertise);
    }
    
    public boolean hasExpertiseIn(int sportId) {
        return expertiseList.stream()
            .anyMatch(exp -> exp.getSportId() == sportId);
    }
    
    public ExpertiseLevel getExpertiseLevel(int sportId) {
        return expertiseList.stream()
            .filter(exp -> exp.getSportId() == sportId)
            .map(SportExpertise::getLevel)
            .findFirst()
            .orElse(ExpertiseLevel.BEGINNER);
    }
    
    // Getters and setters
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public int getMaxHoursPerWeek() { return maxHoursPerWeek; }
    public void setMaxHoursPerWeek(int maxHoursPerWeek) { 
        this.maxHoursPerWeek = maxHoursPerWeek; 
    }
    
    public double getPerformanceRating() { return performanceRating; }
    public void setPerformanceRating(double performanceRating) { 
        this.performanceRating = performanceRating; 
    }
    
    public boolean isSupervisorEligible() { return supervisorEligible; }
    public void setSupervisorEligible(boolean supervisorEligible) { 
        this.supervisorEligible = supervisorEligible; 
    }
    
    public boolean isActiveStatus() { return activeStatus; }
    public void setActiveStatus(boolean activeStatus) { 
        this.activeStatus = activeStatus; 
    }
    
    public List<SportExpertise> getExpertiseList() { return expertiseList; }
}