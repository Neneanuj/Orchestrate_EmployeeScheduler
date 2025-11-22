package com.intramural.scheduling;

import com.intramural.scheduling.view.AnalyticsPage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestAnalytics extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setTitle("Employee Scheduling System - Analytics");
            
            AnalyticsPage analyticsPage = new AnalyticsPage(primaryStage, "Admin");
            Scene scene = analyticsPage.createScene();
            
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