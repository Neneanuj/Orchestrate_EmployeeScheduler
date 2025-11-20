package com.intramural.scheduling;

import com.intramural.scheduling.view.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Set up the window
            primaryStage.setTitle("Employee Scheduling System - Login");
            
            // Create and show login view
            LoginView loginView = new LoginView(primaryStage);
            Scene scene = loginView.createScene();
            
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}