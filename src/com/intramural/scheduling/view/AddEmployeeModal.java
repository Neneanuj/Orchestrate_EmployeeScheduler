package com.intramural.scheduling.view;

import com.intramural.scheduling.dao.SportDAO;
import com.intramural.scheduling.model.*;
import com.intramural.scheduling.service.EmployeeManagementService;
import javafx.application.Platform;
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
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

/**
 * AddEmployeeModal - Called by AdminDashboard
 * Creates employees with availability and sport expertise
 */
public class AddEmployeeModal {
    private Stage dialogStage;
    private TextField firstNameField, lastNameField;
    private CheckBox supervisorCheckBox;
    private Map<DayOfWeek, CheckBox> availabilityCheckboxes;
    private Map<Integer, CheckBox> sportCheckboxes;
    private List<Sport> availableSports;
    
    private EmployeeManagementService employeeService;
    private SportDAO sportDAO;
    private Runnable onSuccess;
    private Employee createdEmployee;

    public AddEmployeeModal(Stage parentStage) {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle("Add New Employee");
        
        employeeService = new EmployeeManagementService();
        sportDAO = new SportDAO();
        availabilityCheckboxes = new HashMap<>();
        sportCheckboxes = new HashMap<>();
        
        loadSports();
    }
    
    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }
    
    public Employee getCreatedEmployee() {
        return createdEmployee;
    }
    
    private void loadSports() {
        try {
            availableSports = sportDAO.getAll();
        } catch (SQLException e) {
            availableSports = getDefaultSports();
        }
    }
    
    private List<Sport> getDefaultSports() {
        List<Sport> sports = new ArrayList<>();
        sports.add(new Sport(1, "Soccer", 120, 1, 3));
        sports.add(new Sport(2, "Cricket", 180, 1, 3));
        sports.add(new Sport(3, "Volleyball", 90, 1, 2));
        sports.add(new Sport(4, "Basketball", 120, 1, 2));
        sports.add(new Sport(5, "Softball", 120, 1, 2));
        return sports;
    }

    public void show() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        
        VBox root = new VBox(25);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(750);

        root.getChildren().addAll(
            createHeader(),
            createEmployeeInfoSection(),
            createAvailabilitySection(),
            createSportsSection(),
            createButtonBox()
        );

        scrollPane.setContent(root);
        Scene scene = new Scene(scrollPane, 800, 900);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
    
    private VBox createHeader() {
        VBox header = new VBox(8);
        
        Label titleLabel = new Label("Add New Employee");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #1f2937;");

        Label subtitleLabel = new Label("Enter employee name, availability, and sports");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setStyle("-fx-text-fill: #6b7280;");

        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }
    
    private VBox createEmployeeInfoSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 10;");
        
        Label sectionTitle = new Label("📋 Employee Information");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        HBox nameRow = new HBox(15);
        firstNameField = createStyledTextField("John");
        lastNameField = createStyledTextField("Doe");
        // BUG-F018: Auto-trim name fields on focus lost
        firstNameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                firstNameField.setText(firstNameField.getText().trim());
            }
        });
        lastNameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                lastNameField.setText(lastNameField.getText().trim());
            }
        });
        HBox.setHgrow(firstNameField, Priority.ALWAYS);
        HBox.setHgrow(lastNameField, Priority.ALWAYS);
        nameRow.getChildren().addAll(firstNameField, lastNameField);

        HBox maxHoursRow = new HBox(10);
        maxHoursRow.setAlignment(Pos.CENTER_LEFT);
        Label maxLabel = new Label("Maximum Hours Per Week:");
        maxLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        Label maxValue = new Label("20 hours");
        maxValue.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        maxValue.setStyle("-fx-text-fill: #3b82f6;");
        maxHoursRow.getChildren().addAll(maxLabel, maxValue);

        supervisorCheckBox = new CheckBox("Supervisor Eligible");
        supervisorCheckBox.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));

        section.getChildren().addAll(sectionTitle, nameRow, maxHoursRow, supervisorCheckBox);
        return section;
    }
    
    private VBox createAvailabilitySection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 10;");
        
        Label sectionTitle = new Label("📅 Weekly Availability (6:00 PM - 12:00 AM)");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        HBox quickButtons = new HBox(10);
        Button allBtn = createQuickButton("Select All", "#3b82f6");
        allBtn.setOnAction(e -> selectAll());
        Button weekdaysBtn = createQuickButton("Weekdays", "#10b981");
        weekdaysBtn.setOnAction(e -> selectWeekdays());
        Button weekendsBtn = createQuickButton("Weekends", "#f59e0b");
        weekendsBtn.setOnAction(e -> selectWeekends());
        Button clearBtn = createQuickButton("Clear", "#ef4444");
        clearBtn.setOnAction(e -> clearAll());
        quickButtons.getChildren().addAll(allBtn, weekdaysBtn, weekendsBtn, clearBtn);
        
        VBox daysGrid = new VBox(8);
        for (DayOfWeek day : DayOfWeek.values()) {
            daysGrid.getChildren().add(createDayRow(day));
        }
        
        section.getChildren().addAll(sectionTitle, quickButtons, daysGrid);
        return section;
    }
    
    private VBox createSportsSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 10;");
        
        Label sectionTitle = new Label("⚽ Sports Employee Can Officiate");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        FlowPane sportsFlow = new FlowPane();
        sportsFlow.setHgap(15);
        sportsFlow.setVgap(12);
        
        for (Sport sport : availableSports) {
            CheckBox sportCheck = new CheckBox(sport.getSportName());
            sportCheck.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
            sportCheckboxes.put(sport.getSportId(), sportCheck);
            sportsFlow.getChildren().add(sportCheck);
        }
        
        section.getChildren().addAll(sectionTitle, sportsFlow);
        return section;
    }
    
    private HBox createDayRow(DayOfWeek day) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 6;");
        
        CheckBox checkbox = new CheckBox();
        availabilityCheckboxes.put(day, checkbox);
        
        Label dayLabel = new Label(day.toString().substring(0, 1) + 
                                  day.toString().substring(1).toLowerCase());
        dayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        dayLabel.setPrefWidth(100);
        
        Label timeLabel = new Label("6:00 PM - 12:00 AM");
        timeLabel.setFont(Font.font("Arial", 13));
        timeLabel.setStyle("-fx-text-fill: #6b7280;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label hoursLabel = new Label("(6 hours)");
        hoursLabel.setFont(Font.font("Arial", 12));
        hoursLabel.setStyle("-fx-text-fill: #9ca3af;");
        
        row.getChildren().addAll(checkbox, dayLabel, timeLabel, spacer, hoursLabel);
        
        checkbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                row.setStyle("-fx-background-color: #dbeafe; -fx-background-radius: 6;");
            } else {
                row.setStyle("-fx-background-color: white; -fx-background-radius: 6;");
            }
        });
        
        return row;
    }
    
    private TextField createStyledTextField(String example) {
        TextField field = new TextField();
        field.setPromptText(example);
        field.setStyle("-fx-font-size: 14px; -fx-pref-height: 40px; " +
                      "-fx-background-radius: 6; -fx-border-color: #e5e7eb; " +
                      "-fx-border-radius: 6;");
        return field;
    }
    
    private Button createQuickButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                    "-fx-background-radius: 6; -fx-padding: 6 16; -fx-cursor: hand; " +
                    "-fx-font-size: 12px; -fx-font-weight: bold;");
        return btn;
    }
    
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(45);
        cancelBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #374151; " +
                          "-fx-background-radius: 6; -fx-font-weight: bold;");
        cancelBtn.setOnAction(e -> dialogStage.close());

        Button addBtn = new Button("Add Employee");
        addBtn.setPrefWidth(170);
        addBtn.setPrefHeight(45);
        addBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                          "-fx-background-radius: 6; -fx-font-weight: bold;");
        addBtn.setOnAction(e -> handleAddEmployee());

        buttonBox.getChildren().addAll(cancelBtn, addBtn);
        return buttonBox;
    }
    
    private void handleAddEmployee() {
        if (!validate()) return;
        
        List<Availability.Seasonal> availabilities = collectAvailability();
        List<Integer> selectedSports = collectSports();
        
        try {
            // CALLS: employeeService.createEmployee() 
            // (matches EmployeeManagementService_FINAL)
            createdEmployee = employeeService.createEmployee(
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                supervisorCheckBox.isSelected(),
                availabilities,
                selectedSports
            );
            
            showSuccess("Employee " + createdEmployee.getFullName() + " created successfully!");
            
            if (onSuccess != null) {
                Platform.runLater(onSuccess);
            }
            
            dialogStage.close();
            
        } catch (Exception e) {
            showError("Failed to create employee: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean validate() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        
        if (firstName.isEmpty()) {
            showError("First name is required");
            return false;
        }
        // BUG-F003: Validate name contains only letters, spaces, hyphens, apostrophes
        if (!firstName.matches("^[a-zA-Z\\s'-]+$")) {
            showError("First name must contain only letters, spaces, hyphens, or apostrophes");
            return false;
        }
        if (lastName.isEmpty()) {
            showError("Last name is required");
            return false;
        }
        // BUG-F003: Validate name contains only letters
        if (!lastName.matches("^[a-zA-Z\\s'-]+$")) {
            showError("Last name must contain only letters, spaces, hyphens, or apostrophes");
            return false;
        }
        if (!availabilityCheckboxes.values().stream().anyMatch(CheckBox::isSelected)) {
            showError("Select at least one availability day");
            return false;
        }
        if (!sportCheckboxes.values().stream().anyMatch(CheckBox::isSelected)) {
            showError("Select at least one sport");
            return false;
        }
        return true;
    }
    
    private List<Availability.Seasonal> collectAvailability() {
        List<Availability.Seasonal> availabilities = new ArrayList<>();
        LocalTime startTime = LocalTime.of(18, 0);
        LocalTime endTime = LocalTime.of(0, 0);
        int currentYear = java.time.LocalDate.now().getYear();
        
        for (Map.Entry<DayOfWeek, CheckBox> entry : availabilityCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                for (Availability.Season season : Availability.Season.values()) {
                    availabilities.add(new Availability.Seasonal(
                        0, season, currentYear, entry.getKey(), startTime, endTime
                    ));
                }
            }
        }
        return availabilities;
    }
    
    private List<Integer> collectSports() {
        List<Integer> sports = new ArrayList<>();
        for (Map.Entry<Integer, CheckBox> entry : sportCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                sports.add(entry.getKey());
            }
        }
        return sports;
    }
    
    private void selectAll() {
        availabilityCheckboxes.values().forEach(cb -> cb.setSelected(true));
    }
    
    private void selectWeekdays() {
        clearAll();
        availabilityCheckboxes.get(DayOfWeek.MONDAY).setSelected(true);
        availabilityCheckboxes.get(DayOfWeek.TUESDAY).setSelected(true);
        availabilityCheckboxes.get(DayOfWeek.WEDNESDAY).setSelected(true);
        availabilityCheckboxes.get(DayOfWeek.THURSDAY).setSelected(true);
        availabilityCheckboxes.get(DayOfWeek.FRIDAY).setSelected(true);
    }
    
    private void selectWeekends() {
        clearAll();
        availabilityCheckboxes.get(DayOfWeek.SATURDAY).setSelected(true);
        availabilityCheckboxes.get(DayOfWeek.SUNDAY).setSelected(true);
    }
    
    private void clearAll() {
        availabilityCheckboxes.values().forEach(cb -> cb.setSelected(false));
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