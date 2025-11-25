package com.intramural.scheduling.dao;

import com.intramural.scheduling.model.Tracking;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WeeklyHoursDAO {
    
    /**
     * Insert new weekly hours record
     */
    public void insert(Tracking.WeeklyHours weeklyHours) throws SQLException {
        String sql = "INSERT INTO weekly_hours " +
                    "(employee_id, week_start_date, total_scheduled_hours) " +
                    "VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, weeklyHours.getEmployeeId());
            stmt.setDate(2, Date.valueOf(weeklyHours.getWeekStartDate()));
            stmt.setDouble(3, weeklyHours.getTotalScheduledHours());
            
            stmt.executeUpdate();
            
            // Get generated tracking_id
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    weeklyHours.setTrackingId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    /**
     * Get weekly hours by employee and week
     * If record doesn't exist, creates a new one
     */
    public Tracking.WeeklyHours getByEmployeeAndWeek(int employeeId, LocalDate weekStartDate) 
            throws SQLException {
        String sql = "SELECT * FROM weekly_hours " +
                    "WHERE employee_id = ? AND week_start_date = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            stmt.setDate(2, Date.valueOf(weekStartDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractWeeklyHoursFromResultSet(rs);
                } else {
                    // Create new record if doesn't exist
                    Tracking.WeeklyHours newRecord = new Tracking.WeeklyHours(
                        employeeId, weekStartDate
                    );
                    insert(newRecord);
                    return newRecord;
                }
            }
        }
    }
    
    /**
     * Get all weekly hours for an employee
     */
    public List<Tracking.WeeklyHours> getByEmployee(int employeeId) throws SQLException {
        List<Tracking.WeeklyHours> hoursList = new ArrayList<>();
        String sql = "SELECT * FROM weekly_hours " +
                    "WHERE employee_id = ? " +
                    "ORDER BY week_start_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    hoursList.add(extractWeeklyHoursFromResultSet(rs));
                }
            }
        }
        
        return hoursList;
    }
    
    /**
     * Get weekly hours for all employees for a specific week
     */
    public List<Tracking.WeeklyHours> getByWeek(LocalDate weekStartDate) throws SQLException {
        List<Tracking.WeeklyHours> hoursList = new ArrayList<>();
        String sql = "SELECT * FROM weekly_hours " +
                    "WHERE week_start_date = ? " +
                    "ORDER BY employee_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(weekStartDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    hoursList.add(extractWeeklyHoursFromResultSet(rs));
                }
            }
        }
        
        return hoursList;
    }
    
    /**
     * Update weekly hours
     */
    public void update(Tracking.WeeklyHours weeklyHours) throws SQLException {
        String sql = "UPDATE weekly_hours SET " +
                    "total_scheduled_hours = ?, last_updated = GETDATE() " +
                    "WHERE tracking_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, weeklyHours.getTotalScheduledHours());
            stmt.setInt(2, weeklyHours.getTrackingId());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Update only scheduled hours (when assigning/unassigning shifts)
     */
    public void updateScheduledHours(int employeeId, LocalDate weekStartDate, 
                                    double scheduledHours) throws SQLException {
        String sql = "UPDATE weekly_hours SET " +
                    "total_scheduled_hours = ?, last_updated = GETDATE() " +
                    "WHERE employee_id = ? AND week_start_date = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, scheduledHours);
            stmt.setInt(2, employeeId);
            stmt.setDate(3, Date.valueOf(weekStartDate));
            
            int rowsAffected = stmt.executeUpdate();
            
            // If no rows affected, record doesn't exist - create it
            if (rowsAffected == 0) {
                Tracking.WeeklyHours newRecord = new Tracking.WeeklyHours(
                    employeeId, weekStartDate
                );
                newRecord.setTotalScheduledHours(scheduledHours);
                insert(newRecord);
            }
        }
    }
    
    /**
     * Add hours to scheduled total
     */
    public void addScheduledHours(int employeeId, LocalDate weekStartDate, 
                                 double additionalHours) throws SQLException {
        Tracking.WeeklyHours hours = getByEmployeeAndWeek(employeeId, weekStartDate);
        hours.addScheduledHours(additionalHours);
        update(hours);
    }
    
    /**
     * Remove hours from scheduled total
     */
    public void removeScheduledHours(int employeeId, LocalDate weekStartDate, 
                                    double hoursToRemove) throws SQLException {
        Tracking.WeeklyHours hours = getByEmployeeAndWeek(employeeId, weekStartDate);
        hours.removeScheduledHours(hoursToRemove);
        update(hours);
    }
    
    /**
     * Delete weekly hours record
     */
    public void delete(int trackingId) throws SQLException {
        String sql = "DELETE FROM weekly_hours WHERE tracking_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, trackingId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Delete all records for an employee (when employee is deleted)
     */
    public void deleteByEmployee(int employeeId) throws SQLException {
        String sql = "DELETE FROM weekly_hours WHERE employee_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Get employees who are approaching their hour limit
     */
    public List<Tracking.WeeklyHours> getApproachingLimit(LocalDate weekStartDate, 
                                                          double threshold) 
            throws SQLException {
        List<Tracking.WeeklyHours> hoursList = new ArrayList<>();
        
        // Join with employees to get max_hours_per_week
        String sql = "SELECT wh.* FROM weekly_hours wh " +
                    "JOIN employees e ON wh.employee_id = e.employee_id " +
                    "WHERE wh.week_start_date = ? " +
                    "AND (wh.total_scheduled_hours / e.max_hours_per_week) >= ? " +
                    "ORDER BY (wh.total_scheduled_hours / e.max_hours_per_week) DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(weekStartDate));
            stmt.setDouble(2, threshold);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    hoursList.add(extractWeeklyHoursFromResultSet(rs));
                }
            }
        }
        
        return hoursList;
    }
    
    /**
     * Get total scheduled hours across all employees for a week
     */
    public double getTotalScheduledHours(LocalDate weekStartDate) throws SQLException {
        String sql = "SELECT SUM(total_scheduled_hours) as total " +
                    "FROM weekly_hours WHERE week_start_date = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(weekStartDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        
        return 0.0;
    }
    
    /**
     * Extract WeeklyHours object from ResultSet
     */
    private Tracking.WeeklyHours extractWeeklyHoursFromResultSet(ResultSet rs) 
            throws SQLException {
        
        Tracking.WeeklyHours weeklyHours = new Tracking.WeeklyHours(
            rs.getInt("employee_id"),
            rs.getDate("week_start_date").toLocalDate()
        );
        
        weeklyHours.setTrackingId(rs.getInt("tracking_id"));
        weeklyHours.setTotalScheduledHours(rs.getDouble("total_scheduled_hours"));
        // Note: total_worked_hours removed from simplified schema
        
        return weeklyHours;
    }
}