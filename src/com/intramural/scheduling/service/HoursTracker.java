package com.intramural.scheduling.service;

import com.intramural.scheduling.dao.WeeklyHoursDAO;
import com.intramural.scheduling.model.Employee;
import com.intramural.scheduling.model.Schedule;
import com.intramural.scheduling.model.Tracking;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HoursTracker {
    private WeeklyHoursDAO weeklyHoursDAO;
    
    public HoursTracker() {
        this.weeklyHoursDAO = new WeeklyHoursDAO();
    }
    
    /**
     * Update weekly hours when a shift is assigned
     */
    public void assignShift(int employeeId, Schedule.Game game) throws SQLException {
        LocalDate weekStart = getWeekStartDate(game.getGameDate());
        Tracking.WeeklyHours weeklyHours = 
            weeklyHoursDAO.getByEmployeeAndWeek(employeeId, weekStart);
        
        double hours = game.getDurationHours();
        weeklyHours.addScheduledHours(hours);
        weeklyHoursDAO.update(weeklyHours);
    }
    
    /**
     * Update weekly hours when a shift is unassigned
     */
    public void unassignShift(int employeeId, Schedule.Game game) throws SQLException {
        LocalDate weekStart = getWeekStartDate(game.getGameDate());
        Tracking.WeeklyHours weeklyHours = 
            weeklyHoursDAO.getByEmployeeAndWeek(employeeId, weekStart);
        
        double hours = game.getDurationHours();
        weeklyHours.removeScheduledHours(hours);
        weeklyHoursDAO.update(weeklyHours);
    }
    
    /**
     * Get weekly hours for an employee
     */
    public Tracking.WeeklyHours getWeeklyHours(int employeeId, LocalDate date) 
            throws SQLException {
        LocalDate weekStart = getWeekStartDate(date);
        return weeklyHoursDAO.getByEmployeeAndWeek(employeeId, weekStart);
    }
    
    /**
     * Calculate total hours for a list of games
     */
    public double calculateTotalHours(List<Schedule.Game> games) {
        return games.stream()
            .mapToDouble(Schedule.Game::getDurationHours)
            .sum();
    }
    
    /**
     * Check if employee can accommodate additional hours
     */
    public boolean canAccommodateHours(int employeeId,
                                      LocalDate date,
                                      double additionalHours, 
                                      int maxHours) throws SQLException {
        Tracking.WeeklyHours weeklyHours = getWeeklyHours(employeeId, date);
        return weeklyHours.canAccommodate(additionalHours, maxHours);
    }
    
    /**
     * Get employees approaching their hour limit
     */
    public List<Employee> getEmployeesApproachingLimit(
            List<Employee> employees,
            LocalDate weekDate,
            double threshold) throws SQLException {
        
        List<Employee> approaching = new ArrayList<>();
        
        for (Employee emp : employees) {
            Tracking.WeeklyHours hours = getWeeklyHours(emp.getEmployeeId(), weekDate);
            if (hours != null && 
                hours.isApproachingLimit(emp.getMaxHoursPerWeek(), threshold)) {
                approaching.add(emp);
            }
        }
        
        return approaching;
    }
    
    /**
     * Get week start date for a given date (weeks start on Monday)
     */
    public static LocalDate getWeekStartDate(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }
    
    /**
     * Get hours summary for multiple employees
     */
    public Map<Integer, Tracking.WeeklyHours> getHoursSummary(
            List<Integer> employeeIds, 
            LocalDate weekDate) throws SQLException {
        
        LocalDate weekStart = getWeekStartDate(weekDate);
        
        return employeeIds.stream()
            .collect(Collectors.toMap(
                empId -> empId,
                empId -> {
                    try {
                        return weeklyHoursDAO.getByEmployeeAndWeek(empId, weekStart);
                    } catch (SQLException e) {
                        return new Tracking.WeeklyHours(empId, weekStart);
                    }
                }
            ));
    }
    
    /**
     * Reset hours for a new week (usually run automatically)
     */
    public void initializeWeekForEmployee(int employeeId, LocalDate weekStart) 
            throws SQLException {
        // Check if hours already exist
        Tracking.WeeklyHours existing = 
            weeklyHoursDAO.getByEmployeeAndWeek(employeeId, weekStart);
        
        // Will be created automatically if doesn't exist
    }
    
    /**
     * Get utilization percentage for an employee
     */
    public double getUtilization(int employeeId, LocalDate date, int maxHours) 
            throws SQLException {
        Tracking.WeeklyHours hours = getWeeklyHours(employeeId, date);
        if (hours == null || maxHours == 0) {
            return 0.0;
        }
        return (hours.getTotalScheduledHours() / maxHours) * 100.0;
    }
}