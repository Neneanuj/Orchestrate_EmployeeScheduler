package com.intramural.scheduling.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class DatabaseConnection {
	private static String url;
	private static String username;
	private static String password;
	private static String driver;

	static {
		try {
			Properties props = new Properties();
			String configPath = Paths.get(System.getProperty("user.dir"), "resources", "config", "application.properties").toString();
			try (FileInputStream in = new FileInputStream(configPath)) {
				props.load(in);
			}
			url = props.getProperty("db.url");
			username = props.getProperty("db.username");
			password = props.getProperty("db.password");
			driver = props.getProperty("db.driver");
			if (driver != null && !driver.isEmpty()) {
				Class.forName(driver);
			}
		} catch (IOException | ClassNotFoundException e) {
			throw new ExceptionInInitializerError("Failed to load database configuration: " + e.getMessage());
		}
	}

	private DatabaseConnection() {
		// Utility class
	}

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url, username, password);
	}
}
