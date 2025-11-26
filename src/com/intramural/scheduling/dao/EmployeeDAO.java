package com.intramural.scheduling.dao;

import com.intramural.scheduling.model.Employee;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    
    // QA-003: Constants for boolean BIT field values
    private static final int BIT_TRUE = 1;
    private static final int BIT_FALSE = 0;
    
    public void insert(Employee employee) throws SQLException {
        // BUG-006: Add null check
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (employee.getFirstName() == null || employee.getLastName() == null) {
            throw new IllegalArgumentException("Employee first and last name are required");
        }
        
        String sql = "INSERT INTO employees (user_id, first_name, last_name, " +
                    "max_hours_per_week, is_supervisor_eligible, active_status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, employee.getUserId());
            stmt.setString(2, employee.getFirstName());
            stmt.setString(3, employee.getLastName());
            stmt.setInt(4, employee.getMaxHoursPerWeek());
            stmt.setInt(5, employee.isSupervisorEligible() ? BIT_TRUE : BIT_FALSE);
            stmt.setInt(6, employee.isActiveStatus() ? BIT_TRUE : BIT_FALSE);
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    employee.setEmployeeId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    public Employee getById(int employeeId) throws SQLException {
        String sql = "SELECT * FROM employees WHERE employee_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractEmployee(rs);
                }
            }
        }
        return null;
    }
    
    public List<Employee> getAllActive() throws SQLException {
        // BUG-006: Add null check
        List<Employee> employees = new ArrayList<>();
        // BUG-004: Use PreparedStatement instead of Statement
        String sql = "SELECT * FROM employees WHERE active_status = 1 " +
                    "ORDER BY last_name, first_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                employees.add(extractEmployee(rs));
            }
        }
        return employees;
    }
    
    public void update(Employee employee) throws SQLException {
        // BUG-006: Add null check
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (employee.getEmployeeId() <= 0) {
            throw new IllegalArgumentException("Invalid employee ID");
        }
        
        String sql = "UPDATE employees SET first_name = ?, last_name = ?, " +
                    "max_hours_per_week = ?, is_supervisor_eligible = ?, " +
                    "active_status = ? WHERE employee_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setInt(3, employee.getMaxHoursPerWeek());
            stmt.setInt(4, employee.isSupervisorEligible() ? BIT_TRUE : BIT_FALSE);
            stmt.setInt(5, employee.isActiveStatus() ? BIT_TRUE : BIT_FALSE);
            stmt.setInt(6, employee.getEmployeeId());
            
            stmt.executeUpdate();
        }
    }
    public List<Employee> getAll() throws SQLException {
        // BUG-006: Add null check
        List<Employee> employees = new ArrayList<>();
        // BUG-004: Use PreparedStatement instead of Statement
        String sql = "SELECT * FROM employees ORDER BY last_name, first_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                employees.add(extractEmployee(rs));
            }
        }
        return employees;
    }
    
    /**
     * BUG-F005: Check if employee with same first and last name exists
     * @param firstName First name to check
     * @param lastName Last name to check
     * @return true if employee with this name exists
     */
    public boolean nameExists(String firstName, String lastName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees WHERE " +
                    "LOWER(first_name) = LOWER(?) AND LOWER(last_name) = LOWER(?) AND active_status = 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, firstName.trim());
            stmt.setString(2, lastName.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    private Employee extractEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee(
            rs.getInt("employee_id"),
            rs.getInt("user_id"),
            rs.getString("first_name"),
            rs.getString("last_name")
        );
        
        employee.setMaxHoursPerWeek(rs.getInt("max_hours_per_week"));
        employee.setSupervisorEligible(rs.getBoolean("is_supervisor_eligible"));
        employee.setActiveStatus(rs.getBoolean("active_status"));
        
        return employee;
    }
}