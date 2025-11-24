package com.intramural.scheduling.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Availability {
    
    public enum Season {
        FALL, SPRING, SUMMER
    }
    
    public enum ConflictType {
        CLASS, JOB, OTHER
    }
    
    // Seasonal Availability
    public static class Seasonal {
        private int availabilityId;
        private int employeeId;
        private Season season;
        private int year;
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private boolean isPreferred;
        private LocalDateTime createdAt;
        
        public Seasonal(int employeeId, Season season, int year,
                       DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
            this.employeeId = employeeId;
            this.season = season;
            this.year = year;
            this.dayOfWeek = dayOfWeek;
            this.startTime = startTime;
            this.endTime = endTime;
            this.isPreferred = false;
            this.createdAt = LocalDateTime.now();
        }
        
        public boolean isAvailableAt(LocalDateTime dateTime) {
            if (dateTime.getDayOfWeek() != this.dayOfWeek) {
                return false;
            }
            LocalTime time = dateTime.toLocalTime();
            return !time.isBefore(startTime) && !time.isAfter(endTime);
        }
        
        public boolean overlapsWith(LocalTime checkStart, LocalTime checkEnd) {
            return !(checkEnd.isBefore(startTime) || checkStart.isAfter(endTime));
        }
        
        // Getters and setters
        public int getAvailabilityId() { return availabilityId; }
        public void setAvailabilityId(int id) { this.availabilityId = id; }
        public int getEmployeeId() { return employeeId; }
        public Season getSeason() { return season; }
        public int getYear() { return year; }
        public DayOfWeek getDayOfWeek() { return dayOfWeek; }
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
        public boolean isPreferred() { return isPreferred; }
        public void setPreferred(boolean preferred) { this.isPreferred = preferred; }
    }
    
    // Permanent Conflict
    public static class PermanentConflict {
        private int conflictId;
        private int employeeId;
        private ConflictType conflictType;
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private String description;
        
        public PermanentConflict(int employeeId, ConflictType conflictType,
                                DayOfWeek dayOfWeek, LocalTime startTime, 
                                LocalTime endTime, String description) {
            this.employeeId = employeeId;
            this.conflictType = conflictType;
            this.dayOfWeek = dayOfWeek;
            this.startTime = startTime;
            this.endTime = endTime;
            this.description = description;
        }
        
        public boolean conflictsWith(LocalDateTime dateTime, LocalTime endTime) {
            if (dateTime.getDayOfWeek() != this.dayOfWeek) {
                return false;
            }
            LocalTime checkTime = dateTime.toLocalTime();
            return !(endTime.isBefore(this.startTime) || checkTime.isAfter(this.endTime));
        }
        
        // Getters
        public int getConflictId() { return conflictId; }
        public void setConflictId(int id) { this.conflictId = id; }
        public int getEmployeeId() { return employeeId; }
        public ConflictType getConflictType() { return conflictType; }
        public DayOfWeek getDayOfWeek() { return dayOfWeek; }
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
        public String getDescription() { return description; }
    }
}