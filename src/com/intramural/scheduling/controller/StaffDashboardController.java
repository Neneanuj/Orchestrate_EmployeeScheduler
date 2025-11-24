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
     * Get pending time-off requests for employee
     */
    public List<TimeOffRequest> getPendingTimeOffRequests(int employeeId) 
            throws SQLException {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusMonths(3);
        
        return timeOffDAO.getApprovedByEmployee(employeeId, today, futureDate)
            .stream()
            .filter(req -> req.getStatus() == TimeOffRequest.Status.PENDING)
            .collect(Collectors.toList());
    }
    
    /**
     * Get approved time-offs for employee
     */
    public List<TimeOffRequest> getApprovedTimeOffs(int employeeId) 
            throws SQLException {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusMonths(3);
        
        return timeOffDAO.getApprovedByEmployee(employeeId, today, futureDate)
            .stream()
            .filter(req -> req.getStatus() == TimeOffRequest.Status.APPROVED)
            .collect(Collectors.toList());
    }
    
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