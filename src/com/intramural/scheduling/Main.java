package com.intramural.scheduling;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Create UI components
        Label titleLabel = new Label("Intramural Scheduling System");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> System.out.println("Login clicked!"));
        
        // Layout
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 50px; -fx-alignment: center;");
        root.getChildren().addAll(titleLabel, loginButton);
        
        // Scene
        Scene scene = new Scene(root, 600, 400);
        
        // Load CSS (optional)
        // scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        // Stage
        primaryStage.setTitle("Intramural Scheduling");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}