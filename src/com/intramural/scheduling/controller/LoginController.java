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
     * Validate credentials format - SIMPLIFIED (no constraints)
     */
    public String validateCredentials(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return "Username is required";
        }
        if (password == null || password.trim().isEmpty()) {
            return "Password is required";
        }
        // NO minimum length requirements - accept any password!
        return null; // Valid
    }
}