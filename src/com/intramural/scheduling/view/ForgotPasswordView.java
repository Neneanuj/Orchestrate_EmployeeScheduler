package com.intramural.scheduling.view;

import com.intramural.scheduling.service.PasswordResetService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ForgotPasswordView {
    
    private Stage stage;
    private PasswordResetService resetService;
    
    public ForgotPasswordView(Stage stage) {
        this.stage = stage;
        this.resetService = new PasswordResetService();
    }
    
    public Scene createScene() {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        container.setMaxWidth(400);
        
        // Title
        Label titleLabel = new Label("Reset Password");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        // Instructions
        Label instructionsLabel = new Label("Enter your email address to receive a password reset token.");
        instructionsLabel.setWrapText(true);
        instructionsLabel.setFont(Font.font("Arial", 14));
        instructionsLabel.setTextFill(Color.web("#7f8c8d"));
        
        // Email field
        Label emailLabel = new Label("Email Address:");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        TextField emailField = new TextField();
        emailField.setPromptText("your.email@example.com");
        emailField.setStyle("-fx-padding: 10; -fx-font-size: 14px;");
        
        // Message label for feedback
        Label messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("Arial", 12));
        
        // Token display (for demo purposes)
        VBox tokenBox = new VBox(5);
        tokenBox.setVisible(false);
        Label tokenLabel = new Label("Your Reset Token:");
        tokenLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        TextArea tokenDisplay = new TextArea();
        tokenDisplay.setEditable(false);
        tokenDisplay.setWrapText(true);
        tokenDisplay.setPrefRowCount(3);
        tokenDisplay.setStyle("-fx-control-inner-background: #ecf0f1; -fx-font-family: monospace;");
        tokenBox.getChildren().addAll(tokenLabel, tokenDisplay);
        
        // Send token button
        Button sendTokenBtn = new Button("Send Reset Token");
        sendTokenBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                             "-fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand;");
        sendTokenBtn.setPrefWidth(200);
        sendTokenBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            
            if (email.isEmpty()) {
                messageLabel.setText("Please enter your email address.");
                messageLabel.setTextFill(Color.web("#e74c3c"));
                tokenBox.setVisible(false);
                return;
            }
            
            if (!isValidEmail(email)) {
                messageLabel.setText("Please enter a valid email address.");
                messageLabel.setTextFill(Color.web("#e74c3c"));
                tokenBox.setVisible(false);
                return;
            }
            
            try {
                String token = resetService.generateResetToken(email);
                
                if (token == null) {
                    messageLabel.setText("No account found with this email address.");
                    messageLabel.setTextFill(Color.web("#e74c3c"));
                    tokenBox.setVisible(false);
                } else {
                    messageLabel.setText("Reset token generated successfully!");
                    messageLabel.setTextFill(Color.web("#27ae60"));
                    tokenDisplay.setText(token);
                    tokenBox.setVisible(true);
                }
            } catch (Exception ex) {
                messageLabel.setText("Error: " + ex.getMessage());
                messageLabel.setTextFill(Color.web("#e74c3c"));
                tokenBox.setVisible(false);
            }
        });
        
        // Reset password section
        VBox resetBox = new VBox(10);
        resetBox.setVisible(false);
        resetBox.setPadding(new Insets(20, 0, 0, 0));
        
        Label resetInstructionsLabel = new Label("Enter the token and your new password:");
        resetInstructionsLabel.setFont(Font.font("Arial", 12));
        
        TextField tokenInputField = new TextField();
        tokenInputField.setPromptText("Reset Token");
        tokenInputField.setStyle("-fx-padding: 10; -fx-font-size: 14px;");
        
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        newPasswordField.setStyle("-fx-padding: 10; -fx-font-size: 14px;");
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");
        confirmPasswordField.setStyle("-fx-padding: 10; -fx-font-size: 14px;");
        
        Button resetPasswordBtn = new Button("Reset Password");
        resetPasswordBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                                 "-fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand;");
        resetPasswordBtn.setPrefWidth(200);
        
        resetPasswordBtn.setOnAction(e -> {
            String token = tokenInputField.getText().trim();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            
            if (token.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                messageLabel.setText("Please fill in all fields.");
                messageLabel.setTextFill(Color.web("#e74c3c"));
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                messageLabel.setText("Passwords do not match.");
                messageLabel.setTextFill(Color.web("#e74c3c"));
                return;
            }
            
            if (newPassword.length() < 8) {
                messageLabel.setText("Password must be at least 8 characters long.");
                messageLabel.setTextFill(Color.web("#e74c3c"));
                return;
            }
            
            try {
                boolean success = resetService.resetPassword(token, newPassword);
                
                if (success) {
                    messageLabel.setText("Password reset successful! Redirecting to login...");
                    messageLabel.setTextFill(Color.web("#27ae60"));
                    
                    // Redirect to login after 2 seconds
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            javafx.application.Platform.runLater(() -> {
                                LoginView loginView = new LoginView(stage);
                                stage.setScene(loginView.createScene());
                            });
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                } else {
                    messageLabel.setText("Invalid or expired token. Please request a new one.");
                    messageLabel.setTextFill(Color.web("#e74c3c"));
                }
            } catch (Exception ex) {
                messageLabel.setText("Error: " + ex.getMessage());
                messageLabel.setTextFill(Color.web("#e74c3c"));
            }
        });
        
        resetBox.getChildren().addAll(resetInstructionsLabel, tokenInputField, 
                                      newPasswordField, confirmPasswordField, resetPasswordBtn);
        
        // Show reset section button
        Button showResetSectionBtn = new Button("I have a token - Reset Password");
        showResetSectionBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; " +
                                    "-fx-underline: true; -fx-cursor: hand; -fx-font-size: 12px;");
        showResetSectionBtn.setOnAction(e -> {
            resetBox.setVisible(!resetBox.isVisible());
            showResetSectionBtn.setText(resetBox.isVisible() ? 
                "Hide Reset Section" : "I have a token - Reset Password");
        });
        
        // Back to login link
        Hyperlink backToLoginLink = new Hyperlink("Back to Login");
        backToLoginLink.setFont(Font.font("Arial", 12));
        backToLoginLink.setTextFill(Color.web("#3498db"));
        backToLoginLink.setOnAction(e -> {
            LoginView loginView = new LoginView(stage);
            stage.setScene(loginView.createScene());
        });
        
        container.getChildren().addAll(titleLabel, instructionsLabel, emailLabel, emailField, 
                                      sendTokenBtn, messageLabel, tokenBox, showResetSectionBtn, 
                                      resetBox, backToLoginLink);
        
        // Main layout
        VBox mainLayout = new VBox();
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9);");
        mainLayout.getChildren().add(container);
        
        Scene scene = new Scene(mainLayout, 800, 600);
        
        // Make responsive
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {\n            container.setMaxWidth(Math.min(500, newVal.doubleValue() * 0.9));\n        });\n        \n        return scene;\n    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
