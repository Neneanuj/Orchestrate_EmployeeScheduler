package com.intramural.scheduling.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DatabaseConnection {
    private static String url;
    private static String username;
    private static String password;
    private static String driver;

    static {
        try {
            Properties props = new Properties();
            
            // Load from classpath (src/main/resources)
            try (InputStream in = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream("application.properties")) {
                
                if (in == null) {
                    throw new IOException("application.properties not found in classpath");
                }
                
                props.load(in);
            }
            
            url = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");
            driver = props.getProperty("db.driver");
            
            // Validate properties
            if (url == null || username == null || password == null || driver == null) {
                throw new IllegalStateException("Missing required database properties");
            }
            
            // Load JDBC driver
            Class.forName(driver);
            
            System.out.println("✅ Database configuration loaded successfully");
            System.out.println("URL: " + url);
            System.out.println("Username: " + username);
            
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("Failed to load database configuration: " + e.getMessage());
        }
    }

    private DatabaseConnection() {
        // Utility class - prevent instantiation
    }

    /**
     * Get a database connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
    
    /**
     * Test the database connection
     * @return true if connection successful
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Database connection test successful!");
            System.out.println("Connected to: " + conn.getCatalog());
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Database connection test failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}