package com.intramural.scheduling.view;

import com.intramural.scheduling.dao.UserDao;
import com.intramural.scheduling.model.User;
import com.intramural.scheduling.service.AuthenticationService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class UserManagementView {
    private Stage primaryStage;
    private String username;
    private int userId;
    private UserDao userDao;
    private VBox userListContainer;

    public UserManagementView(Stage primaryStage, String username, int userId) {
        this.primaryStage = primaryStage;
        this.username = username;
        this.userId = userId;
        this.userDao = new UserDao();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");
        root.setTop(createTopBar());
        
        ScrollPane mainContent = createMainContent();
        root.setCenter(mainContent);
        
        Scene scene = new Scene(root, 1400, 900);
        
        // Make responsive
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            mainContent.setPrefWidth(newVal.doubleValue());
        });
        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            mainContent.setPrefHeight(newVal.doubleValue() - 80);
        });
        
        return scene;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(25);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label logo = new Label("👥");
        logo.setFont(Font.font(28));

        VBox titleBox = new VBox(2);
        Label title = new Label("User Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        Label subtitle = new Label("Manage System Users");
        subtitle.setFont(Font.font("Arial", 11));
        subtitle.setStyle("-fx-text-fill: #7f8c8d;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("← Back to Dashboard");
        backBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-cursor: hand; -fx-background-radius: 5;");
        backBtn.setOnAction(e -> backToDashboard());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-cursor: hand; -fx-background-radius: 5;");
        logoutBtn.setOnAction(e -> logout());

        topBar.getChildren().addAll(logo, titleBox, spacer, backBtn, logoutBtn);
        return topBar;
    }

    private ScrollPane createMainContent() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30));
        mainContent.setStyle("-fx-background-color: #f8f9fa;");

        // Header with Create User button
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label headerLabel = new Label("System Users");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button createUserBtn = new Button("➕ Create New User");
        createUserBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20 10 20; " +
                "-fx-cursor: hand; -fx-background-radius: 5;");
        createUserBtn.setOnAction(e -> showCreateUserDialog());
        
        header.getChildren().addAll(headerLabel, spacer, createUserBtn);
        mainContent.getChildren().add(header);

        // User list container
        userListContainer = new VBox(15);
        loadUsers();
        
        mainContent.getChildren().add(userListContainer);
        scrollPane.setContent(mainContent);
        
        return scrollPane;
    }

    private void loadUsers() {
        userListContainer.getChildren().clear();
        
        try {
            List<User> users = userDao.getAllUsers();
            
            for (User user : users) {
                userListContainer.getChildren().add(createUserCard(user));
            }
            
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load users");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private VBox createUserCard(User user) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Header
        HBox cardHeader = new HBox(15);
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        Label roleLabel = new Label(user.getRole().toString());
        roleLabel.setStyle("-fx-background-color: " + getRoleColor(user.getRole()) + "; " +
                "-fx-text-fill: white; -fx-padding: 5 15 5 15; -fx-background-radius: 15;");
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("✏️ Edit");
        editBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand; " +
                "-fx-font-size: 12px; -fx-font-weight: bold;");
        editBtn.setOnAction(e -> editUser(user));

        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand; " +
                "-fx-font-size: 12px; -fx-font-weight: bold;");
        deleteBtn.setOnAction(e -> deleteUser(user));

        cardHeader.getChildren().addAll(usernameLabel, roleLabel, spacer, editBtn, deleteBtn);

        // Details
        GridPane details = new GridPane();
        details.setHgap(20);
        details.setVgap(10);

        Label userIdLabel = new Label("User ID:");
        userIdLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        Label userIdValue = new Label(String.valueOf(user.getUserId()));

        details.add(userIdLabel, 0, 0);
        details.add(userIdValue, 1, 0);

        card.getChildren().addAll(cardHeader, new Separator(), details);
        return card;
    }

    private String getRoleColor(User.UserRole role) {
        switch (role) {
            case ADMIN: return "#e74c3c";
            case SUPERVISOR: return "#f39c12";
            case STAFF: return "#3498db";
            default: return "#95a5a6";
        }
    }

    private void showCreateUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Create New User");
        dialog.setHeaderText("Enter user details");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email (optional)");
        
        ComboBox<User.UserRole> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(User.UserRole.values());
        roleCombo.setValue(User.UserRole.STAFF);

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    String username = usernameField.getText().trim();
                    String password = passwordField.getText();
                    String email = emailField.getText().trim();
                    
                    if (username.isEmpty() || password.isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Validation Error");
                        alert.setHeaderText("Missing required fields");
                        alert.setContentText("Username and password are required.");
                        alert.showAndWait();
                        return null;
                    }
                    
                    // Hash the password
                    AuthenticationService authService = new AuthenticationService();
                    String hashedPassword = authService.hashPassword(password);
                    
                    // Create user
                    User newUser = new User(0, username, hashedPassword, roleCombo.getValue(), email.isEmpty() ? null : email);
                    userDao.createUser(newUser, email.isEmpty() ? null : email);
                    
                    loadUsers();
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Success");
                    success.setHeaderText("User Created");
                    success.setContentText("User created successfully!\n\n" +
                            "Login Username: " + username.toLowerCase() + "\n" +
                            "Password: (as entered)\n" +
                            "Role: " + roleCombo.getValue());
                    success.showAndWait();
                    
                } catch (Exception e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText("Failed to create user");
                    error.setContentText(e.getMessage());
                    error.showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void editUser(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user: " + user.getUsername());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField(user.getUsername());
        
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New password (leave blank to keep current)");
        
        ComboBox<User.UserRole> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(User.UserRole.values());
        roleCombo.setValue(user.getRole());

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Role:"), 0, 2);
        grid.add(roleCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    user.setUsername(usernameField.getText().trim());
                    user.setRole(roleCombo.getValue());
                    
                    // Update password if provided
                    if (!newPasswordField.getText().isEmpty()) {
                        AuthenticationService authService = new AuthenticationService();
                        String hashedPassword = authService.hashPassword(newPasswordField.getText());
                        user.setPasswordHash(hashedPassword);
                    }
                    
                    userDao.updateUser(user);
                    loadUsers();
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Success");
                    success.setHeaderText("User Updated");
                    success.setContentText("User has been updated successfully.");
                    success.showAndWait();
                    
                } catch (Exception e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText("Failed to update user");
                    error.setContentText(e.getMessage());
                    error.showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void deleteUser(User user) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete User");
        confirmAlert.setHeaderText("Delete user: " + user.getUsername() + "?");
        confirmAlert.setContentText("This action cannot be undone.\n\n" +
                "If this user has an inactive employee record, it will be automatically deleted along with the user.\n" +
                "If the employee is active, you must deactivate it first.\n\n" +
                "Are you sure you want to delete this user?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userDao.deleteUser(user.getUserId());
                    loadUsers();
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Success");
                    success.setHeaderText("User Deleted");
                    success.setContentText("User and any associated inactive employee records have been deleted successfully.");
                    success.showAndWait();
                    
                } catch (SQLException e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText("Failed to delete user");
                    
                    // Show user-friendly error message
                    String errorMessage = e.getMessage();
                    if (errorMessage.contains("active employee")) {
                        error.setContentText(errorMessage + "\n\nGo to the Employees page and deactivate this employee first, then try deleting the user again.");
                    } else if (errorMessage.contains("employee record")) {
                        error.setContentText(errorMessage);
                    } else if (errorMessage.contains("REFERENCE constraint") || errorMessage.contains("FOREIGN KEY")) {
                        error.setContentText("Cannot delete user: This user has associated records (shifts, etc.).\nPlease remove or reassign these records first.");
                    } else {
                        error.setContentText("Error: " + errorMessage);
                    }
                    
                    error.showAndWait();
                } catch (Exception e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText("Failed to delete user");
                    error.setContentText(e.getMessage());
                    error.showAndWait();
                }
            }
        });
    }

    private void backToDashboard() {
        AdminDashboard dashboard = new AdminDashboard(primaryStage, username, userId);
        primaryStage.setScene(dashboard.createScene());
    }

    private void logout() {
        LoginView loginView = new LoginView(primaryStage);
        primaryStage.setScene(loginView.createScene());
        primaryStage.setTitle("Employee Scheduling System - Login");
    }
}
