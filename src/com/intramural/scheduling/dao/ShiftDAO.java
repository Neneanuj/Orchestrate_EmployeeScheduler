package com.intramural.scheduling.dao;

import com.intramural.scheduling.model.Schedule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShiftDAO {
    
    /**
     * Insert new shift
     */
    public void insert(Schedule.Shift shift) throws SQLException {
        String sql = "INSERT INTO shifts (game_schedule_id, position_type, position_number, " +
                    "assigned_employee_id, recommendation_a_id, recommendation_b_id, assignment_status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, shift.getGameScheduleId());
            stmt.setString(2, shift.getPositionType().toString());
            stmt.setInt(3, shift.getPositionNumber());
            
            if (shift.getAssignedEmployeeId() != null) {
                stmt.setInt(4, shift.getAssignedEmployeeId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            
            if (shift.getRecommendationAId() != null) {
                stmt.setInt(5, shift.getRecommendationAId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            if (shift.getRecommendationBId() != null) {
                stmt.setInt(6, shift.getRecommendationBId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            
            stmt.setString(7, shift.getAssignmentStatus().toString());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    shift.setShiftId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    /**
     * Get shifts by game schedule ID
     */
    public List<Schedule.Shift> getByGameSchedule(int gameScheduleId) throws SQLException {
        List<Schedule.Shift> shifts = new ArrayList<>();
        String sql = "SELECT * FROM shifts WHERE game_schedule_id = ? " +
                    "ORDER BY position_type, position_number";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, gameScheduleId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    shifts.add(extractShiftFromResultSet(rs));
                }
            }
        }
        
        return shifts;
    }
    
    /**
     * Update shift - handles all updates (assignment, recommendations, status)
     */
    public void update(Schedule.Shift shift) throws SQLException {
        String sql = "UPDATE shifts SET assigned_employee_id = ?, " +
                    "recommendation_a_id = ?, recommendation_b_id = ?, " +
                    "assignment_status = ?, assigned_at = ? " +
                    "WHERE shift_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (shift.getAssignedEmployeeId() != null) {
                stmt.setInt(1, shift.getAssignedEmployeeId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            
            if (shift.getRecommendationAId() != null) {
                stmt.setInt(2, shift.getRecommendationAId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            
            if (shift.getRecommendationBId() != null) {
                stmt.setInt(3, shift.getRecommendationBId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setString(4, shift.getAssignmentStatus().toString());
            
            if (shift.getAssignedAt() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(shift.getAssignedAt()));
            } else {
                stmt.setNull(5, Types.TIMESTAMP);
            }
            
            stmt.setInt(6, shift.getShiftId());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Update only recommendations (Option A and B) - used by SchedulingController
     */
    public void updateRecommendations(int shiftId, int optionAId, int optionBId) 
            throws SQLException {
        String sql = "UPDATE shifts SET recommendation_a_id = ?, recommendation_b_id = ?, " +
                    "assignment_status = 'RECOMMENDED' WHERE shift_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, optionAId);
            stmt.setInt(2, optionBId);
            stmt.setInt(3, shiftId);
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Update only assignment (when admin selects an option) - used by SchedulingController
     * Version 1: Takes shiftId and employeeId separately
     */
    public void updateAssignment(int shiftId, int employeeId) throws SQLException {
        // BUG-010: Use Timestamp instead of SQL Server specific GETDATE()
        String sql = "UPDATE shifts SET assigned_employee_id = ?, " +
                    "assignment_status = 'ASSIGNED', assigned_at = ? " +
                    "WHERE shift_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(3, shiftId);
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Update only assignment - Version 2: Takes Shift object directly
     * Used by StaffDashboardController and other controllers
     */
    public void updateAssignment(Schedule.Shift shift) throws SQLException {
        if (shift.getAssignedEmployeeId() == null) {
            throw new IllegalArgumentException("Shift must have an assigned employee");
        }
        
        updateAssignment(shift.getShiftId(), shift.getAssignedEmployeeId());
    }
    
    /**
     * Get shift by ID
     */
    public Schedule.Shift getById(int shiftId) throws SQLException {
        String sql = "SELECT * FROM shifts WHERE shift_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, shiftId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractShiftFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Delete shifts by game schedule
     */
    public void deleteByGameSchedule(int gameScheduleId) throws SQLException {
        String sql = "DELETE FROM shifts WHERE game_schedule_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, gameScheduleId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Extract Shift from ResultSet
     */
    private Schedule.Shift extractShiftFromResultSet(ResultSet rs) throws SQLException {
        Schedule.Shift shift = new Schedule.Shift(
            rs.getInt("game_schedule_id"),
            Schedule.PositionType.valueOf(rs.getString("position_type")),
            rs.getInt("position_number")
        );
        
        shift.setShiftId(rs.getInt("shift_id"));
        
        int assignedId = rs.getInt("assigned_employee_id");
        if (!rs.wasNull()) {
            shift.assignEmployee(assignedId);
        }
        
        int recAId = rs.getInt("recommendation_a_id");
        if (!rs.wasNull()) {
            int recBId = rs.getInt("recommendation_b_id");
            shift.setRecommendations(recAId, recBId);
        }
        
        String status = rs.getString("assignment_status");
        if (status != null) {
            shift.setAssignmentStatus(Schedule.AssignmentStatus.valueOf(status));
        }
        
        return shift;
    }
}