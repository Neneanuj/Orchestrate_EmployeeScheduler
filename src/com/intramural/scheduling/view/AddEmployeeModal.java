package com.intramural.scheduling.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AddEmployeeModal {
    private Stage dialogStage;
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField roleField;
    private TextField emailField;
    private TextField phoneField;
    private TextField expertiseField;
    private Label firstNameError;
    private Label lastNameError;
    private Label roleError;
    private Label emailError;
    private Label phoneError;
    private Label expertiseError;

    public AddEmployeeModal(Stage parentStage) {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle("Add New Employee");
        dialogStage.setResizable(false);
    }

    public void show() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(600);

        // Header
        VBox header = new VBox(5);
        Label titleLabel = new Label("Add New Employee");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitleLabel = new Label("Add a new team member to your workforce");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");

        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Form fields
        VBox formBox = new VBox(15);

        // First Name and Last Name Row
        HBox nameRow = new HBox(15);
        VBox firstNameBox = createFieldWithError("First Name", firstNameField = new TextField(), 
                firstNameError = new Label());
        firstNameField.setPromptText("John");
        
        VBox lastNameBox = createFieldWithError("Last Name", lastNameField = new TextField(), 
                lastNameError = new Label());
        lastNameField.setPromptText("Doe");
        
        HBox.setHgrow(firstNameBox, Priority.ALWAYS);
        HBox.setHgrow(lastNameBox, Priority.ALWAYS);
        nameRow.getChildren().addAll(firstNameBox, lastNameBox);

        // Role
        VBox roleBox = createFieldWithError("Role", roleField = new TextField(), 
                roleError = new Label());
        roleField.setPromptText("Senior Referee");

        // Email
        VBox emailBox = createFieldWithError("Email", emailField = new TextField(), 
                emailError = new Label());
        emailField.setPromptText("john.doe@example.com");

        // Phone Number
        VBox phoneBox = createFieldWithError("Phone Number", phoneField = new TextField(), 
                phoneError = new Label());
        phoneField.setPromptText("+1 (555) 123-4567");

        // Expertise
        VBox expertiseBox = createFieldWithError("Expertise", expertiseField = new TextField(), 
                expertiseError = new Label());
        expertiseField.setPromptText("Basketball, Soccer, Football");

        formBox.getChildren().addAll(nameRow, roleBox, emailBox, phoneBox, expertiseBox);

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setStyle("-fx-background-color: white; -fx-text-fill: #2c3e50; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-font-weight: bold; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialogStage.close());

        Button addBtn = new Button("Add Employee");
        addBtn.setPrefWidth(150);
        addBtn.setPrefHeight(40);
        addBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        addBtn.setOnAction(e -> handleAddEmployee());

        buttonBox.getChildren().addAll(cancelBtn, addBtn);

        root.getChildren().addAll(header, formBox, buttonBox);

        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private VBox createFieldWithError(String labelText, TextField field, Label errorLabel) {
        VBox box = new VBox(5);
        box.setMaxWidth(Double.MAX_VALUE);

        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        label.setStyle("-fx-text-fill: #2c3e50;");

        field.setPrefHeight(40);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setStyle("-fx-font-size: 14px; -fx-background-radius: 6; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 6; -fx-padding: 0 12 0 12;");

        // Focus effects
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle("-fx-font-size: 14px; -fx-background-radius: 6; " +
                        "-fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 6; " +
                        "-fx-padding: 0 12 0 12;");
                errorLabel.setText("");
            } else {
                field.setStyle("-fx-font-size: 14px; -fx-background-radius: 6; " +
                        "-fx-border-color: #e5e7eb; -fx-border-radius: 6; -fx-padding: 0 12 0 12;");
            }
        });

        errorLabel.setFont(Font.font("Arial", 12));
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");

        box.getChildren().addAll(label, field, errorLabel);
        return box;
    }

    private void handleAddEmployee() {
        clearErrors();
        boolean isValid = true;

        // Validate First Name
        if (firstNameField.getText().trim().isEmpty()) {
            firstNameError.setText("First name is required");
            isValid = false;
        }

        // Validate Last Name
        if (lastNameField.getText().trim().isEmpty()) {
            lastNameError.setText("Last name is required");
            isValid = false;
        }

        // Validate Role
        if (roleField.getText().trim().isEmpty()) {
            roleError.setText("Role is required");
            isValid = false;
        }

        // Validate Email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailError.setText("Email is required");
            isValid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            emailError.setText("Invalid email address");
            isValid = false;
        }

        // Validate Phone
        if (phoneField.getText().trim().isEmpty()) {
            phoneError.setText("Phone is required");
            isValid = false;
        }

        // Validate Expertise
        if (expertiseField.getText().trim().isEmpty()) {
            expertiseError.setText("Expertise is required");
            isValid = false;
        }

        if (isValid) {
            // TODO: Save employee to database
            System.out.println("Adding employee: " + firstNameField.getText() + " " + lastNameField.getText());
            
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Employee added successfully!");
            alert.showAndWait();
            
            dialogStage.close();
        }
    }

    private void clearErrors() {
        firstNameError.setText("");
        lastNameError.setText("");
        roleError.setText("");
        emailError.setText("");
        phoneError.setText("");
        expertiseError.setText("");
    }
}