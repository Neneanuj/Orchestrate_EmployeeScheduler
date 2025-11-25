package com.intramural.scheduling.view;

import com.intramural.scheduling.controller.LoginController;
import com.intramural.scheduling.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginView {
    private Stage primaryStage;
    private TextField usernameField;
    private PasswordField passwordField;
    private Label messageLabel;
    private LoginController loginController;

    public LoginView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.loginController = new LoginController();
    }

    public Scene createScene() {
        // Main container
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Center content
        VBox centerBox = createCenterContent();
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 1000, 700);
        return scene;
    }

    private VBox createCenterContent() {
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(50));
        vbox.setMaxWidth(450);
        vbox.setStyle("-fx-background-color: white; " +
                      "-fx-background-radius: 10; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");

        // Title
        Label titleLabel = new Label("Employee Scheduling System");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitleLabel = new Label("Intramural Sports Program");
        subtitleLabel.setFont(Font.font("Arial", 16));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");

        // Separator
        Separator separator = new Separator();
        separator.setMaxWidth(350);

        // Username field
        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefHeight(40);
        usernameField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5;");

        // Password field
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5;");

        // Message label (for errors/success)
        messageLabel = new Label("");
        messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        messageLabel.setWrapText(true);

        // Login button
        Button loginButton = new Button("LOGIN");
        loginButton.setPrefWidth(350);
        loginButton.setPrefHeight(45);
        loginButton.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );
        
        // Hover effect
        loginButton.setOnMouseEntered(e -> 
            loginButton.setStyle(
                "-fx-background-color: #2980b9; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
            )
        );
        loginButton.setOnMouseExited(e -> 
            loginButton.setStyle(
                "-fx-background-color: #3498db; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
            )
        );

        // Login button action
        loginButton.setOnAction(e -> handleLogin());
        
        // Allow Enter key to login
        passwordField.setOnAction(e -> handleLogin());

        // Forgot password link
        Hyperlink forgotPasswordLink = new Hyperlink("Forgot password?");
        forgotPasswordLink.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12px;");

        // Add all elements
        vbox.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            separator,
            new VBox(5, usernameLabel, usernameField),
            new VBox(5, passwordLabel, passwordField),
            messageLabel,
            loginButton,
            forgotPasswordLink
        );

        // Wrap in container to center it
        VBox container = new VBox(vbox);
        container.setAlignment(Pos.CENTER);
        return container;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Clear previous message
        messageLabel.setText("");

        // Validate input
        String validationError = loginController.validateCredentials(username, password);
        if (validationError != null) {
            showError(validationError);
            return;
        }

        // Attempt login
        User user = loginController.login(username, password);
        
        if (user != null) {
            showSuccess("Login successful!");
            
            // Small delay to show success message
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                
                // Switch to dashboard on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    openDashboard(user);
                });
            }).start();
        } else {
            showError("Invalid username or password");
        }
    }

    private void showError(String message) {
        messageLabel.setText("✗ " + message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
    }

    private void showSuccess(String message) {
        messageLabel.setText("✓ " + message);
        messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");
    }

    private void openDashboard(User user) {
        AdminDashboard dashboard = new AdminDashboard(primaryStage, user.getUsername(), user.getUserId());
        Scene dashboardScene = dashboard.createScene();
        primaryStage.setScene(dashboardScene);
        primaryStage.setTitle("Employee Scheduling System - Dashboard");
    }
}