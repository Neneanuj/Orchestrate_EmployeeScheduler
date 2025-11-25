package com.intramural.scheduling.dao;

import com.intramural.scheduling.model.Employee;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeExpertiseDAO {
    
    /**
     * Add sport expertise for an employee
     */
    public void insert(int employeeId, int sportId, Employee.ExpertiseLevel level) 
            throws SQLException {
        String sql = "INSERT INTO employee_expertise (employee_id, sport_id, expertise_level) " +
                    "VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, employeeId);
            stmt.setInt(2, sportId);
            stmt.setString(3, level.toString());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Get all sports an employee can work in
     */
    public List<Integer> getSportIdsByEmployee(int employeeId) throws SQLException {
        List<Integer> sportIds = new ArrayList<>();
        String sql = "SELECT sport_id FROM employee_expertise WHERE employee_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sportIds.add(rs.getInt("sport_id"));
                }
            }
        }
        
        return sportIds;
    }
    
    /**
     * Get expertise level for a specific sport
     */
    public Employee.ExpertiseLevel getExpertiseLevel(int employeeId, int sportId) 
            throws SQLException {
        String sql = "SELECT expertise_level FROM employee_expertise " +
                    "WHERE employee_id = ? AND sport_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            stmt.setInt(2, sportId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Employee.ExpertiseLevel.valueOf(rs.getString("expertise_level"));
                }
            }
        }
        
        return Employee.ExpertiseLevel.BEGINNER;
    }
    
    /**
     * Get all employees for a specific sport
     */
    public List<Integer> getEmployeeIdsBySport(int sportId) throws SQLException {
        List<Integer> employeeIds = new ArrayList<>();
        String sql = "SELECT employee_id FROM employee_expertise WHERE sport_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sportId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    employeeIds.add(rs.getInt("employee_id"));
                }
            }
        }
        
        return employeeIds;
    }
    
    /**
     * Delete all expertise for an employee
     */
    public void deleteByEmployee(int employeeId) throws SQLException {
        String sql = "DELETE FROM employee_expertise WHERE employee_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Delete specific sport expertise
     */
    public void delete(int employeeId, int sportId) throws SQLException {
        String sql = "DELETE FROM employee_expertise WHERE employee_id = ? AND sport_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            stmt.setInt(2, sportId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Update expertise level
     */
    public void updateLevel(int employeeId, int sportId, Employee.ExpertiseLevel level) 
            throws SQLException {
        String sql = "UPDATE employee_expertise SET expertise_level = ? " +
                    "WHERE employee_id = ? AND sport_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, level.toString());
            stmt.setInt(2, employeeId);
            stmt.setInt(3, sportId);
            
            stmt.executeUpdate();
        }
    }
}