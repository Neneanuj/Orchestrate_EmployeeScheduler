package com.intramural.scheduling.dao;

import com.intramural.scheduling.model.Schedule;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class GameScheduleDAO {
    
    /**
     * Insert new game schedule
     */
    public void insert(Schedule.Game game) throws SQLException {
        String sql = "INSERT INTO game_schedules " +
                    "(sport_id, game_date, start_time, end_time, location, " +
                    "required_supervisors, required_referees, schedule_cycle_start, " +
                    "schedule_cycle_end, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, game.getSportId());
            stmt.setDate(2, Date.valueOf(game.getGameDate()));
            stmt.setTime(3, Time.valueOf(game.getStartTime()));
            stmt.setTime(4, Time.valueOf(game.getEndTime()));
            stmt.setString(5, game.getLocation());
            stmt.setInt(6, game.getRequiredSupervisors());
            stmt.setInt(7, game.getRequiredReferees());
            stmt.setDate(8, Date.valueOf(game.getScheduleCycleStart()));
            stmt.setDate(9, Date.valueOf(game.getScheduleCycleEnd()));
            stmt.setInt(10, game.getCreatedBy());
            
            stmt.executeUpdate();
            
            // Get generated schedule_id
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    game.setScheduleId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    /**
     * Get game schedule by ID
     */
    public Schedule.Game getById(int scheduleId) throws SQLException {
        String sql = "SELECT * FROM game_schedules WHERE schedule_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, scheduleId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractGameFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get games by cycle date range
     */
    public List<Schedule.Game> getByCycle(LocalDate cycleStart, LocalDate cycleEnd) 
            throws SQLException {
        List<Schedule.Game> games = new ArrayList<>();
        String sql = "SELECT * FROM game_schedules " +
                    "WHERE schedule_cycle_start = ? AND schedule_cycle_end = ? " +
                    "ORDER BY game_date, start_time";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(cycleStart));
            stmt.setDate(2, Date.valueOf(cycleEnd));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    games.add(extractGameFromResultSet(rs));
                }
            }
        }
        
        return games;
    }
    
    /**
     * Get games by date range (regardless of cycle)
     */
    public List<Schedule.Game> getByDateRange(LocalDate startDate, LocalDate endDate) 
            throws SQLException {
        List<Schedule.Game> games = new ArrayList<>();
        String sql = "SELECT * FROM game_schedules " +
                    "WHERE game_date BETWEEN ? AND ? " +
                    "ORDER BY game_date, start_time";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    games.add(extractGameFromResultSet(rs));
                }
            }
        }
        
        return games;
    }
    
    /**
     * Get games for a specific date
     */
    public List<Schedule.Game> getByDate(LocalDate date) throws SQLException {
        List<Schedule.Game> games = new ArrayList<>();
        String sql = "SELECT * FROM game_schedules " +
                    "WHERE game_date = ? " +
                    "ORDER BY start_time";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(date));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    games.add(extractGameFromResultSet(rs));
                }
            }
        }
        
        return games;
    }
    
    /**
     * Get games by sport
     */
    public List<Schedule.Game> getBySport(int sportId) throws SQLException {
        List<Schedule.Game> games = new ArrayList<>();
        String sql = "SELECT * FROM game_schedules " +
                    "WHERE sport_id = ? " +
                    "ORDER BY game_date, start_time";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sportId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    games.add(extractGameFromResultSet(rs));
                }
            }
        }
        
        return games;
    }
    
    /**
     * Update game schedule
     */
    public void update(Schedule.Game game) throws SQLException {
        String sql = "UPDATE game_schedules SET " +
                    "sport_id = ?, game_date = ?, start_time = ?, end_time = ?, " +
                    "location = ?, required_supervisors = ?, required_referees = ? " +
                    "WHERE schedule_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, game.getSportId());
            stmt.setDate(2, Date.valueOf(game.getGameDate()));
            stmt.setTime(3, Time.valueOf(game.getStartTime()));
            stmt.setTime(4, Time.valueOf(game.getEndTime()));
            stmt.setString(5, game.getLocation());
            stmt.setInt(6, game.getRequiredSupervisors());
            stmt.setInt(7, game.getRequiredReferees());
            stmt.setInt(8, game.getScheduleId());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Delete game schedule
     */
    public void delete(int scheduleId) throws SQLException {
        String sql = "DELETE FROM game_schedules WHERE schedule_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, scheduleId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Get all game schedules
     */
    public List<Schedule.Game> getAll() throws SQLException {
        List<Schedule.Game> games = new ArrayList<>();
        String sql = "SELECT * FROM game_schedules ORDER BY game_date, start_time";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                games.add(extractGameFromResultSet(rs));
            }
        }
        
        return games;
    }
    
    /**
     * Get upcoming games (from today onwards)
     */
    public List<Schedule.Game> getUpcoming(int days) throws SQLException {
        List<Schedule.Game> games = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        
        String sql = "SELECT * FROM game_schedules " +
                    "WHERE game_date BETWEEN ? AND ? " +
                    "ORDER BY game_date, start_time";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(today));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    games.add(extractGameFromResultSet(rs));
                }
            }
        }
        
        return games;
    }
    
    /**
     * Extract Game object from ResultSet
     */
    private Schedule.Game extractGameFromResultSet(ResultSet rs) throws SQLException {
        Schedule.Game game = new Schedule.Game(
            rs.getInt("sport_id"),
            rs.getDate("game_date").toLocalDate(),
            rs.getTime("start_time").toLocalTime(),
            rs.getTime("end_time").toLocalTime(),
            rs.getString("location"),
            rs.getInt("required_supervisors"),
            rs.getInt("required_referees"),
            rs.getDate("schedule_cycle_start").toLocalDate(),
            rs.getDate("schedule_cycle_end").toLocalDate(),
            rs.getInt("created_by")
        );
        
        game.setScheduleId(rs.getInt("schedule_id"));
        
        return game;
    }
}