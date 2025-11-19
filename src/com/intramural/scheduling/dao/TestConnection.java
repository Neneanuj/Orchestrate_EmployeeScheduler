package com.intramural.scheduling.dao;

import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("Testing SQL Server Connection...\n");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("✓ SUCCESS! Connected to SQL Server");
            System.out.println("✓ Database: Emploment shift scheduling");
        } catch (Exception e) {
            System.err.println("✗ FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
