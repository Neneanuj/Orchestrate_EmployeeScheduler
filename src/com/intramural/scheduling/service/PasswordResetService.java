package com.intramural.scheduling.service;

import com.intramural.scheduling.dao.DatabaseConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

public class PasswordResetService {
    
    public String generateResetToken(String email) throws SQLException {
        // Check if user exists with this email
        String checkUserQuery = "SELECT user_id FROM Users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkUserQuery)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (!rs.next()) {
                return null; // User not found
            }
            
            int userId = rs.getInt("user_id");
            String token = UUID.randomUUID().toString();
            
            // Store token in database (expires in 1 hour)
            String insertTokenQuery = "INSERT INTO PasswordResetTokens " +
                "(user_id, reset_token, expiry_datetime, is_used) VALUES (?, ?, ?, 0)";
            
            try (PreparedStatement insertStmt = conn.prepareStatement(insertTokenQuery)) {
                insertStmt.setInt(1, userId);
                insertStmt.setString(2, token);
                insertStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusHours(1)));
                insertStmt.executeUpdate();
            }
            
            return token;
        }
    }
    
    public boolean validateToken(String token) throws SQLException {
        String query = "SELECT expiry_datetime, is_used FROM PasswordResetTokens " +
                      "WHERE reset_token = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();
            
            if (!rs.next()) {
                return false;
            }
            
            Timestamp expiry = rs.getTimestamp("expiry_datetime");
            boolean isUsed = rs.getBoolean("is_used");
            
            return !isUsed && expiry.after(new Timestamp(System.currentTimeMillis()));
        }
    }
    
    public boolean resetPassword(String token, String newPassword) throws SQLException {
        if (!validateToken(token)) {
            return false;
        }
        
        String getUserQuery = "SELECT user_id FROM PasswordResetTokens WHERE reset_token = ?";
        String updatePasswordQuery = "UPDATE Users SET password_hash = ? WHERE user_id = ?";
        String markTokenUsedQuery = "UPDATE PasswordResetTokens SET is_used = 1 WHERE reset_token = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                int userId;
                try (PreparedStatement pstmt = conn.prepareStatement(getUserQuery)) {
                    pstmt.setString(1, token);
                    ResultSet rs = pstmt.executeQuery();
                    if (!rs.next()) {
                        return false;
                    }
                    userId = rs.getInt("user_id");
                }
                
                // Hash the password (for now storing as-is, but should use BCrypt)
                String hashedPassword = hashPassword(newPassword);
                
                try (PreparedStatement pstmt = conn.prepareStatement(updatePasswordQuery)) {
                    pstmt.setString(1, hashedPassword);
                    pstmt.setInt(2, userId);
                    pstmt.executeUpdate();
                }
                
                try (PreparedStatement pstmt = conn.prepareStatement(markTokenUsedQuery)) {
                    pstmt.setString(1, token);
                    pstmt.executeUpdate();
                }
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
}
