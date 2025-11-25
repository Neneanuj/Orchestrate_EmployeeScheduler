package com.intramural.scheduling.view;

import com.intramural.scheduling.controller.AvailabilityController;
import com.intramural.scheduling.model.Availability;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityEditor {
    private Stage primaryStage;
    private int employeeId;
    private AvailabilityController controller;
    private VBox availabilityContainer;
    private ComboBox<String> seasonCombo;
    private ComboBox<Integer> yearCombo;
    
    public AvailabilityEditor(Stage primaryStage, int employeeId) {
        this.primaryStage = primaryStage;
        this.employeeId = employeeId;
        this.controller = new AvailabilityController();
    }
    
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");
        
        root.setTop(createTopBar());
        root.setCenter(createMainContent());
        
        return new Scene(root, 1200, 800);
    }
    
    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; " +
                "-fx-border-width: 0 0 1 0;");
        
        Label title = new Label("📅 Availability Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: #2c3e50;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button backBtn = new Button("← Back");
        backBtn.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        
        topBar.getChildren().addAll(title, spacer, backBtn);
        return topBar;
    }
    
    private ScrollPane createMainContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        // Season/Year selector
        HBox selectorBox = new HBox(15);
        selectorBox.setAlignment(Pos.CENTER_LEFT);
        
        seasonCombo = new ComboBox<>();
        seasonCombo.getItems().addAll("FALL", "SPRING", "SUMMER");
        seasonCombo.setValue("FALL");
        seasonCombo.setPrefWidth(150);
        
        yearCombo = new ComboBox<>();
        yearCombo.getItems().addAll(2024, 2025, 2026);
        yearCombo.setValue(2025);
        yearCombo.setPrefWidth(100);
        
        Button loadBtn = new Button("Load Availability");
        loadBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        loadBtn.setOnAction(e -> loadAvailability());
        
        selectorBox.getChildren().addAll(
            new Label("Season:"), seasonCombo,
            new Label("Year:"), yearCombo,
            loadBtn
        );
        
        // Availability grid
        availabilityContainer = new VBox(15);
        availabilityContainer.setPadding(new Insets(20));
        availabilityContainer.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10;");
        
        // Add day slots (6PM-12AM focus)
        for (DayOfWeek day : DayOfWeek.values()) {
            availabilityContainer.getChildren().add(createDaySlot(day));
        }
        
        // Save button
        Button saveBtn = new Button("💾 Save Availability");
        saveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 12 24 12 24; -fx-cursor: hand; " +
                "-fx-font-size: 14px; -fx-font-weight: bold;");
        saveBtn.setOnAction(e -> saveAvailability());
        
        content.getChildren().addAll(selectorBox, availabilityContainer, saveBtn);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");
        
        return scrollPane;
    }
    
    private HBox createDaySlot(DayOfWeek day) {
        HBox slot = new HBox(15);
        slot.setAlignment(Pos.CENTER_LEFT);
        slot.setPadding(new Insets(10));
        slot.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;");
        
        CheckBox enabledCheck = new CheckBox();
        enabledCheck.setSelected(day != DayOfWeek.SUNDAY && day != DayOfWeek.SATURDAY);
        
        Label dayLabel = new Label(day.toString());
        dayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        dayLabel.setPrefWidth(100);
        
        // Time pickers (6PM-12AM)
        ComboBox<String> startTime = new ComboBox<>();
        startTime.getItems().addAll(
            "6:00 PM", "6:30 PM", "7:00 PM", "7:30 PM", "8:00 PM", 
            "8:30 PM", "9:00 PM", "9:30 PM", "10:00 PM"
        );
        startTime.setValue("6:00 PM");
        startTime.setPrefWidth(100);
        
        ComboBox<String> endTime = new ComboBox<>();
        endTime.getItems().addAll(
            "8:00 PM", "8:30 PM", "9:00 PM", "9:30 PM", "10:00 PM", 
            "10:30 PM", "11:00 PM", "11:30 PM", "12:00 AM"
        );
        endTime.setValue("10:00 PM");
        endTime.setPrefWidth(100);
        
        CheckBox preferredCheck = new CheckBox("Preferred");
        
        slot.getChildren().addAll(enabledCheck, dayLabel, 
            new Label("From:"), startTime,
            new Label("To:"), endTime,
            preferredCheck
        );
        
        // Store references for saving
        slot.setUserData(new Object[]{enabledCheck, startTime, endTime, preferredCheck});
        
        return slot;
    }
    
    private void loadAvailability() {
        try {
            Availability.Season season = Availability.Season.valueOf(seasonCombo.getValue());
            int year = yearCombo.getValue();
            
            List<Availability.Seasonal> existing = 
                controller.getAvailability(employeeId, season, year);
            
            // Update UI with loaded data
            showAlert(Alert.AlertType.INFORMATION, "Loaded", 
                "Loaded " + existing.size() + " availability entries");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                "Failed to load availability: " + e.getMessage());
        }
    }
    
    private void saveAvailability() {
        try {
            Availability.Season season = Availability.Season.valueOf(seasonCombo.getValue());
            int year = yearCombo.getValue();
            List<Availability.Seasonal> availabilities = new ArrayList<>();
            
            int dayIndex = 0;
            for (DayOfWeek day : DayOfWeek.values()) {
                HBox slot = (HBox) availabilityContainer.getChildren().get(dayIndex++);
                Object[] controls = (Object[]) slot.getUserData();
                
                CheckBox enabled = (CheckBox) controls[0];
                if (enabled.isSelected()) {
                    ComboBox<String> startCombo = (ComboBox<String>) controls[1];
                    ComboBox<String> endCombo = (ComboBox<String>) controls[2];
                    CheckBox preferred = (CheckBox) controls[3];
                    
                    LocalTime start = parseTime(startCombo.getValue());
                    LocalTime end = parseTime(endCombo.getValue());
                    
                    Availability.Seasonal avail = new Availability.Seasonal(
                        employeeId, season, year, day, start, end
                    );
                    avail.setPreferred(preferred.isSelected());
                    availabilities.add(avail);
                }
            }
            
            controller.submitAvailability(employeeId, season, year, availabilities);
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                "Availability saved successfully!");
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                "Failed to save: " + e.getMessage());
        }
    }
    
    private LocalTime parseTime(String timeStr) {
        // Parse "6:00 PM" format
        String[] parts = timeStr.replace(" PM", "").replace(" AM", "").split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        
        if (timeStr.contains("PM") && hour != 12) {
            hour += 12;
        } else if (timeStr.contains("AM") && hour == 12) {
            hour = 0;
        }
        
        return LocalTime.of(hour, minute);
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}