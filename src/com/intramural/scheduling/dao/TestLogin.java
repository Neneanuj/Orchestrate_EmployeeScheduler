package com.intramural.scheduling.dao;

import com.intramural.scheduling.model.User;
import com.intramural.scheduling.service.AuthenticationService;

public class TestLogin {
    public static void main(String[] args) {
        System.out.println("Testing login...");
        
        try {
            // Test DB connection
            System.out.println("1. Testing database connection...");
            DatabaseConnection.getConnection();
            System.out.println("   ✓ Database connected");
            
            // Test finding user
            System.out.println("2. Testing UserDao.findByUsername...");
            UserDAO userDao = new UserDAO();
            User user = userDao.findByUsername("admin");
            if (user != null) {
                System.out.println("   ✓ User found: " + user.getUsername());
                System.out.println("   Password hash: " + user.getPasswordHash());
            } else {
                System.out.println("   ✗ User NOT found");
            }
            
            // Test authentication
            System.out.println("3. Testing authentication...");
            AuthenticationService authService = new AuthenticationService();
            User loggedIn = authService.login("admin", "admin123");
            
            if (loggedIn != null) {
                System.out.println("   ✓ LOGIN SUCCESS!");
                System.out.println("   User: " + loggedIn.getUsername());
                System.out.println("   Role: " + loggedIn.getRole());
            } else {
                System.out.println("   ✗ LOGIN FAILED");
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
