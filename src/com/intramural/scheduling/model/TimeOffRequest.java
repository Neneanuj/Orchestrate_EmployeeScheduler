package com.intramural.scheduling.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeOffRequest {
    
    public enum Status {
        PENDING, APPROVED, DENIED
    }
    
    private int requestId;
    private int employeeId;
    private LocalDate requestDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isFullDay;
    private String reason;
    private Status status;
    private LocalDateTime submittedAt;
    private Integer reviewedBy;
    private LocalDateTime reviewedAt;
    
    public TimeOffRequest(int employeeId, LocalDate requestDate, 
                         boolean isFullDay, String reason) {
        this.employeeId = employeeId;
        this.requestDate = requestDate;
        this.isFullDay = isFullDay;
        this.reason = reason;
        this.status = Status.PENDING;
        this.submittedAt = LocalDateTime.now();
    }
    
    public TimeOffRequest(int employeeId, LocalDate requestDate, 
                         LocalTime startTime, LocalTime endTime, String reason) {
        this(employeeId, requestDate, false, reason);
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public boolean conflictsWith(LocalDateTime shiftStart, LocalDateTime shiftEnd) {
        if (!requestDate.equals(shiftStart.toLocalDate())) {
            return false;
        }
        
        if (isFullDay) {
            return true;
        }
        
        LocalTime shiftStartTime = shiftStart.toLocalTime();
        LocalTime shiftEndTime = shiftEnd.toLocalTime();
        
        return !(shiftEndTime.isBefore(startTime) || 
                 shiftStartTime.isAfter(endTime));
    }
    
    public void approve(int reviewerId) {
        this.status = Status.APPROVED;
        this.reviewedBy = reviewerId;
        this.reviewedAt = LocalDateTime.now();
    }
    
    public void deny(int reviewerId) {
        this.status = Status.DENIED;
        this.reviewedBy = reviewerId;
        this.reviewedAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public int getRequestId() { return requestId; }
    public void setRequestId(int id) { this.requestId = id; }
    public int getEmployeeId() { return employeeId; }
    public LocalDate getRequestDate() { return requestDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public boolean isFullDay() { return isFullDay; }
    public String getReason() { return reason; }
    public Status getStatus() { return status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public Integer getReviewedBy() { return reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
}