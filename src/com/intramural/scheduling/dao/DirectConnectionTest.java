package com.intramural.scheduling.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DirectConnectionTest {
    public static void main(String[] args) {
        System.out.println("=== Direct SQL Server Connection Test ===\n");
        
        // Try multiple connection strings
        String[] connectionStrings = {
            // Option 1: Simple server name
            "jdbc:sqlserver://DESKTOP-UDDCF59;databaseName=Emploment shift scheduling;integratedSecurity=true;encrypt=false",
            
            // Option 2: Localhost
            "jdbc:sqlserver://localhost;databaseName=Emploment shift scheduling;integratedSecurity=true;encrypt=false",
            
            // Option 3: With default port
            "jdbc:sqlserver://DESKTOP-UDDCF59:1433;databaseName=Emploment shift scheduling;integratedSecurity=true;encrypt=false;trustServerCertificate=true",
            
            // Option 4: 127.0.0.1
            "jdbc:sqlserver://127.0.0.1:1433;databaseName=Emploment shift scheduling;integratedSecurity=true;encrypt=false;trustServerCertificate=true"
        };
        
        // Load driver
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("✓ SQL Server JDBC Driver loaded\n");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ Driver not found: " + e.getMessage());
            return;
        }
        
        // Try each connection string
        for (int i = 0; i < connectionStrings.length; i++) {
            String url = connectionStrings[i];
            System.out.println("Attempt " + (i + 1) + ":");
            System.out.println("URL: " + url);
            
            try (Connection conn = DriverManager.getConnection(url)) {
                if (conn != null && !conn.isClosed()) {
                    System.out.println("✓✓✓ SUCCESS! Connected to database! ✓✓✓");
                    System.out.println("Database: " + conn.getCatalog());
                    System.out.println("Schema: " + conn.getSchema());
                    System.out.println("\n=== CONNECTION SUCCESSFUL ===\n");
                    return; // Exit after first success
                }
            } catch (SQLException e) {
                System.err.println("✗ Failed: " + e.getMessage());
                System.out.println();
            }
        }
        
        System.err.println("\n=== ALL CONNECTION ATTEMPTS FAILED ===");
        System.err.println("Possible issues:");
        System.err.println("1. SQL Server service is not running");
        System.err.println("2. TCP/IP is not enabled in SQL Server Configuration Manager");
        System.err.println("3. Windows Firewall is blocking the connection");
        System.err.println("4. Database name is incorrect");
    }
}