package com.intramural.scheduling.controller;

import com.intramural.scheduling.model.Availability;
import com.intramural.scheduling.model.TimeOffRequest;
import com.intramural.scheduling.service.AvailabilityService;
import com.intramural.scheduling.util.ValidationUtil;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AvailabilityController {
    private AvailabilityService availabilityService;
    
    public AvailabilityController() {
        this.availabilityService = new AvailabilityService();
    }
    
    /**
     * Submit seasonal availability for an employee
     */
    public void submitAvailability(int employeeId,
                                   Availability.Season season,
                                   int year,
                                   List<Availability.Seasonal> availabilities) 
                                   throws SQLException {
        // Validate availability entries
        for (Availability.Seasonal avail : availabilities) {
            if (!ValidationUtil.isValidTimeRange(
                    avail.getStartTime(), avail.getEndTime())) {
                throw new IllegalArgumentException(
                    "Invalid time range for " + avail.getDayOfWeek()
                );
            }
        }
        
        availabilityService.submitSeasonalAvailability(
            employeeId, season, year, availabilities
        );
    }
    
    /**
     * Get employee's availability
     */
    public List<Availability.Seasonal> getAvailability(int employeeId,
                                                       Availability.Season season,
                                                       int year) 
                                                       throws SQLException {
        return availabilityService.getSeasonalAvailability(employeeId, season, year);
    }
    
    /**
     * Add permanent conflict
     */
    public void addConflict(int employeeId,
                           Availability.ConflictType type,
                           DayOfWeek day,
                           LocalTime start,
                           LocalTime end,
                           String description) throws SQLException {
        if (!ValidationUtil.isValidTimeRange(start, end)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        
        Availability.PermanentConflict conflict = 
            new Availability.PermanentConflict(
                employeeId, type, day, start, end, description
            );
        availabilityService.addPermanentConflict(conflict);
    }
    
    /**
     * Remove permanent conflict
     */
    public void removeConflict(int conflictId) throws SQLException {
        availabilityService.removePermanentConflict(conflictId);
    }
    
    /**
     * Get all conflicts for employee
     */
    public List<Availability.PermanentConflict> getConflicts(int employeeId) 
            throws SQLException {
        return availabilityService.getPermanentConflicts(employeeId);
    }
    
    /**
     * Check if employee is available at specific time
     */
    public boolean isAvailable(int employeeId,
                              Availability.Season season,
                              int year,
                              DayOfWeek day,
                              LocalTime start,
                              LocalTime end) throws SQLException {
        return availabilityService.isAvailable(
            employeeId, season, year, day, start, end
        );
    }
    
    /**
     * Validate availability doesn't conflict with permanent conflicts
     */
    public String validateAvailability(int employeeId,
                                      DayOfWeek day,
                                      LocalTime start,
                                      LocalTime end) throws SQLException {
        if (availabilityService.hasConflict(employeeId, day, start, end)) {
            return "This time conflicts with an existing permanent conflict";
        }
        return null; // Valid
    }
}