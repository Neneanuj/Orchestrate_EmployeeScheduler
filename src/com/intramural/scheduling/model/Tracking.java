package com.intramural.scheduling.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Tracking {
    
    public enum NotificationType {
        INFO, WARNING, ALERT
    }
    
    // Weekly Hours
    public static class WeeklyHours {
        private int trackingId;
        private int employeeId;
        private LocalDate weekStartDate;
        private double totalScheduledHours;
        private double totalWorkedHours;
        private LocalDateTime lastUpdated;
        
        public WeeklyHours(int employeeId, LocalDate weekStartDate) {
            this.employeeId = employeeId;
            this.weekStartDate = weekStartDate;
            this.totalScheduledHours = 0.0;
            this.totalWorkedHours = 0.0;
            this.lastUpdated = LocalDateTime.now();
        }
        
        public void addScheduledHours(double hours) {
            this.totalScheduledHours += hours;
            this.lastUpdated = LocalDateTime.now();
        }
        
        public void removeScheduledHours(double hours) {
            this.totalScheduledHours = Math.max(0, this.totalScheduledHours - hours);
            this.lastUpdated = LocalDateTime.now();
        }
        
        public double getRemainingHours(int maxHours) {
            return Math.max(0, maxHours - totalScheduledHours);
        }
        
        public boolean canAccommodate(double hours, int maxHours) {
            return (totalScheduledHours + hours) <= maxHours;
        }
        
        public boolean isApproachingLimit(int maxHours, double threshold) {
            return (totalScheduledHours / maxHours) >= threshold;
        }
        
        // Getters and setters
        public int getTrackingId() { return trackingId; }
        public void setTrackingId(int id) { this.trackingId = id; }
        public int getEmployeeId() { return employeeId; }
        public LocalDate getWeekStartDate() { return weekStartDate; }
        public double getTotalScheduledHours() { return totalScheduledHours; }
        public void setTotalScheduledHours(double hours) { 
            this.totalScheduledHours = hours; 
        }
        public double getTotalWorkedHours() { return totalWorkedHours; }
        public void setTotalWorkedHours(double hours) { 
            this.totalWorkedHours = hours; 
        }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
    }
    
    // Assignment History
    public static class AssignmentHistory {
        private int historyId;
        private int shiftId;
        private int employeeId;
        private LocalDateTime assignedAt;
        private LocalDateTime unassignedAt;
        private Double hoursWorked;
        private String performanceNotes;
        
        public AssignmentHistory(int shiftId, int employeeId) {
            this.shiftId = shiftId;
            this.employeeId = employeeId;
            this.assignedAt = LocalDateTime.now();
        }
        
        public void markUnassigned() {
            this.unassignedAt = LocalDateTime.now();
        }
        
        public boolean isCurrentlyAssigned() {
            return unassignedAt == null;
        }
        
        // Getters and setters
        public int getHistoryId() { return historyId; }
        public void setHistoryId(int id) { this.historyId = id; }
        public int getShiftId() { return shiftId; }
        public int getEmployeeId() { return employeeId; }
        public LocalDateTime getAssignedAt() { return assignedAt; }
        public LocalDateTime getUnassignedAt() { return unassignedAt; }
        public Double getHoursWorked() { return hoursWorked; }
        public void setHoursWorked(Double hours) { this.hoursWorked = hours; }
        public String getPerformanceNotes() { return performanceNotes; }
        public void setPerformanceNotes(String notes) { this.performanceNotes = notes; }
    }
    
    // Notification
    public static class Notification {
        private int notificationId;
        private int userId;
        private String message;
        private NotificationType notificationType;
        private boolean isRead;
        private LocalDateTime createdAt;
        
        public Notification(int userId, String message, NotificationType type) {
            this.userId = userId;
            this.message = message;
            this.notificationType = type;
            this.isRead = false;
            this.createdAt = LocalDateTime.now();
        }
        
        public void markAsRead() {
            this.isRead = true;
        }
        
        // Getters and setters
        public int getNotificationId() { return notificationId; }
        public void setNotificationId(int id) { this.notificationId = id; }
        public int getUserId() { return userId; }
        public String getMessage() { return message; }
        public NotificationType getNotificationType() { return notificationType; }
        public boolean isRead() { return isRead; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}