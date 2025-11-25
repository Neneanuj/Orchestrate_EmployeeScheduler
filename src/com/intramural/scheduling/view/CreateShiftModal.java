package com.intramural.scheduling.view;

import com.intramural.scheduling.controller.SchedulingController;
import com.intramural.scheduling.model.Schedule;
import com.intramural.scheduling.model.Sport;
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
import java.time.LocalTime;

public class CreateShiftModal {
    private Stage dialogStage;
    private SchedulingController schedulingController;
    
    private ComboBox<String> sportComboBox;
    private TextField locationField;
    private DatePicker datePicker;
    private TextField startTimeField;
    private TextField endTimeField;
    private Spinner<Integer> supervisorSpinner;
    private Spinner<Integer> refereeSpinner;
    
    private Label sportError;
    private Label locationError;
    private Label dateError;
    private Label startTimeError;
    private Label endTimeError;
    
    private boolean shiftCreated = false;

    public CreateShiftModal(Stage parentStage, SchedulingController controller) {
        this.schedulingController = controller;
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
        root.setPrefWidth(600);

        // Header
        VBox header = new VBox(5);
        Label titleLabel = new Label("Create New Shift");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitleLabel = new Label("Add a new game or event to the schedule");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");

        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Form
        VBox formBox = new VBox(15);

        // Sport Selection
        VBox sportBox = createComboBoxWithError("Sport", sportComboBox = new ComboBox<>(), 
                sportError = new Label());
        sportComboBox.getItems().addAll("Basketball", "Soccer", "Cricket", "Volleyball", "Pickleball");
        sportComboBox.setPromptText("Select sport");
        sportComboBox.setPrefWidth(Double.MAX_VALUE);
        sportComboBox.setStyle("-fx-font-size: 14px;");

        // Location
        VBox locationBox = createFieldWithError("Location", locationField = new TextField(), 
                locationError = new Label());
        locationField.setPromptText("Arena A - Main Court");

        // Date
        VBox dateBox = createDateFieldWithError("Date", datePicker = new DatePicker(), 
                dateError = new Label());
        datePicker.setValue(LocalDate.now());
        datePicker.setPrefWidth(Double.MAX_VALUE);

        // Time Row
        HBox timeRow = new HBox(15);
        VBox startBox = createFieldWithError("Start Time (HH:MM)", startTimeField = new TextField(), 
                startTimeError = new Label());
        startTimeField.setPromptText("14:00");
        
        VBox endBox = createFieldWithError("End Time (HH:MM)", endTimeField = new TextField(), 
                endTimeError = new Label());
        endTimeField.setPromptText("16:00");
        
        HBox.setHgrow(startBox, Priority.ALWAYS);
        HBox.setHgrow(endBox, Priority.ALWAYS);
        timeRow.getChildren().addAll(startBox, endBox);

        // Staff Requirements Row
        HBox staffRow = new HBox(15);
        
        VBox supervisorBox = new VBox(5);
        Label supervisorLabel = new Label("Supervisors Required");
        supervisorLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        supervisorLabel.setStyle("-fx-text-fill: #2c3e50;");
        supervisorSpinner = new Spinner<>(0, 5, 1);
        supervisorSpinner.setPrefWidth(Double.MAX_VALUE);
        supervisorSpinner.setPrefHeight(45);
        supervisorBox.getChildren().addAll(supervisorLabel, supervisorSpinner);
        
        VBox refereeBox = new VBox(5);
        Label refereeLabel = new Label("Referees Required");
        refereeLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        refereeLabel.setStyle("-fx-text-fill: #2c3e50;");
        refereeSpinner = new Spinner<>(1, 10, 2);
        refereeSpinner.setPrefWidth(Double.MAX_VALUE);
        refereeSpinner.setPrefHeight(45);
        refereeBox.getChildren().addAll(refereeLabel, refereeSpinner);
        
        HBox.setHgrow(supervisorBox, Priority.ALWAYS);
        HBox.setHgrow(refereeBox, Priority.ALWAYS);
        staffRow.getChildren().addAll(supervisorBox, refereeBox);

        formBox.getChildren().addAll(sportBox, locationBox, dateBox, timeRow, staffRow);

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

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
    
    private VBox createComboBoxWithError(String labelText, ComboBox<String> comboBox, Label errorLabel) {
        VBox box = new VBox(5);
        box.setMaxWidth(Double.MAX_VALUE);

        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        label.setStyle("-fx-text-fill: #2c3e50;");

        comboBox.setPrefHeight(45);
        errorLabel.setFont(Font.font("Arial", 12));
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");

        box.getChildren().addAll(label, comboBox, errorLabel);
        return box;
    }

    private VBox createDateFieldWithError(String labelText, DatePicker datePicker, Label errorLabel) {
        VBox box = new VBox(5);
        box.setMaxWidth(Double.MAX_VALUE);

        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        label.setStyle("-fx-text-fill: #2c3e50;");

        datePicker.setPrefHeight(45);
        errorLabel.setFont(Font.font("Arial", 12));
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");

        box.getChildren().addAll(label, datePicker, errorLabel);
        return box;
    }

    private void handleCreateShift() {
        clearErrors();
        boolean isValid = true;

        // Validate Sport
        if (sportComboBox.getValue() == null) {
            sportError.setText("Please select a sport");
            isValid = false;
        }

        // Validate Location
        if (locationField.getText().trim().isEmpty()) {
            locationError.setText("Location is required");
            isValid = false;
        }

        // Validate Date
        if (datePicker.getValue() == null) {
            dateError.setText("Date is required");
            isValid = false;
        }

        // Validate Times
        LocalTime startTime = null;
        LocalTime endTime = null;
        
        try {
            startTime = LocalTime.parse(startTimeField.getText().trim());
        } catch (Exception e) {
            startTimeError.setText("Invalid time format (use HH:MM)");
            isValid = false;
        }
        
        try {
            endTime = LocalTime.parse(endTimeField.getText().trim());
        } catch (Exception e) {
            endTimeError.setText("Invalid time format (use HH:MM)");
            isValid = false;
        }
        
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            endTimeError.setText("End time must be after start time");
            isValid = false;
        }

        if (isValid) {
            try {
                // Create the game schedule
                // Note: You'll need to get sport ID from sport name
                // For now, using dummy sport ID
                int sportId = getSportIdFromName(sportComboBox.getValue());
                
                Schedule.Game game = new Schedule.Game(
                    sportId,
                    datePicker.getValue(),
                    startTime,
                    endTime,
                    locationField.getText().trim(),
                    supervisorSpinner.getValue(),
                    refereeSpinner.getValue(),
                    schedulingController.getCurrentCycle().getCycleStart(),
                    schedulingController.getCurrentCycle().getCycleEnd(),
                    1 // createdBy - should be current user ID
                );
                
                // Add to current cycle
                schedulingController.getCurrentCycle().addGameSchedule(game);
                game.generateShifts();
                
                shiftCreated = true;
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Shift created successfully!");
                alert.showAndWait();
                
                dialogStage.close();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to create shift: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    
    private int getSportIdFromName(String sportName) {
        // Map sport names to IDs
        switch (sportName) {
            case "Basketball": return 1;
            case "Soccer": return 2;
            case "Cricket": return 3;
            case "Volleyball": return 4;
            case "Pickleball": return 5;
            default: return 1;
        }
    }

    private void clearErrors() {
        sportError.setText("");
        locationError.setText("");
        dateError.setText("");
        startTimeError.setText("");
        endTimeError.setText("");
    }
    
    public boolean wasShiftCreated() {
        return shiftCreated;
    }
}