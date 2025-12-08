package com.intramural.scheduling.test;

import com.intramural.scheduling.dao.DatabaseConnection;
import java.sql.Connection;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Testing Database Connection...");
        System.out.println("========================================\n");
        
        try {
            System.out.println("Attempting to connect to database...");
            Connection conn = DatabaseConnection.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("\n✅ SUCCESS! Database connected successfully!");
                System.out.println("Database Product: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("Database Version: " + conn.getMetaData().getDatabaseProductVersion());
                System.out.println("Driver Name: " + conn.getMetaData().getDriverName());
                System.out.println("Connection URL: " + conn.getMetaData().getURL());
                
                conn.close();
                System.out.println("\n✅ Connection closed successfully");
            } else {
                System.out.println("\n❌ FAILED! Connection is null or closed");
            }
            
        } catch (Exception e) {
            System.out.println("\n❌ ERROR! Database connection failed!");
            System.out.println("Error Type: " + e.getClass().getName());
            System.out.println("Error Message: " + e.getMessage());
            System.out.println("\nPossible Issues:");
            System.out.println("1. Database name might be incorrect in application.properties");
            System.out.println("2. SQL Server service might not be running");
            System.out.println("3. Windows Authentication might not be configured");
            System.out.println("4. Database might not exist on the server");
            System.out.println("\nFull Stack Trace:");
            e.printStackTrace();
        }
        
        System.out.println("\n========================================");
        System.out.println("Test Complete");
        System.out.println("========================================");
    }
}
