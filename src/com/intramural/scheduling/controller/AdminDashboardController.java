package com.intramural.scheduling.controller;

import com.intramural.scheduling.dao.*;
import com.intramural.scheduling.model.*;
import com.intramural.scheduling.service.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class AdminDashboardController {
    private EmployeeDAO employeeDAO;
    private GameScheduleDAO gameScheduleDAO;
    private TimeOffDAO timeOffDAO;
    private WeeklyHoursDAO weeklyHoursDAO;
    private AvailabilityService availabilityService;
    private HoursTracker hoursTracker;
    
    public AdminDashboardController() {
        this.employeeDAO = new EmployeeDAO();
        this.gameScheduleDAO = new GameScheduleDAO();
        this.timeOffDAO = new TimeOffDAO();
        this.weeklyHoursDAO = new WeeklyHoursDAO();
        this.availabilityService = new AvailabilityService();
        this.hoursTracker = new HoursTracker();
    }
    
    /**
     * Get dashboard statistics
     */
    public Map<String, Object> getDashboardStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        // Employee stats
        List<Employee> allEmployees = employeeDAO.getAllActive();
        stats.put("totalEmployees", allEmployees.size());
        
        long supervisors = allEmployees.stream()
            .filter(Employee::isSupervisorEligible)
            .count();
        stats.put("supervisors", supervisors);
        stats.put("referees", allEmployees.size() - supervisors);
        
        // Pending time-off requests
        List<TimeOffRequest> pendingRequests = timeOffDAO.getPendingRequests();
        stats.put("pendingTimeOff", pendingRequests.size());
        
        // Current week games
        LocalDate today = LocalDate.now();
        LocalDate weekStart = HoursTracker.getWeekStartDate(today);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        List<Schedule.Game> currentWeekGames = 
            gameScheduleDAO.getByCycle(weekStart, weekEnd);
        stats.put("currentWeekGames", currentWeekGames.size());
        
        return stats;
    }
    
    /**
     * Get all active employees
     */
    public List<Employee> getAllEmployees() throws SQLException {
        return employeeDAO.getAllActive();
    }
    
    /**
     * Get pending time-off requests
     */
    public List<TimeOffRequest> getPendingTimeOffRequests() throws SQLException {
        return timeOffDAO.getPendingRequests();
    }
    
    /**
     * Get upcoming games
     */
    public List<Schedule.Game> getUpcomingGames(int days) throws SQLException {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        return gameScheduleDAO.getByCycle(today, endDate);
    }
    
    /**
     * Get employee by ID
     */
    public Employee getEmployee(int employeeId) throws SQLException {
        return employeeDAO.getById(employeeId);
    }
    
    /**
     * Update employee information
     */
    public void updateEmployee(Employee employee) throws SQLException {
        employeeDAO.update(employee);
    }
    
    /**
     * Get employees approaching hour limit
     */
    public List<Employee> getEmployeesApproachingLimit() throws SQLException {
        List<Employee> allEmployees = employeeDAO.getAllActive();
        return hoursTracker.getEmployeesApproachingLimit(
            allEmployees, LocalDate.now(), 0.85
        );
    }
    
    /**
     * Get weekly hours summary for all employees
     */
    public Map<Integer, Tracking.WeeklyHours> getWeeklyHoursSummary() 
            throws SQLException {
        List<Employee> employees = employeeDAO.getAllActive();
        List<Integer> employeeIds = new ArrayList<>();
        for (Employee emp : employees) {
            employeeIds.add(emp.getEmployeeId());
        }
        
        return hoursTracker.getHoursSummary(employeeIds, LocalDate.now());
    }
}