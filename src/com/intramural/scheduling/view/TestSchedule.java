package com.intramural.scheduling.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestSchedule extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setTitle("Employee Scheduling System - Schedule");
            
            ScheduleBuilder schedulePage = new ScheduleBuilder(primaryStage, "Admin");
            Scene scene = schedulePage.createScene();
            
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