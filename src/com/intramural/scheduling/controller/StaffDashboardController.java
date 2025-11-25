package com.intramural.scheduling.controller;

import com.intramural.scheduling.dao.*;
import com.intramural.scheduling.model.*;
import com.intramural.scheduling.service.HoursTracker;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class StaffDashboardController {
    private EmployeeDAO employeeDAO;
    private GameScheduleDAO gameScheduleDAO;
    private WeeklyHoursDAO weeklyHoursDAO;
    private TimeOffDAO timeOffDAO;
    private HoursTracker hoursTracker;
    
    public StaffDashboardController() {
        this.employeeDAO = new EmployeeDAO();
        this.gameScheduleDAO = new GameScheduleDAO();
        this.weeklyHoursDAO = new WeeklyHoursDAO();
        this.timeOffDAO = new TimeOffDAO();
        this.hoursTracker = new HoursTracker();
    }
    
    /**
     * Get employee information
     */
    public Employee getEmployee(int employeeId) throws SQLException {
        return employeeDAO.getById(employeeId);
    }
    
    /**
     * Get upcoming shifts for employee
     */
    public List<Schedule.Game> getUpcomingShifts(int employeeId, int days) 
            throws SQLException {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        
        List<Schedule.Game> allGames = gameScheduleDAO.getByCycle(today, endDate);
        
        // Filter games where employee is assigned
        List<Schedule.Game> myShifts = new ArrayList<>();
        for (Schedule.Game game : allGames) {
            game.generateShifts();
            boolean isAssigned = game.getShifts().stream()
                .anyMatch(shift -> 
                    shift.getAssignedEmployeeId() != null &&
                    shift.getAssignedEmployeeId() == employeeId
                );
            if (isAssigned) {
                myShifts.add(game);
            }
        }
        
        return myShifts;
    }
    
    /**
     * Get weekly hours for employee
     */
    public Tracking.WeeklyHours getWeeklyHours(int employeeId) throws SQLException {
        return hoursTracker.getWeeklyHours(employeeId, LocalDate.now());
    }
    
    /**
     * Get hours remaining this week
     */
    public double getRemainingHours(int employeeId) throws SQLException {
        Employee employee = employeeDAO.getById(employeeId);
        Tracking.WeeklyHours hours = getWeeklyHours(employeeId);
        
        if (hours == null) {
            return employee.getMaxHoursPerWeek();
        }
        
        return hours.getRemainingHours(employee.getMaxHoursPerWeek());
    }
    
    /**
//     * Get pending time-off requests for employee
//     */
//    public List<TimeOffRequest> getPendingTimeOffRequests(int employeeId) 
//            throws SQLException {
//        LocalDate today = LocalDate.now();
//        LocalDate futureDate = today.plusMonths(3);
//        
//        return timeOffDAO.getApprovedByEmployee(employeeId, today, futureDate)
//            .stream()
//            .filter(req -> req.getStatus() == TimeOffRequest.Status.PENDING)
//            .collect(Collectors.toList());
//    }
//    
//    /**
//     * Get approved time-offs for employee
//     */
//    public List<TimeOffRequest> getApprovedTimeOffs(int employeeId) 
//            throws SQLException {
//        LocalDate today = LocalDate.now();
//        LocalDate futureDate = today.plusMonths(3);
//        
//        return timeOffDAO.getApprovedByEmployee(employeeId, today, futureDate)
//            .stream()
//            .filter(req -> req.getStatus() == TimeOffRequest.Status.APPROVED)
//            .collect(Collectors.toList());
//    }
    
    /**
     * Get staff dashboard statistics
     */
    public Map<String, Object> getDashboardStats(int employeeId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        // Upcoming shifts count
        List<Schedule.Game> upcoming = getUpcomingShifts(employeeId, 30);
        stats.put("upcomingShifts", upcoming.size());
        
        // Weekly hours
        Tracking.WeeklyHours hours = getWeeklyHours(employeeId);
        if (hours != null) {
            stats.put("scheduledHours", hours.getTotalScheduledHours());
            stats.put("workedHours", hours.getTotalWorkedHours());
        } else {
            stats.put("scheduledHours", 0.0);
            stats.put("workedHours", 0.0);
        }
        
        // Remaining hours
        stats.put("remainingHours", getRemainingHours(employeeId));
        
        // Pending requests
        stats.put("pendingRequests", getPendingTimeOffRequests(employeeId).size());
        
        return stats;
    }
    
    /**
     * Get available shifts for employee (unassigned shifts only)
     */
    public List<Schedule.Game> getAvailableShifts(int employeeId) throws SQLException {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(14);
        
        List<Schedule.Game> allGames = gameScheduleDAO.getByCycle(today, endDate);
        List<Schedule.Game> availableGames = new ArrayList<>();
        
        for (Schedule.Game game : allGames) {
            game.generateShifts();
            
            // Check if game has unassigned shifts
            boolean hasAvailableShifts = game.getShifts().stream()
                .anyMatch(shift -> shift.getAssignedEmployeeId() == null);
            
            // Check if employee is already assigned to this game
            boolean alreadyAssigned = game.getShifts().stream()
                .anyMatch(shift -> 
                    shift.getAssignedEmployeeId() != null &&
                    shift.getAssignedEmployeeId() == employeeId
                );
            
            // Only show games with available shifts where employee isn't assigned
            if (hasAvailableShifts && !alreadyAssigned) {
                // Check 20-hour limit
                Tracking.WeeklyHours hours = getWeeklyHours(employeeId);
                if (hours == null || hours.canAccommodate(game.getDurationHours(), 20)) {
                    availableGames.add(game);
                }
            }
        }
        
        return availableGames;
    }

    /**
     * Assign employee to a shift
     */
    public void assignShift(int employeeId, Schedule.Game game) throws SQLException {
        for (Schedule.Shift shift : game.getShifts()) {
            if (shift.getAssignedEmployeeId() == null) {
                // Check if employee meets requirements
                Employee employee = employeeDAO.getById(employeeId);
                if (shift.getPositionType() == Schedule.PositionType.SUPERVISOR && 
                    !employee.isSupervisorEligible()) {
                    continue; // Skip if not eligible for supervisor
                }
                
                // Assign the shift
                shift.assignEmployee(employeeId);
                
                // Update database
                ShiftDAO shiftDAO = new ShiftDAO();
                shiftDAO.updateAssignment(shift);
                
                // Update weekly hours
                hoursTracker.assignShift(employeeId, game);
                
                break;
            }
        }
    }
    
    /**
     * Get shift details for a specific game
     */
    public Schedule.Shift getMyShift(int employeeId, Schedule.Game game) {
        game.generateShifts();
        return game.getShifts().stream()
            .filter(shift -> 
                shift.getAssignedEmployeeId() != null &&
                shift.getAssignedEmployeeId() == employeeId
            )
            .findFirst()
            .orElse(null);
    }
}