package com.intramural.scheduling.view;

import com.intramural.scheduling.dao.GameScheduleDAO;
import com.intramural.scheduling.dao.ShiftDAO;
import com.intramural.scheduling.dao.SportDAO;
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
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreateShiftView {
    private Stage dialogStage;
    private int adminUserId;
    private Runnable onSuccess;
    
    private ComboBox<Sport> sportCombo;
    private DatePicker datePicker;
    private ComboBox<String> startTimeCombo;
    private ComboBox<String> endTimeCombo;
    private TextField locationField;
    private Spinner<Integer> supervisorsSpinner;
    private Spinner<Integer> refereesSpinner;
    
    private Label sportError;
    private Label dateError;
    private Label startTimeError;
    private Label endTimeError;
    private Label locationError;
    
    private SportDAO sportDAO;
    private GameScheduleDAO gameDAO;
    private ShiftDAO shiftDAO;

    public CreateShiftView(Stage parentStage, int adminUserId) {
        this.adminUserId = adminUserId;
        this.sportDAO = new SportDAO();
        this.gameDAO = new GameScheduleDAO();
        this.shiftDAO = new ShiftDAO();
        
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle("Create New Shift");
    }
    
    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    public void show() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        
        VBox root = new VBox(25);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(600);

        Label titleLabel = new Label("Create New Shift");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Label subtitleLabel = new Label("Schedule a new game and staffing requirements");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");

        VBox shiftDetails = createShiftDetailsSection();
        VBox staffingReqs = createStaffingSection();
        HBox buttons = createButtonSection();

        root.getChildren().addAll(titleLabel, subtitleLabel, shiftDetails, staffingReqs, buttons);
        scrollPane.setContent(root);

        Scene scene = new Scene(scrollPane, 650, 750);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private VBox createShiftDetailsSection() {
        VBox section = new VBox(20);
        section.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; " +
                "-fx-border-radius: 10; -fx-background-radius: 10;");
        section.setPadding(new Insets(25));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("⚽");
        icon.setFont(Font.font(20));
        Label title = new Label("Shift Details");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        header.getChildren().addAll(icon, title);

        Label sportLabel = createLabel("Sport*");
        sportCombo = new ComboBox<>();
        sportCombo.setPrefWidth(400);
        sportCombo.setPrefHeight(40);
        sportCombo.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; " +
                "-fx-border-radius: 6; -fx-background-radius: 6;");
        
        // Fix sport display
        sportCombo.setCellFactory(param -> new ListCell<Sport>() {
            @Override
            protected void updateItem(Sport item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getSportName());
            }
        });
        
        sportCombo.setButtonCell(new ListCell<Sport>() {
            @Override
            protected void updateItem(Sport item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getSportName());
            }
        });
        
        loadSports();
        sportError = createErrorLabel();

        Label dateLabel = createLabel("Date*");
        datePicker = new DatePicker();
        datePicker.setPrefWidth(400);
        datePicker.setPrefHeight(40);
        datePicker.setValue(LocalDate.now());
        dateError = createErrorLabel();

        HBox timeBox = new HBox(20);
        VBox startTimeBox = new VBox(8);
        Label startTimeLabel = createLabel("Start Time*");
        startTimeCombo = new ComboBox<>();
        startTimeCombo.getItems().addAll(generateTimeSlots());
        startTimeCombo.setValue("6:00 PM");
        startTimeCombo.setPrefWidth(190);
        startTimeCombo.setPrefHeight(40);
        startTimeError = createErrorLabel();
        startTimeBox.getChildren().addAll(startTimeLabel, startTimeCombo, startTimeError);
        
        VBox endTimeBox = new VBox(8);
        Label endTimeLabel = createLabel("End Time*");
        endTimeCombo = new ComboBox<>();
        endTimeCombo.getItems().addAll(generateTimeSlots());
        endTimeCombo.setValue("9:00 PM");
        endTimeCombo.setPrefWidth(190);
        endTimeCombo.setPrefHeight(40);
        endTimeError = createErrorLabel();
        endTimeBox.getChildren().addAll(endTimeLabel, endTimeCombo, endTimeError);
        
        timeBox.getChildren().addAll(startTimeBox, endTimeBox);

        Label locationLabel = createLabel("Location*");
        locationField = new TextField();
        locationField.setPromptText("e.g., Field A, Court 1");
        locationField.setPrefWidth(400);
        locationField.setPrefHeight(40);
        locationError = createErrorLabel();

        section.getChildren().addAll(
            header,
            sportLabel, sportCombo, sportError,
            dateLabel, datePicker, dateError,
            timeBox,
            locationLabel, locationField, locationError
        );
        
        return section;
    }

    private VBox createStaffingSection() {
        VBox section = new VBox(20);
        section.setStyle("-fx-background-color: #f0f9ff; -fx-border-color: #bfdbfe; " +
                "-fx-border-radius: 10; -fx-background-radius: 10;");
        section.setPadding(new Insets(25));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("👥");
        icon.setFont(Font.font(20));
        Label title = new Label("Staffing Requirements");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        header.getChildren().addAll(icon, title);

        HBox staffingBox = new HBox(40);
        
        VBox supervisorsBox = new VBox(10);
        Label supervisorsLabel = createLabel("Supervisors Needed");
        supervisorsSpinner = new Spinner<>(0, 5, 1);
        supervisorsSpinner.setPrefWidth(120);
        supervisorsSpinner.setPrefHeight(40);
        supervisorsBox.getChildren().addAll(supervisorsLabel, supervisorsSpinner);
        
        VBox refereesBox = new VBox(10);
        Label refereesLabel = createLabel("Referees Needed");
        refereesSpinner = new Spinner<>(1, 10, 3);
        refereesSpinner.setPrefWidth(120);
        refereesSpinner.setPrefHeight(40);
        refereesBox.getChildren().addAll(refereesLabel, refereesSpinner);
        
        staffingBox.getChildren().addAll(supervisorsBox, refereesBox);
        section.getChildren().addAll(header, staffingBox);
        
        return section;
    }

    private HBox createButtonSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(45);
        cancelBtn.setStyle("-fx-background-color: white; -fx-text-fill: #6b7280; " +
                "-fx-border-color: #d1d5db; -fx-border-radius: 6; -fx-background-radius: 6;");
        cancelBtn.setOnAction(e -> dialogStage.close());

        Button createBtn = new Button("Create Shift");
        createBtn.setPrefWidth(140);
        createBtn.setPrefHeight(45);
        createBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-font-weight: bold;");
        createBtn.setOnAction(e -> handleCreateShift());

        buttonBox.getChildren().addAll(cancelBtn, createBtn);
        return buttonBox;
    }
    
    private void loadSports() {
        try {
            List<Sport> sports = sportDAO.getAll();
            sportCombo.getItems().addAll(sports);
            if (!sports.isEmpty()) {
                sportCombo.setValue(sports.get(0));
            }
        } catch (SQLException e) {
            showError("Failed to load sports: " + e.getMessage());
        }
    }
    
    private void handleCreateShift() {
        clearErrors();
        
        if (!validate()) {
            return;
        }
        
        try {
            Sport sport = sportCombo.getValue();
            LocalDate date = datePicker.getValue();
            LocalTime startTime = parseTime(startTimeCombo.getValue());
            LocalTime endTime = parseTime(endTimeCombo.getValue());
            String location = locationField.getText().trim();
            int supervisors = supervisorsSpinner.getValue();
            int referees = refereesSpinner.getValue();
            
            System.out.println("Creating shift:");
            System.out.println("  Sport: " + sport.getSportName());
            System.out.println("  Date: " + date);
            System.out.println("  Time: " + startTime + " - " + endTime);
            System.out.println("  Location: " + location);
            System.out.println("  Supervisors: " + supervisors);
            System.out.println("  Referees: " + referees);
            
            // Create game schedule with cycle dates
            LocalDate cycleStart = date;
            LocalDate cycleEnd = date.plusDays(7);
            
            Schedule.Game game = new Schedule.Game(
                sport.getSportId(),
                date,
                startTime,
                endTime,
                location,
                supervisors,
                referees,
                cycleStart,
                cycleEnd,
                adminUserId
            );
            
            // Insert game into database
            gameDAO.insert(game);
            System.out.println("Game inserted with ID: " + game.getScheduleId());
            
            // Generate and insert shifts
            game.generateShifts();
            System.out.println("Generated " + game.getShifts().size() + " shifts");
            
            for (Schedule.Shift shift : game.getShifts()) {
                shiftDAO.insert(shift);
                System.out.println("Shift inserted with ID: " + shift.getShiftId());
            }
            
            showSuccess("Shift created successfully!\n" + 
                       supervisors + " supervisor(s) + " + referees + " referee(s) positions created.");
            
            // Call success callback to refresh parent view
            if (onSuccess != null) {
                System.out.println("Calling success callback...");
                onSuccess.run();
            }
            
            dialogStage.close();
            
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to create shift: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("General Error: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to create shift: " + e.getMessage());
        }
    }
    
    private boolean validate() {
        boolean valid = true;
        
        if (sportCombo.getValue() == null) {
            sportError.setText("Sport is required");
            valid = false;
        }
        
        if (datePicker.getValue() == null) {
            dateError.setText("Date is required");
            valid = false;
        }
        
        if (startTimeCombo.getValue() == null) {
            startTimeError.setText("Start time is required");
            valid = false;
        }
        
        if (endTimeCombo.getValue() == null) {
            endTimeError.setText("End time is required");
            valid = false;
        }
        
        if (startTimeCombo.getValue() != null && endTimeCombo.getValue() != null) {
            LocalTime start = parseTime(startTimeCombo.getValue());
            LocalTime end = parseTime(endTimeCombo.getValue());
            if (!start.isBefore(end)) {
                endTimeError.setText("End time must be after start time");
                valid = false;
            }
        }
        
        if (locationField.getText().trim().isEmpty()) {
            locationError.setText("Location is required");
            valid = false;
        }
        
        return valid;
    }
    
    private void clearErrors() {
        sportError.setText("");
        dateError.setText("");
        startTimeError.setText("");
        endTimeError.setText("");
        locationError.setText("");
    }
    
    private LocalTime parseTime(String timeStr) {
        try {
            timeStr = timeStr.trim().replaceAll("\\s+", " ");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
            return LocalTime.parse(timeStr, formatter);
        } catch (Exception e) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma", Locale.US);
                return LocalTime.parse(timeStr.replaceAll("\\s+", ""), formatter);
            } catch (Exception e2) {
                throw new RuntimeException("Cannot parse time: " + timeStr, e);
            }
        }
    }
    
    private List<String> generateTimeSlots() {
        List<String> slots = new ArrayList<>();
        for (int hour = 6; hour <= 23; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                LocalTime time = LocalTime.of(hour, minute);
                slots.add(time.format(DateTimeFormatter.ofPattern("h:mm a")));
            }
        }
        return slots;
    }
    
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setStyle("-fx-text-fill: #374151;");
        return label;
    }
    
    private Label createErrorLabel() {
        Label label = new Label();
        label.setFont(Font.font("Arial", 11));
        label.setStyle("-fx-text-fill: #dc2626;");
        return label;
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}