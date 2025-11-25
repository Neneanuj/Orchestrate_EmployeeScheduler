package com.intramural.scheduling.controller;

import com.intramural.scheduling.dao.EmployeeDAO;
import com.intramural.scheduling.dao.GameScheduleDAO;
import com.intramural.scheduling.model.Employee;
import com.intramural.scheduling.model.Schedule;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardController {
    private EmployeeDAO employeeDAO;
    private GameScheduleDAO gameScheduleDAO;
    
    public AdminDashboardController() {
        this.employeeDAO = new EmployeeDAO();
        this.gameScheduleDAO = new GameScheduleDAO();
    }
    
    public Map<String, Object> getDashboardStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        // Get employee counts
        List<Employee> allEmployees = employeeDAO.getAll();
        List<Employee> activeEmployees = employeeDAO.getAllActive();
        
        stats.put("totalEmployees", allEmployees.size());
        stats.put("activeEmployees", activeEmployees.size());
        
        long supervisors = activeEmployees.stream()
            .filter(Employee::isSupervisorEligible)
            .count();
        stats.put("supervisors", supervisors);
        
        // Get current week's games
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        List<Schedule.Game> weekGames = gameScheduleDAO.getByDateRange(weekStart, weekEnd);
        stats.put("currentWeekGames", weekGames.size());
        
        return stats;
    }
    
    public List<Employee> getAllEmployees() throws SQLException {
        return employeeDAO.getAll();
    }
    
    public List<Employee> getActiveEmployees() throws SQLException {
        return employeeDAO.getAllActive();
    }
}