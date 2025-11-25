package com.intramural.scheduling.dao;

import com.intramural.scheduling.model.Sport;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SportDAO {
    
    /**
     * Get all sports
     */
    public List<Sport> getAll() throws SQLException {
        List<Sport> sports = new ArrayList<>();
        String sql = "SELECT * FROM sports ORDER BY sport_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                sports.add(extractSportFromResultSet(rs));
            }
        }
        
        return sports;
    }
    
    /**
     * Get sport by ID
     */
    public Sport getById(int sportId) throws SQLException {
        String sql = "SELECT * FROM sports WHERE sport_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sportId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractSportFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Insert new sport
     */
    public void insert(Sport sport) throws SQLException {
        String sql = "INSERT INTO sports (sport_name, default_duration_minutes, " +
                    "required_supervisors, required_referees) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, sport.getSportName());
            stmt.setInt(2, sport.getDefaultDurationMinutes());
            stmt.setInt(3, sport.getRequiredSupervisors());
            stmt.setInt(4, sport.getRequiredReferees());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    // Note: Sport class doesn't have setSportId, so we can't set it
                    // Consider adding it if needed
                }
            }
        }
    }
    
    /**
     * Extract Sport from ResultSet
     */
    private Sport extractSportFromResultSet(ResultSet rs) throws SQLException {
        return new Sport(
            rs.getInt("sport_id"),
            rs.getString("sport_name"),
            rs.getInt("default_duration_minutes"),
            rs.getInt("required_supervisors"),
            rs.getInt("required_referees")
        );
    }
}