package com.intramural.scheduling.model;

public class Sport {
    private int sportId;
    private String sportName;
    private int defaultDurationMinutes;
    private int requiredSupervisors;
    private int requiredReferees;
    
    public Sport(int sportId, String sportName, int defaultDurationMinutes,
                 int requiredSupervisors, int requiredReferees) {
        this.sportId = sportId;
        this.sportName = sportName;
        this.defaultDurationMinutes = defaultDurationMinutes;
        this.requiredSupervisors = requiredSupervisors;
        this.requiredReferees = requiredReferees;
    }
    
    public int getTotalStaffRequired() {
        return requiredSupervisors + requiredReferees;
    }
    
    // Getters
    public int getSportId() { return sportId; }
    public String getSportName() { return sportName; }
    public int getDefaultDurationMinutes() { return defaultDurationMinutes; }
    public int getRequiredSupervisors() { return requiredSupervisors; }
    public int getRequiredReferees() { return requiredReferees; }
}