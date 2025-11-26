package com.intramural.scheduling.dao;

import com.intramural.scheduling.model.TimeOffRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimeOffDAO {
    
    /**
     * Get all time off requests for an employee
     */
    public List<TimeOffRequest> getTimeOffRequestsByEmployee(int employeeId) throws SQLException {
        List<TimeOffRequest> requests = new ArrayList<>();
        String query = "SELECT * FROM time_off_requests WHERE employee_id = ? ORDER BY start_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                requests.add(mapResultSetToTimeOffRequest(rs));
            }
        }
        return requests;
    }
    
    /**
     * Get pending time off requests for an employee
     */
    public List<TimeOffRequest> getPendingTimeOffRequests(int employeeId) throws SQLException {
        List<TimeOffRequest> requests = new ArrayList<>();
        String query = "SELECT * FROM time_off_requests WHERE employee_id = ? AND request_status = 'PENDING' ORDER BY submitted_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                requests.add(mapResultSetToTimeOffRequest(rs));
            }
        }
        return requests;
    }
    
    /**
     * Get all pending time off requests (for admin)
     */
    public List<TimeOffRequest> getAllPendingRequests() throws SQLException {
        List<TimeOffRequest> requests = new ArrayList<>();
        // BUG-004: Use PreparedStatement instead of Statement
        String query = "SELECT * FROM time_off_requests WHERE request_status = 'PENDING' ORDER BY submitted_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                requests.add(mapResultSetToTimeOffRequest(rs));
            }
        }
        return requests;
    }
    
    /**
     * Create a new time off request
     */
    public boolean createTimeOffRequest(TimeOffRequest request) throws SQLException {
        // BUG-010: Use Timestamp instead of GETDATE()
        String query = "INSERT INTO time_off_requests (employee_id, request_date, start_time, end_time, is_full_day, request_status, reason, submitted_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, request.getEmployeeId());
            stmt.setDate(2, java.sql.Date.valueOf(request.getRequestDate()));
            
            if (request.isFullDay()) {
                stmt.setNull(3, Types.TIME);
                stmt.setNull(4, Types.TIME);
            } else {
                stmt.setTime(3, java.sql.Time.valueOf(request.getStartTime()));
                stmt.setTime(4, java.sql.Time.valueOf(request.getEndTime()));
            }
            
            stmt.setBoolean(5, request.isFullDay());
            stmt.setString(6, request.getStatus().name());
            stmt.setString(7, request.getReason());
            stmt.setTimestamp(8, Timestamp.valueOf(request.getSubmittedAt()));
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Update time off request status
     */
    public boolean updateRequestStatus(int requestId, String status, int reviewedBy) throws SQLException {
        // BUG-010: Use Timestamp instead of GETDATE()
        String query = "UPDATE time_off_requests SET request_status = ?, reviewed_by = ?, reviewed_at = ? WHERE request_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, reviewedBy);
            stmt.setTimestamp(3, Timestamp.valueOf(java.time.LocalDateTime.now()));
            stmt.setInt(4, requestId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Delete a time off request
     */
    public boolean deleteTimeOffRequest(int requestId) throws SQLException {
        String query = "DELETE FROM time_off_requests WHERE request_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Map ResultSet to TimeOffRequest object
     */
    private TimeOffRequest mapResultSetToTimeOffRequest(ResultSet rs) throws SQLException {
        int employeeId = rs.getInt("employee_id");
        java.sql.Date requestDate = rs.getDate("request_date");
        boolean isFullDay = rs.getBoolean("is_full_day");
        String reason = rs.getString("reason");
        
        TimeOffRequest request;
        
        if (isFullDay) {
            request = new TimeOffRequest(employeeId, requestDate.toLocalDate(), true, reason);
        } else {
            java.sql.Time startTime = rs.getTime("start_time");
            java.sql.Time endTime = rs.getTime("end_time");
            request = new TimeOffRequest(employeeId, requestDate.toLocalDate(), 
                                        startTime.toLocalTime(), endTime.toLocalTime(), reason);
        }
        
        request.setRequestId(rs.getInt("request_id"));
        
        // These might be null
        int reviewedBy = rs.getInt("reviewed_by");
        if (!rs.wasNull()) {
            String status = rs.getString("request_status");
            if ("APPROVED".equals(status)) {
                request.approve(reviewedBy);
            } else if ("DENIED".equals(status)) {
                request.deny(reviewedBy);
            }
        }
        
        return request;
    }
}