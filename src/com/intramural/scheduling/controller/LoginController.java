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
        if (username.length() < 3) {
            return "Username must be at least 3 characters";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters";
        }
        return null; // Valid
    }
    
    /**
     * Change password for current user
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        try {
            return authService.changePassword(oldPassword, newPassword);
        } catch (SQLException e) {
            System.err.println("Password change error: " + e.getMessage());
            return false;
        }
    }
}