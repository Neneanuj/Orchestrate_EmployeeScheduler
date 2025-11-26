package com.intramural.scheduling.service;

import com.intramural.scheduling.dao.AvailabilityDAO;
import com.intramural.scheduling.model.Availability;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class AvailabilityService {
    private AvailabilityDAO availabilityDAO;
    
    public AvailabilityService() {
        this.availabilityDAO = new AvailabilityDAO();
    }
    
    /**
     * Submit seasonal availability for an employee
     */
    public void submitSeasonalAvailability(int employeeId, 
                                          Availability.Season season,
                                          int year,
                                          List<Availability.Seasonal> availabilities) 
                                          throws SQLException {
        // Delete existing availability for this season
        availabilityDAO.deleteByEmployeeAndSeason(employeeId, season, year);
        
        // Insert new availability
        for (Availability.Seasonal avail : availabilities) {
            availabilityDAO.insert(avail);
        }
    }
    
    /**
     * Get employee's availability for a season
     */
    public List<Availability.Seasonal> getSeasonalAvailability(int employeeId,
                                                               Availability.Season season,
                                                               int year) 
                                                               throws SQLException {
        return availabilityDAO.getByEmployee(employeeId, season, year);
    }
    
    /**
     * Add a permanent conflict (class, job, etc.)
     */
    public void addPermanentConflict(Availability.PermanentConflict conflict) 
            throws SQLException {
        availabilityDAO.insertConflict(conflict);
    }
    
    /**
     * Remove a permanent conflict
     */
    public void removePermanentConflict(int conflictId) throws SQLException {
        availabilityDAO.deleteConflict(conflictId);
    }
    
    /**
     * Get all permanent conflicts for an employee
     */
    public List<Availability.PermanentConflict> getPermanentConflicts(int employeeId) 
            throws SQLException {
        return availabilityDAO.getConflictsByEmployee(employeeId);
    }
    
    /**
     * Check if employee is available on a specific day/time
     */
    public boolean isAvailable(int employeeId, 
                              Availability.Season season,
                              int year,
                              DayOfWeek dayOfWeek,
                              LocalTime startTime,
                              LocalTime endTime) throws SQLException {
        List<Availability.Seasonal> availabilities = 
            getSeasonalAvailability(employeeId, season, year);
        
        return availabilities.stream()
            .anyMatch(avail -> 
                avail.getDayOfWeek() == dayOfWeek &&
                avail.overlapsWith(startTime, endTime)
            );
    }
    
    /**
     * Validate availability doesn't overlap with conflicts
     */
    public boolean hasConflict(int employeeId,
                              DayOfWeek dayOfWeek,
                              LocalTime startTime,
                              LocalTime endTime) throws SQLException {
        List<Availability.PermanentConflict> conflicts = 
            getPermanentConflicts(employeeId);
        
        return conflicts.stream()
            .anyMatch(conflict -> 
                conflict.getDayOfWeek() == dayOfWeek &&
                !(endTime.isBefore(conflict.getStartTime()) || 
                  startTime.isAfter(conflict.getEndTime()))
            );
    }
}