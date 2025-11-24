package com.intramural.scheduling.dao;

import java.sql.Connection;
import java.sql.SQLException;

public class DBTest {
    public static void main(String[] args) {
        System.out.println("DBTest: attempting to get a connection...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("SUCCESS: Connected to database.");
            } else {
                System.out.println("FAIL: Connection is null or closed.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Error: " + t.getMessage());
            t.printStackTrace();
        }
    }
}
