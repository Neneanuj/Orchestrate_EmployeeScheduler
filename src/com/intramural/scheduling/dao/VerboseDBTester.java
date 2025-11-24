package com.intramural.scheduling.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class VerboseDBTester {
    public static void main(String[] args) {
        System.out.println("VerboseDBTester: starting...\n");

        Properties props = new Properties();
        String configPath = Paths.get(System.getProperty("user.dir"), "resources", "config", "application.properties").toString();
        try (FileInputStream in = new FileInputStream(configPath)) {
            props.load(in);
        } catch (IOException e) {
            System.err.println("ERROR: cannot read properties file: " + configPath);
            e.printStackTrace();
            return;
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String pass = props.getProperty("db.password");
        String driver = props.getProperty("db.driver");

        System.out.println("Loaded DB config:");
        System.out.println("  url: " + url);
        System.out.println("  username: " + user);
        System.out.println("  driver: " + driver);
        System.out.println("  password: " + (pass == null || pass.isEmpty() ? "<empty>" : "<masked>"));

        try {
            if (driver != null && !driver.isEmpty()) {
                System.out.println("Loading driver class: " + driver);
                Class.forName(driver);
            }

            // Short login timeout to fail fast
            DriverManager.setLoginTimeout(10);

            System.out.println("Attempting DriverManager.getConnection(...) (timeout 10s)...");
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                if (conn != null && !conn.isClosed()) {
                    System.out.println("\n✓ SUCCESS: Connected to database");
                    try {
                        DatabaseMetaData md = conn.getMetaData();
                        System.out.println("Database product: " + md.getDatabaseProductName() + " " + md.getDatabaseProductVersion());
                        System.out.println("URL reported by driver: " + md.getURL());
                        System.out.println("User: " + md.getUserName());
                    } catch (SQLException me) {
                        System.out.println("Connected but failed to read metadata: " + me.getMessage());
                    }
                } else {
                    System.err.println("FAILED: Connection object was null or closed after getConnection");
                }
            }

        } catch (ClassNotFoundException e) {
            System.err.println("FAILED: Driver class not found: " + driver);
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("SQL Exception during connection: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState() + " VendorCode: " + e.getErrorCode());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Unexpected error: " + t.getMessage());
            t.printStackTrace();
        }
    }
}
