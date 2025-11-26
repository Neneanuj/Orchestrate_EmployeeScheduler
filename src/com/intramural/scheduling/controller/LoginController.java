package com.intramural.scheduling.controller;

import com.intramural.scheduling.model.User;
import com.intramural.scheduling.service.AuthenticationService;
import java.sql.SQLException;

public class LoginController {
    private AuthenticationService authService;
    
    public LoginController() {
        this.authService = new AuthenticationService();
    }
    
    /**
     * Authenticate user with username and password
     */
    public User login(String username, String password) {
        try {
            return authService.login(username, password);
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        authService.logout();
    }
    
    /**
     * Get currently logged-in user
     */
    public User getCurrentUser() {
        return authService.getCurrentUser();
    }
    
    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return authService.isAuthenticated();
    }
    
    /**
     * Validate credentials format before attempting login
     */
    public String validateCredentials(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return "Username is required";
        }
        if (password == null || password.trim().isEmpty()) {
            return "Password is required";
        }
        
        // BUG-F008: Validate username format (alphanumeric, underscore, dash only)
        if (!username.matches("^[a-zA-Z0-9_-]{3,20}$")) {
            return "Username must be 3-20 characters (letters, numbers, underscore, dash only)";
        }
        
        // BUG-F009: Enforce minimum 8 characters for password
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        
        return null; // Valid
    }
}