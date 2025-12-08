package com.intramural.scheduling.dao;

import com.intramural.scheduling.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    
    /**
     * Insert new user
     */
    public void insert(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // BUG-F019: Normalize username to lowercase for consistency
            stmt.setString(1, user.getUsername().toLowerCase());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole().name());  // Use .name() for enum
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    /**
     * Update user
     */
    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, password_hash = ?, role = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // BUG-F019: Normalize username to lowercase
            stmt.setString(1, user.getUsername().toLowerCase());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole().name());
            stmt.setInt(4, user.getUserId());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Find user by username
     */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // BUG-F019: Normalize username to lowercase for case-insensitive search
            stmt.setString(1, username.toLowerCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUser(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Find user by ID
     */
    public User findById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUser(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // BUG-F019: Normalize username to lowercase
            stmt.setString(1, username.toLowerCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * REQ-007: Delete user by ID
     * @param userId User ID to delete
     * @return true if user was deleted
     */
    public boolean delete(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Extract User from ResultSet
     */
    private User extractUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            User.UserRole.valueOf(rs.getString("role")),
            ""  // No email in simplified schema
        );
    }
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(extractUser(rs));
            }
        }
        return users;
    }
    
    /**
     * Create new user with optional email
     */
    public void createUser(User user, String email) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, email) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getUsername().toLowerCase());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole().name());
            stmt.setString(4, email);
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    /**
     * Update user
     */
    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, password_hash = ?, role = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername().toLowerCase());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole().name());
            stmt.setInt(4, user.getUserId());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Delete user
     */
    public void deleteUser(int userId) throws SQLException {
        // First check if user has associated employee record
        String checkSql = "SELECT employee_id, active_status FROM employees WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            Integer employeeId = null;
            boolean isActive = false;
            
            // Check for employee association
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    employeeId = rs.getInt("employee_id");
                    isActive = rs.getBoolean("active_status");
                }
            }
            
            // If employee exists and is active, block deletion
            if (employeeId != null && isActive) {
                throw new SQLException("Cannot delete user: User has an active employee record. Please deactivate the employee first.");
            }
            
            // If employee exists but is inactive, delete the employee record and its dependencies
            if (employeeId != null && !isActive) {
                // Delete employee-related records in correct order (to avoid FK constraint violations)
                // First, handle shifts with multiple FK references
                String updateShiftsRecommendations = "UPDATE shifts SET recommendation_a_id = NULL, recommendation_b_id = NULL WHERE recommendation_a_id = ? OR recommendation_b_id = ?";
                String deleteShifts = "DELETE FROM shifts WHERE assigned_employee_id = ?";
                String deletePermanentConflicts = "DELETE FROM permanent_conflicts WHERE employee_id = ?";
                String deleteTimeOff = "DELETE FROM time_off_requests WHERE employee_id = ?";
                String deleteWeeklyHours = "DELETE FROM weekly_hours WHERE employee_id = ?";
                String deleteExpertise = "DELETE FROM employee_expertise WHERE employee_id = ?";
                String deleteAvailability = "DELETE FROM seasonal_availability WHERE employee_id = ?";
                String deleteEmployee = "DELETE FROM employees WHERE employee_id = ?";
                
                try (PreparedStatement stmtUpdate = conn.prepareStatement(updateShiftsRecommendations);
                     PreparedStatement stmt0 = conn.prepareStatement(deleteShifts);
                     PreparedStatement stmt1 = conn.prepareStatement(deletePermanentConflicts);
                     PreparedStatement stmt2 = conn.prepareStatement(deleteTimeOff);
                     PreparedStatement stmt3 = conn.prepareStatement(deleteWeeklyHours);
                     PreparedStatement stmt4 = conn.prepareStatement(deleteExpertise);
                     PreparedStatement stmt5 = conn.prepareStatement(deleteAvailability);
                     PreparedStatement stmt6 = conn.prepareStatement(deleteEmployee)) {
                    
                    // First update recommendations to NULL
                    stmtUpdate.setInt(1, employeeId);
                    stmtUpdate.setInt(2, employeeId);
                    stmtUpdate.executeUpdate();
                    
                    // Then delete shifts assigned to this employee
                    stmt0.setInt(1, employeeId);
                    stmt0.executeUpdate();
                    
                    // Delete permanent conflicts
                    stmt1.setInt(1, employeeId);
                    stmt1.executeUpdate();
                    
                    // Delete time off requests
                    stmt2.setInt(1, employeeId);
                    stmt2.executeUpdate();
                    
                    // Delete weekly hours
                    stmt3.setInt(1, employeeId);
                    stmt3.executeUpdate();
                    
                    // Delete expertise
                    stmt4.setInt(1, employeeId);
                    stmt4.executeUpdate();
                    
                    // Delete availability
                    stmt5.setInt(1, employeeId);
                    stmt5.executeUpdate();
                    
                    // Finally delete employee
                    stmt6.setInt(1, employeeId);
                    stmt6.executeUpdate();
                }
            }
            
            // Delete the user
            String deleteSql = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, userId);
                int rowsAffected = deleteStmt.executeUpdate();
                
                if (rowsAffected == 0) {
                    throw new SQLException("User not found with ID: " + userId);
                }
            }
        }
    }
}
