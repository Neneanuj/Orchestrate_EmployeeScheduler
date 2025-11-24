package com.intramural.scheduling.model;

import java.time.*;
import java.util.*;


public class User {
	public enum UserRole {
	    ADMIN, SUPERVISOR, STAFF
	}

    private int userId;
    private String username;
    private String passwordHash;
    private UserRole role;
    private String email;
    private LocalDateTime createdAt;
    
    public User(int userId, String username, String passwordHash, 
                UserRole role, String email) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}