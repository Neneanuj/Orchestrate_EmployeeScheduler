package com.intramural.scheduling.controller;

import com.intramural.scheduling.model.TimeOffRequest;
import com.intramural.scheduling.service.AvailabilityService;
import com.intramural.scheduling.util.ValidationUtil;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TimeOffController {
    private AvailabilityService availabilityService;
    
    public TimeOffController() {
        this.availabilityService = new AvailabilityService();
    }
    
    /**
     * Submit full-day time-off request
     */
    public TimeOffRequest submitFullDayRequest(int employeeId,
                                               LocalDate date,
                                               String reason) throws SQLException {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot request time-off for past dates");
        }
        
        return availabilityService.submitTimeOffRequest(
            employeeId, date, true, null, null, reason
        );
    }
    
    /**
     * Submit partial-day time-off request
     */
    public TimeOffRequest submitPartialDayRequest(int employeeId,
                                                  LocalDate date,
                                                  LocalTime startTime,
                                                  LocalTime endTime,
                                                  String reason) throws SQLException {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot request time-off for past dates");
        }
        
        if (!ValidationUtil.isValidTimeRange(startTime, endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        
        return availabilityService.submitTimeOffRequest(
            employeeId, date, false, startTime, endTime, reason
        );
    }
    
    /**
     * Get pending requests
     */
    public List<TimeOffRequest> getPendingRequests() throws SQLException {
        return availabilityService.getPendingTimeOffRequests();
    }
    
    /**
     * Get approved time-offs for employee
     */
    public List<TimeOffRequest> getApprovedTimeOffs(int employeeId,
                                                    LocalDate startDate,
                                                    LocalDate endDate) 
                                                    throws SQLException {
        return availabilityService.getApprovedTimeOffs(employeeId, startDate, endDate);
    }
    
    /**
     * Approve time-off request
     */
    public void approveRequest(int requestId, int reviewerId) throws SQLException {
        availabilityService.approveTimeOffRequest(requestId, reviewerId);
    }
    
    /**
     * Deny time-off request
     */
    public void denyRequest(int requestId, int reviewerId) throws SQLException {
        availabilityService.denyTimeOffRequest(requestId, reviewerId);
    }
    
    /**
     * Validate time-off request
     */
    public String validateRequest(LocalDate date, LocalTime startTime, 
                                  LocalTime endTime, boolean isFullDay) {
        if (date.isBefore(LocalDate.now())) {
            return "Cannot request time-off for past dates";
        }
        
        if (!isFullDay && startTime != null && endTime != null) {
            if (!ValidationUtil.isValidTimeRange(startTime, endTime)) {
                return "Start time must be before end time";
            }
        }
        
        return null; // Valid
    }
}