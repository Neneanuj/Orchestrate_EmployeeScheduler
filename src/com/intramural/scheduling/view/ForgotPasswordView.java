package com.intramural.scheduling.view;

import com.intramural.scheduling.service.PasswordResetService;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ForgotPasswordView - Handles password reset functionality
 * Refactored with improved security, validation, and error handling
 */
public class ForgotPasswordView {
    
    // Constants
    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordView.class.getName());
    
    // Style Constants
    private static final String PRIMARY_BUTTON_STYLE = 
        "-fx-background-color: #3498db; -fx-text-fill: white; " +
        "-fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand;";
    
    private static final String SUCCESS_BUTTON_STYLE = 
        "-fx-background-color: #27ae60; -fx-text-fill: white; " +
        "-fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand;";
    
    private static final String INPUT_FIELD_STYLE = 
        "-fx-padding: 10; -fx-font-size: 14px;";
    
    private static final String LINK_BUTTON_STYLE = 
        "-fx-background-color: transparent; -fx-text-fill: #3498db; " +
        "-fx-underline: true; -fx-cursor: hand; -fx-font-size: 12px;";
    
    private static final String CONTAINER_STYLE = 
        "-fx-background-color: white; -fx-background-radius: 10;";
    
    private static final String GRADIENT_BACKGROUND = 
        "-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9);";
    
    private static final String TOKEN_DISPLAY_STYLE = 
        "-fx-control-inner-background: #ecf0f1; -fx-font-family: monospace;";
    
    // Color Constants
    private static final String COLOR_DARK_TEXT = "#2c3e50";
    private static final String COLOR_LIGHT_TEXT = "#7f8c8d";
    private static final String COLOR_ERROR = "#e74c3c";
    private static final String COLOR_SUCCESS = "#27ae60";
    private static final String COLOR_LINK = "#3498db";
    
    // Validation Constants
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_CONTAINER_WIDTH = 500;
    private static final int DEFAULT_CONTAINER_WIDTH = 400;
    private static final double RESPONSIVE_WIDTH_FACTOR = 0.9;
    private static final int REDIRECT_DELAY_SECONDS = 2;
    
    // Instance Variables
    private final Stage stage;
    private final PasswordResetService resetService;
    
    // UI Components (for easier testing and modification)
    private TextField emailField;
    private TextField tokenInputField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    private Label messageLabel;
    private TextArea tokenDisplay;
    private VBox tokenBox;
    private VBox resetBox;
    
    /**
     * Constructor
     * @param stage The primary stage for this view
     */
    public ForgotPasswordView(Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Stage cannot be null");
        }
        this.stage = stage;
        this.resetService = new PasswordResetService();
    }
    
    /**
     * Creates and returns the password reset scene
     * @return The configured Scene object
     */
    public Scene createScene() {
        VBox container = createMainContainer();
        
        // Add all components to container
        container.getChildren().addAll(
            createTitleLabel(),
            createInstructionsLabel(),
            createEmailSection(),
            createSendTokenButton(),
            messageLabel,
            tokenBox,
            createToggleResetSectionButton(),
            resetBox,
            createBackToLoginLink()
        );
        
        // Main layout with gradient background
        VBox mainLayout = new VBox();
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle(GRADIENT_BACKGROUND);
        mainLayout.getChildren().add(container);
        
        Scene scene = new Scene(mainLayout, 800, 600);
        makeResponsive(scene, container);
        
        return scene;
    }
    
    /**
     * Creates the main container with styling
     */
    private VBox createMainContainer() {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40));
        container.setStyle(CONTAINER_STYLE);
        container.setMaxWidth(DEFAULT_CONTAINER_WIDTH);
        return container;
    }
    
    /**
     * Creates the title label
     */
    private Label createTitleLabel() {
        Label titleLabel = new Label("Reset Password");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(COLOR_DARK_TEXT));
        titleLabel.setAccessibleText("Password Reset Page Title");
        return titleLabel;
    }
    
    /**
     * Creates the instructions label
     */
    private Label createInstructionsLabel() {
        Label instructionsLabel = new Label(
            "Enter your email address to receive a password reset token."
        );
        instructionsLabel.setWrapText(true);
        instructionsLabel.setFont(Font.font("Arial", 14));
        instructionsLabel.setTextFill(Color.web(COLOR_LIGHT_TEXT));
        return instructionsLabel;
    }
    
    /**
     * Creates the email input section
     */
    private VBox createEmailSection() {
        Label emailLabel = new Label("Email Address:");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        emailField = new TextField();
        emailField.setPromptText("your.email@example.com");
        emailField.setStyle(INPUT_FIELD_STYLE);
        emailField.setAccessibleText("Email address input field");
        
        // Initialize message label
        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("Arial", 12));
        
        // Initialize token display box
        tokenBox = createTokenDisplayBox();
        
        VBox emailSection = new VBox(5);
        emailSection.getChildren().addAll(emailLabel, emailField);
        return emailSection;
    }
    
    /**
     * Creates the token display box (for demo purposes)
     */
    private VBox createTokenDisplayBox() {
        VBox box = new VBox(5);
        box.setVisible(false);
        
        Label tokenLabel = new Label("Your Reset Token:");
        tokenLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        tokenDisplay = new TextArea();
        tokenDisplay.setEditable(false);
        tokenDisplay.setWrapText(true);
        tokenDisplay.setPrefRowCount(3);
        tokenDisplay.setStyle(TOKEN_DISPLAY_STYLE);
        tokenDisplay.setAccessibleText("Generated reset token display");
        
        box.getChildren().addAll(tokenLabel, tokenDisplay);
        return box;
    }
    
    /**
     * Creates the send token button with event handler
     */
    private Button createSendTokenButton() {
        Button sendTokenBtn = new Button("Send Reset Token");
        sendTokenBtn.setStyle(PRIMARY_BUTTON_STYLE);
        sendTokenBtn.setPrefWidth(200);
        sendTokenBtn.setDefaultButton(true);
        sendTokenBtn.setAccessibleText("Send password reset token button");
        
        sendTokenBtn.setOnAction(e -> handleSendToken());
        
        return sendTokenBtn;
    }
    
    /**
     * Handles the send token action
     */
    private void handleSendToken() {
        String email = emailField.getText().trim();
        
        // Validation
        if (email.isEmpty()) {
            showMessage("Please enter your email address.", COLOR_ERROR);
            tokenBox.setVisible(false);
            return;
        }
        
        if (!isValidEmail(email)) {
            showMessage("Please enter a valid email address.", COLOR_ERROR);
            tokenBox.setVisible(false);
            return;
        }
        
        try {
            String token = resetService.generateResetToken(email);
            
            if (token == null) {
                showMessage("No account found with this email address.", COLOR_ERROR);
                tokenBox.setVisible(false);
                LOGGER.log(Level.WARNING, "Password reset attempted for non-existent email: {0}", 
                    email.replaceAll("@.*", "@***"));
            } else {
                showMessage(
                    "Reset token generated successfully! In production, this would be sent to your email.", 
                    COLOR_SUCCESS
                );
                tokenDisplay.setText(token);
                tokenBox.setVisible(true);
                LOGGER.log(Level.INFO, "Password reset token generated for email: {0}", 
                    email.replaceAll("@.*", "@***"));
            }
        } catch (SQLException ex) {
            showMessage("Database error. Please try again later.", COLOR_ERROR);
            tokenBox.setVisible(false);
            LOGGER.log(Level.SEVERE, "Database error during token generation", ex);
        } catch (Exception ex) {
            showMessage("An unexpected error occurred. Please try again.", COLOR_ERROR);
            tokenBox.setVisible(false);
            LOGGER.log(Level.SEVERE, "Unexpected error during token generation", ex);
        }
    }
    
    /**
     * Creates the toggle button for reset section
     */
    private Button createToggleResetSectionButton() {
        Button showResetSectionBtn = new Button("I have a token - Reset Password");
        showResetSectionBtn.setStyle(LINK_BUTTON_STYLE);
        showResetSectionBtn.setAccessibleText("Toggle password reset section");
        
        // Initialize reset box
        resetBox = createResetPasswordBox();
        
        showResetSectionBtn.setOnAction(e -> {
            boolean isVisible = !resetBox.isVisible();
            resetBox.setVisible(isVisible);
            showResetSectionBtn.setText(isVisible ? 
                "Hide Reset Section" : "I have a token - Reset Password");
        });
        
        return showResetSectionBtn;
    }
    
    /**
     * Creates the password reset section
     */
    private VBox createResetPasswordBox() {
        VBox box = new VBox(10);
        box.setVisible(false);
        box.setPadding(new Insets(20, 0, 0, 0));
        
        Label resetInstructionsLabel = new Label("Enter the token and your new password:");
        resetInstructionsLabel.setFont(Font.font("Arial", 12));
        
        tokenInputField = new TextField();
        tokenInputField.setPromptText("Reset Token");
        tokenInputField.setStyle(INPUT_FIELD_STYLE);
        tokenInputField.setAccessibleText("Reset token input field");
        
        newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password (min 8 chars)");
        newPasswordField.setStyle(INPUT_FIELD_STYLE);
        newPasswordField.setAccessibleText("New password input field");
        
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");
        confirmPasswordField.setStyle(INPUT_FIELD_STYLE);
        confirmPasswordField.setAccessibleText("Confirm password input field");
        
        Button resetPasswordBtn = createResetPasswordButton();
        
        box.getChildren().addAll(
            resetInstructionsLabel, 
            tokenInputField, 
            newPasswordField, 
            confirmPasswordField, 
            resetPasswordBtn
        );
        
        return box;
    }
    
    /**
     * Creates the reset password button with event handler
     */
    private Button createResetPasswordButton() {
        Button resetPasswordBtn = new Button("Reset Password");
        resetPasswordBtn.setStyle(SUCCESS_BUTTON_STYLE);
        resetPasswordBtn.setPrefWidth(200);
        resetPasswordBtn.setAccessibleText("Reset password button");
        
        resetPasswordBtn.setOnAction(e -> handleResetPassword());
        
        return resetPasswordBtn;
    }
    
    /**
     * Handles the reset password action
     */
    private void handleResetPassword() {
        String token = tokenInputField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validation
        if (token.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Please fill in all fields.", COLOR_ERROR);
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showMessage("Passwords do not match.", COLOR_ERROR);
            return;
        }
        
        String passwordValidationError = validatePasswordStrength(newPassword);
        if (passwordValidationError != null) {
            showMessage(passwordValidationError, COLOR_ERROR);
            return;
        }
        
        try {
            boolean success = resetService.resetPassword(token, newPassword);
            
            if (success) {
                showMessage("Password reset successful! Redirecting to login...", COLOR_SUCCESS);
                LOGGER.log(Level.INFO, "Password successfully reset using token");
                
                // Redirect to login after delay using PauseTransition
                redirectToLogin();
            } else {
                showMessage("Invalid or expired token. Please request a new one.", COLOR_ERROR);
                LOGGER.log(Level.WARNING, "Password reset failed - invalid or expired token");
            }
        } catch (SQLException ex) {
            showMessage("Database error. Please try again later.", COLOR_ERROR);
            LOGGER.log(Level.SEVERE, "Database error during password reset", ex);
        } catch (Exception ex) {
            showMessage("An unexpected error occurred. Please try again.", COLOR_ERROR);
            LOGGER.log(Level.SEVERE, "Unexpected error during password reset", ex);
        }
    }
    
    /**
     * Validates password strength
     * @param password The password to validate
     * @return Error message if invalid, null if valid
     */
    private String validatePasswordStrength(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return String.format("Password must be at least %d characters long.", MIN_PASSWORD_LENGTH);
        }
        
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter.";
        }
        
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter.";
        }
        
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one digit.";
        }
        
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            return "Password must contain at least one special character.";
        }
        
        return null; // Password is valid
    }
    
    /**
     * Redirects to login view after a delay
     */
    private void redirectToLogin() {
        PauseTransition pause = new PauseTransition(Duration.seconds(REDIRECT_DELAY_SECONDS));
        pause.setOnFinished(event -> {
            try {
                LoginView loginView = new LoginView(stage);
                stage.setScene(loginView.createScene());
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error redirecting to login view", ex);
                showMessage("Error navigating to login. Please restart the application.", COLOR_ERROR);
            }
        });
        pause.play();
    }
    
    /**
     * Creates the back to login link
     */
    private Hyperlink createBackToLoginLink() {
        Hyperlink backToLoginLink = new Hyperlink("Back to Login");
        backToLoginLink.setFont(Font.font("Arial", 12));
        backToLoginLink.setTextFill(Color.web(COLOR_LINK));
        backToLoginLink.setAccessibleText("Back to login page link");
        
        backToLoginLink.setOnAction(e -> {
            try {
                LoginView loginView = new LoginView(stage);
                stage.setScene(loginView.createScene());
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error navigating to login view", ex);
                showMessage("Error navigating to login. Please try again.", COLOR_ERROR);
            }
        });
        
        return backToLoginLink;
    }
    
    /**
     * Makes the scene responsive to window size changes
     */
    private void makeResponsive(Scene scene, VBox container) {
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            double newWidth = Math.min(MAX_CONTAINER_WIDTH, newVal.doubleValue() * RESPONSIVE_WIDTH_FACTOR);
            container.setMaxWidth(newWidth);
        });
    }
    
    /**
     * Displays a message with the specified color
     * @param message The message to display
     * @param colorHex The hex color code
     */
    private void showMessage(String message, String colorHex) {
        messageLabel.setText(message);
        messageLabel.setTextFill(Color.web(colorHex));
    }
    
    /**
     * Validates email format
     * @param email The email to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        // More comprehensive email validation
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex) && email.length() <= 254; // RFC 5321
    }
}