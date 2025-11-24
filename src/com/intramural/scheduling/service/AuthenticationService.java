package com.intramural.scheduling.service;

import com.intramural.scheduling.dao.UserDAO;
import com.intramural.scheduling.model.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;

public class AuthenticationService {
    private UserDAO userDAO;
    private User currentUser;
    
    public AuthenticationService() {
        this.userDAO = new UserDAO();
        this.currentUser = null;
    }
    
    /**
     * Authenticate user with username and password
     * @return User object if successful, null otherwise
     */
    public User login(String username, String password) throws SQLException {
        User user = userDAO.findByUsername(username);
        
        if (user != null && verifyPassword(password, user.getPasswordHash())) {
            this.currentUser = user;
            return user;
        }
        
        return null;
    }
    
    /**
     * Log out current user
     */
    public void logout() {
        this.currentUser = null;
    }
    
    /**
     * Get currently logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return currentUser != null;
    }
    
    /**
     * Check if current user has admin privileges
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.Role.ADMIN;
    }
    
    /**
     * Check if current user has supervisor privileges
     */
    public boolean isSupervisor() {
        return currentUser != null && 
               (currentUser.getRole() == User.Role.ADMIN || 
                currentUser.getRole() == User.Role.SUPERVISOR);
    }
    
    /**
     * Hash password using SHA-256
     */
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    /**
     * Verify password against stored hash
     */
    private boolean verifyPassword(String password, String storedHash) {
        String hashedInput = hashPassword(password);
        return hashedInput.equals(storedHash);
    }
    
    /**
     * Change password for current user
     */
    public boolean changePassword(String oldPassword, String newPassword) 
            throws SQLException {
        if (currentUser == null) {
            return false;
        }
        
        if (!verifyPassword(oldPassword, currentUser.getPasswordHash())) {
            return false;
        }
        
        String newHash = hashPassword(newPassword);
        currentUser.setPasswordHash(newHash);
        userDAO.update(currentUser);
        
        return true;
    }
    
    /**
     * Register new user (admin only)
     */
    public User registerUser(String username, String password, String email, 
                            User.Role role) throws SQLException {
        // Check if username already exists
        if (userDAO.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        String passwordHash = hashPassword(password);
        User newUser = new User(0, username, passwordHash, role, email);
        userDAO.insert(newUser);
        
        return newUser;
    }
    
    /**
     * Validate password strength
     */
    public boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        
        return hasUpper && hasLower && hasDigit;
    }
}