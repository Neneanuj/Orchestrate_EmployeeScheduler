package com.intramural.scheduling.service;

import com.intramural.scheduling.dao.*;
import com.intramural.scheduling.model.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service for validating shift assignments
 * Addresses BUG-F012 (conflict detection), BUG-F013 (availability check), BUG-F015 (expertise validation)
 */
public class ShiftValidationService {
    private ShiftDAO shiftDAO;
    private GameScheduleDAO gameScheduleDAO;
    private AvailabilityDAO availabilityDAO;
    private EmployeeExpertiseDAO expertiseDAO;
    
    public ShiftValidationService() {
        this.shiftDAO = new ShiftDAO();
        this.gameScheduleDAO = new GameScheduleDAO();
        this.availabilityDAO = new AvailabilityDAO();
        this.expertiseDAO = new EmployeeExpertiseDAO();
    }
    
    /**
     * BUG-F012: Check if employee has conflicting shift assignments
     * @param employeeId Employee to check
     * @param gameScheduleId Game schedule being assigned
     * @return Error message if conflict exists, null if no conflict
     */
    public String checkForConflicts(int employeeId, int gameScheduleId) throws SQLException {
        Schedule.Game targetGame = gameScheduleDAO.getById(gameScheduleId);
        if (targetGame == null) {
            return "Game schedule not found";
        }
        
        LocalDate gameDate = targetGame.getGameDate();
        LocalTime gameStart = targetGame.getStartTime();
        LocalTime gameEnd = targetGame.getEndTime();
        
        // Get all games on the same date
        List<Schedule.Game> gamesOnDate = gameScheduleDAO.getByDateRange(gameDate, gameDate);
        
        for (Schedule.Game game : gamesOnDate) {
            if (game.getScheduleId() == gameScheduleId) {
                continue; // Skip the target game itself
            }
            
            // Check if times overlap
            if (timesOverlap(gameStart, gameEnd, game.getStartTime(), game.getEndTime())) {
                // Check if employee is assigned to any shift in this game
                List<Schedule.Shift> shifts = shiftDAO.getByGameSchedule(game.getScheduleId());
                for (Schedule.Shift shift : shifts) {
                    if (shift.getAssignedEmployeeId() != null && 
                        shift.getAssignedEmployeeId() == employeeId) {
                        return "Employee is already assigned to another shift at " + 
                               game.getStartTime() + " - " + game.getEndTime() + 
                               " on " + game.getGameDate();
                    }
                }
            }
        }
        
        return null; // No conflict
    }
    
    /**
     * BUG-F013: Check if employee is available at the shift time
     * @param employeeId Employee to check
     * @param gameScheduleId Game schedule being assigned
     * @return Error message if not available, null if available
     */
    public String checkAvailability(int employeeId, int gameScheduleId) throws SQLException {
        Schedule.Game game = gameScheduleDAO.getById(gameScheduleId);
        if (game == null) {
            return "Game schedule not found";
        }
        
        LocalDate gameDate = game.getGameDate();
        LocalTime gameStart = game.getStartTime();
        LocalTime gameEnd = game.getEndTime();
        
        // Determine season and year
        Availability.Season season = getSeason(gameDate);
        int year = gameDate.getYear();
        
        // Get employee's availability for this season
        List<Availability.Seasonal> availabilities = 
            availabilityDAO.getByEmployee(employeeId, season, year);
        
        if (availabilities.isEmpty()) {
            return "Employee has not submitted availability for " + season + " " + year;
        }
        
        // Check if employee is available on this day of week and time
        java.time.DayOfWeek dayOfWeek = gameDate.getDayOfWeek();
        boolean isAvailable = false;
        
        for (Availability.Seasonal avail : availabilities) {
            if (avail.getDayOfWeek() == dayOfWeek) {
                // Check if game time falls within available time
                if (timeWithinRange(gameStart, gameEnd, avail.getStartTime(), avail.getEndTime())) {
                    isAvailable = true;
                    break;
                }
            }
        }
        
        if (!isAvailable) {
            return "Employee is not available on " + dayOfWeek + " from " + 
                   gameStart + " to " + gameEnd;
        }
        
        return null; // Employee is available
    }
    
    /**
     * BUG-F015: Check if employee has expertise in the game's sport
     * @param employeeId Employee to check
     * @param gameScheduleId Game schedule being assigned
     * @return Error message if no expertise, null if qualified
     */
    public String checkSportExpertise(int employeeId, int gameScheduleId) throws SQLException {
        Schedule.Game game = gameScheduleDAO.getById(gameScheduleId);
        if (game == null) {
            return "Game schedule not found";
        }
        
        int sportId = game.getSportId();
        
        // Check if employee has expertise in this sport
        List<Integer> employeeSports = expertiseDAO.getSportIdsByEmployee(employeeId);
        
        if (!employeeSports.contains(sportId)) {
            return "Employee does not have expertise in this sport";
        }
        
        return null; // Employee is qualified
    }
    
    /**
     * Validate all assignment criteria
     * @param employeeId Employee to assign
     * @param gameScheduleId Game schedule to assign to
     * @return Error message if any validation fails, null if all pass
     */
    public String validateAssignment(int employeeId, int gameScheduleId) throws SQLException {
        // Check for scheduling conflicts
        String conflictError = checkForConflicts(employeeId, gameScheduleId);
        if (conflictError != null) {
            return conflictError;
        }
        
        // Check availability
        String availabilityError = checkAvailability(employeeId, gameScheduleId);
        if (availabilityError != null) {
            return availabilityError;
        }
        
        // Check sport expertise
        String expertiseError = checkSportExpertise(employeeId, gameScheduleId);
        if (expertiseError != null) {
            return expertiseError;
        }
        
        return null; // All validations passed
    }
    
    /**
     * Helper: Check if two time ranges overlap
     */
    private boolean timesOverlap(LocalTime start1, LocalTime end1, 
                                  LocalTime start2, LocalTime end2) {
        // Ranges overlap if one starts before the other ends and vice versa
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
    
    /**
     * Helper: Check if a time range is within another range
     */
    private boolean timeWithinRange(LocalTime checkStart, LocalTime checkEnd,
                                     LocalTime rangeStart, LocalTime rangeEnd) {
        return !checkStart.isBefore(rangeStart) && !checkEnd.isAfter(rangeEnd);
    }
    
    /**
     * Helper: Determine season from date
     */
    private Availability.Season getSeason(LocalDate date) {
        int month = date.getMonthValue();
        if (month >= 9 && month <= 12) {
            return Availability.Season.FALL;
        } else if (month >= 1 && month <= 5) {
            return Availability.Season.SPRING;
        } else {
            return Availability.Season.SUMMER;
        }
    }
}
