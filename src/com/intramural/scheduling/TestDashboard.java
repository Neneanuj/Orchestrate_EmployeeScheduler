package com.intramural.scheduling;

import com.intramural.scheduling.view.AdminDashboard;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestDashboard extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Set up the window
            primaryStage.setTitle("Employee Scheduling System - Dashboard");
            
            // Create and show dashboard directly (no login)
            AdminDashboard dashboard = new AdminDashboard(primaryStage, "Admin");
            Scene scene = dashboard.createScene();
            
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMaximized(true);  // Open maximized to see full design
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}