package com.intramural.scheduling.dao;

import com.intramural.scheduling.model.Availability;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityDAO {
    
    /**
     * Insert new seasonal availability
     */
    public void insert(Availability.Seasonal availability) throws SQLException {
        String sql = "INSERT INTO seasonal_availability " +
                    "(employee_id, season, year, day_of_week, start_time, end_time, is_preferred) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, availability.getEmployeeId());
            stmt.setString(2, availability.getSeason().toString());
            stmt.setInt(3, availability.getYear());
            stmt.setString(4, availability.getDayOfWeek().toString());
            stmt.setTime(5, Time.valueOf(availability.getStartTime()));
            stmt.setTime(6, Time.valueOf(availability.getEndTime()));
            stmt.setInt(7, availability.isPreferred() ? 1 : 0);  // Use 1/0 for BIT
            
            stmt.executeUpdate();
            
            // Get generated availability_id
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    availability.setAvailabilityId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    /**
     * Get availability by employee, season, and year
     */
    public List<Availability.Seasonal> getByEmployee(int employeeId, 
                                                     Availability.Season season, 
                                                     int year) throws SQLException {
        List<Availability.Seasonal> availabilities = new ArrayList<>();
        String sql = "SELECT * FROM seasonal_availability " +
                    "WHERE employee_id = ? AND season = ? AND year = ? " +
                    "ORDER BY CASE day_of_week " +
                    "WHEN 'MONDAY' THEN 1 " +
                    "WHEN 'TUESDAY' THEN 2 " +
                    "WHEN 'WEDNESDAY' THEN 3 " +
                    "WHEN 'THURSDAY' THEN 4 " +
                    "WHEN 'FRIDAY' THEN 5 " +
                    "WHEN 'SATURDAY' THEN 6 " +
                    "WHEN 'SUNDAY' THEN 7 END, start_time";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            stmt.setString(2, season.toString());
            stmt.setInt(3, year);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    availabilities.add(extractAvailabilityFromResultSet(rs));
                }
            }
        }
        
        return availabilities;
    }
    
    /**
     * Get all availability for an employee (all seasons)
     */
    public List<Availability.Seasonal> getAllByEmployee(int employeeId) throws SQLException {
        List<Availability.Seasonal> availabilities = new ArrayList<>();
        String sql = "SELECT * FROM seasonal_availability " +
                    "WHERE employee_id = ? " +
                    "ORDER BY year DESC, season, CASE day_of_week " +
                    "WHEN 'MONDAY' THEN 1 " +
                    "WHEN 'TUESDAY' THEN 2 " +
                    "WHEN 'WEDNESDAY' THEN 3 " +
                    "WHEN 'THURSDAY' THEN 4 " +
                    "WHEN 'FRIDAY' THEN 5 " +
                    "WHEN 'SATURDAY' THEN 6 " +
                    "WHEN 'SUNDAY' THEN 7 END";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    availabilities.add(extractAvailabilityFromResultSet(rs));
                }
            }
        }
        
        return availabilities;
    }
    
    /**
     * Delete availability by employee, season, and year
     */
    public void deleteByEmployeeAndSeason(int employeeId, 
                                         Availability.Season season, 
                                         int year) throws SQLException {
        String sql = "DELETE FROM seasonal_availability " +
                    "WHERE employee_id = ? AND season = ? AND year = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            stmt.setString(2, season.toString());
            stmt.setInt(3, year);
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Delete specific availability by ID
     */
    public void delete(int availabilityId) throws SQLException {
        String sql = "DELETE FROM seasonal_availability WHERE availability_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, availabilityId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Update availability
     */
    public void update(Availability.Seasonal availability) throws SQLException {
        String sql = "UPDATE seasonal_availability SET " +
                    "day_of_week = ?, start_time = ?, end_time = ?, is_preferred = ? " +
                    "WHERE availability_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, availability.getDayOfWeek().toString());
            stmt.setTime(2, Time.valueOf(availability.getStartTime()));
            stmt.setTime(3, Time.valueOf(availability.getEndTime()));
            stmt.setInt(4, availability.isPreferred() ? 1 : 0);  // Use 1/0 for BIT
            stmt.setInt(5, availability.getAvailabilityId());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Insert permanent conflict
     */
    public void insertConflict(Availability.PermanentConflict conflict) throws SQLException {
        String sql = "INSERT INTO permanent_conflicts " +
                    "(employee_id, conflict_type, day_of_week, start_time, end_time, description) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, conflict.getEmployeeId());
            stmt.setString(2, conflict.getConflictType().toString());
            stmt.setString(3, conflict.getDayOfWeek().toString());
            stmt.setTime(4, Time.valueOf(conflict.getStartTime()));
            stmt.setTime(5, Time.valueOf(conflict.getEndTime()));
            stmt.setString(6, conflict.getDescription());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    conflict.setConflictId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    /**
     * Get permanent conflicts by employee
     */
    public List<Availability.PermanentConflict> getConflictsByEmployee(int employeeId) 
            throws SQLException {
        List<Availability.PermanentConflict> conflicts = new ArrayList<>();
        String sql = "SELECT * FROM permanent_conflicts WHERE employee_id = ? " +
                    "ORDER BY CASE day_of_week " +
                    "WHEN 'MONDAY' THEN 1 " +
                    "WHEN 'TUESDAY' THEN 2 " +
                    "WHEN 'WEDNESDAY' THEN 3 " +
                    "WHEN 'THURSDAY' THEN 4 " +
                    "WHEN 'FRIDAY' THEN 5 " +
                    "WHEN 'SATURDAY' THEN 6 " +
                    "WHEN 'SUNDAY' THEN 7 END, start_time";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    conflicts.add(extractConflictFromResultSet(rs));
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * Delete permanent conflict
     */
    public void deleteConflict(int conflictId) throws SQLException {
        String sql = "DELETE FROM permanent_conflicts WHERE conflict_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, conflictId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Extract Availability.Seasonal from ResultSet
     */
    private Availability.Seasonal extractAvailabilityFromResultSet(ResultSet rs) 
            throws SQLException {
        
        Availability.Seasonal availability = new Availability.Seasonal(
            rs.getInt("employee_id"),
            Availability.Season.valueOf(rs.getString("season")),
            rs.getInt("year"),
            DayOfWeek.valueOf(rs.getString("day_of_week")),
            rs.getTime("start_time").toLocalTime(),
            rs.getTime("end_time").toLocalTime()
        );
        
        availability.setAvailabilityId(rs.getInt("availability_id"));
        availability.setPreferred(rs.getBoolean("is_preferred"));
        
        return availability;
    }
    
    /**
     * Extract Availability.PermanentConflict from ResultSet
     */
    private Availability.PermanentConflict extractConflictFromResultSet(ResultSet rs) 
            throws SQLException {
        
        Availability.PermanentConflict conflict = new Availability.PermanentConflict(
            rs.getInt("employee_id"),
            Availability.ConflictType.valueOf(rs.getString("conflict_type")),
            DayOfWeek.valueOf(rs.getString("day_of_week")),
            rs.getTime("start_time").toLocalTime(),
            rs.getTime("end_time").toLocalTime(),
            rs.getString("description")
        );
        
        conflict.setConflictId(rs.getInt("conflict_id"));
        
        return conflict;
    }
}