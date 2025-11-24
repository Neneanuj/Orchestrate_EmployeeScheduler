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
import java.time.LocalDate;

public class CreateShiftModal {
    private Stage dialogStage;
    private TextField eventNameField;
    private TextField locationField;
    private DatePicker datePicker;
    private TextField requiredStaffField;
    private TextField startTimeField;
    private TextField endTimeField;
    private VBox employeeListBox;
    
    private Label eventNameError;
    private Label locationError;
    private Label dateError;
    private Label staffError;
    private Label startTimeError;
    private Label endTimeError;

    public CreateShiftModal(Stage parentStage) {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle("Create New Shift");
        dialogStage.setResizable(false);
    }

    public void show() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(700);
        root.setMaxHeight(700);

        // Header
        VBox header = new VBox(5);
        Label titleLabel = new Label("Create New Shift");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitleLabel = new Label("Add a new game or event to the schedule");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");

        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Form in ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(10, 0, 10, 0));

        // Event Name / Sport
        VBox eventBox = createFieldWithError("Event Name / Sport", eventNameField = new TextField(), 
                eventNameError = new Label());
        eventNameField.setPromptText("Basketball Championship");

        // Location
        VBox locationBox = createFieldWithError("Location", locationField = new TextField(), 
                locationError = new Label());
        locationField.setPromptText("Arena A - Main Court");

        // Date and Required Staff Row
        HBox dateStaffRow = new HBox(15);
        VBox dateBox = createDateFieldWithError("Date", datePicker = new DatePicker(), 
                dateError = new Label());
        datePicker.setPromptText("mm/dd/yyyy");
        datePicker.setValue(LocalDate.now());
        
        VBox staffBox = createFieldWithError("Required Staff", requiredStaffField = new TextField(), 
                staffError = new Label());
        requiredStaffField.setPromptText("8");
        
        HBox.setHgrow(dateBox, Priority.ALWAYS);
        HBox.setHgrow(staffBox, Priority.ALWAYS);
        dateStaffRow.getChildren().addAll(dateBox, staffBox);

        // Start Time and End Time Row
        HBox timeRow = new HBox(15);
        VBox startBox = createFieldWithError("Start Time", startTimeField = new TextField(), 
                startTimeError = new Label());
        startTimeField.setPromptText("--:-- --");
        
        VBox endBox = createFieldWithError("End Time", endTimeField = new TextField(), 
                endTimeError = new Label());
        endTimeField.setPromptText("--:-- --");
        
        HBox.setHgrow(startBox, Priority.ALWAYS);
        HBox.setHgrow(endBox, Priority.ALWAYS);
        timeRow.getChildren().addAll(startBox, endBox);

        // Assign Employees Section
        VBox assignSection = new VBox(10);
        Label assignLabel = new Label("Assign Employees (0 selected)");
        assignLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        assignLabel.setStyle("-fx-text-fill: #2c3e50;");

        employeeListBox = createEmployeeCheckboxList();

        assignSection.getChildren().addAll(assignLabel, employeeListBox);

        formBox.getChildren().addAll(eventBox, locationBox, dateStaffRow, timeRow, assignSection);
        scrollPane.setContent(formBox);

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

        Button createBtn = new Button("Create Shift");
        createBtn.setPrefWidth(150);
        createBtn.setPrefHeight(40);
        createBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        createBtn.setOnAction(e -> handleCreateShift());

        buttonBox.getChildren().addAll(cancelBtn, createBtn);

        root.getChildren().addAll(header, scrollPane, buttonBox);

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

        field.setPrefHeight(45);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setStyle("-fx-font-size: 14px; -fx-background-radius: 6; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 6; -fx-padding: 0 12 0 12;");

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

    private VBox createDateFieldWithError(String labelText, DatePicker datePicker, Label errorLabel) {
        VBox box = new VBox(5);
        box.setMaxWidth(Double.MAX_VALUE);

        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        label.setStyle("-fx-text-fill: #2c3e50;");

        datePicker.setPrefHeight(45);
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setStyle("-fx-font-size: 14px;");

        errorLabel.setFont(Font.font("Arial", 12));
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");

        box.getChildren().addAll(label, datePicker, errorLabel);
        return box;
    }

    private VBox createEmployeeCheckboxList() {
        VBox listBox = new VBox(10);
        listBox.setPadding(new Insets(15));
        listBox.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 8;");
        listBox.setMaxHeight(200);

        String[][] employees = {
            {"Sarah Johnson", "Senior Referee • Basketball, Soccer"},
            {"Michael Chen", "Event Coordinator • Football, Baseball"},
            {"Emily Rodriguez", "Team Lead • Hockey, Volleyball"},
            {"David Park", "Sports Official • Tennis, Badminton"}
        };

        for (String[] emp : employees) {
            CheckBox checkBox = new CheckBox();
            
            VBox empBox = new VBox(3);
            Label nameLabel = new Label(emp[0]);
            nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            nameLabel.setStyle("-fx-text-fill: #1f2937;");
            
            Label detailsLabel = new Label(emp[1]);
            detailsLabel.setFont(Font.font("Arial", 12));
            detailsLabel.setStyle("-fx-text-fill: #6b7280;");
            
            empBox.getChildren().addAll(nameLabel, detailsLabel);

            HBox empRow = new HBox(12);
            empRow.setAlignment(Pos.CENTER_LEFT);
            empRow.getChildren().addAll(checkBox, empBox);

            listBox.getChildren().add(empRow);
        }

        ScrollPane scrollPane = new ScrollPane(listBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox wrapper = new VBox(scrollPane);
        return wrapper;
    }

    private void handleCreateShift() {
        clearErrors();
        boolean isValid = true;

        if (eventNameField.getText().trim().isEmpty()) {
            eventNameError.setText("Event name is required");
            isValid = false;
        }

        if (locationField.getText().trim().isEmpty()) {
            locationError.setText("Location is required");
            isValid = false;
        }

        if (datePicker.getValue() == null) {
            dateError.setText("Date is required");
            isValid = false;
        }

        if (requiredStaffField.getText().trim().isEmpty()) {
            staffError.setText("Required staff is required");
            isValid = false;
        }

        if (startTimeField.getText().trim().isEmpty()) {
            startTimeError.setText("Start time is required");
            isValid = false;
        }

        if (endTimeField.getText().trim().isEmpty()) {
            endTimeError.setText("End time is required");
            isValid = false;
        }

        if (isValid) {
            // TODO: Save shift to database
            System.out.println("Creating shift: " + eventNameField.getText());
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Shift created successfully!");
            alert.showAndWait();
            
            dialogStage.close();
        }
    }

    private void clearErrors() {
        eventNameError.setText("");
        locationError.setText("");
        dateError.setText("");
        staffError.setText("");
        startTimeError.setText("");
        endTimeError.setText("");
    }
}