package com.intramural.scheduling;

import com.intramural.scheduling.view.EmployeesPage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestEmployees extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setTitle("Employee Scheduling System - Employees");
            
            EmployeesPage employeesPage = new EmployeesPage(primaryStage, "Admin");
            Scene scene = employeesPage.createScene();
            
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}