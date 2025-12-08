package com.intramural.scheduling.view;

import com.intramural.scheduling.dao.SportDAO;
import com.intramural.scheduling.dao.UserDao;
import com.intramural.scheduling.dao.EmployeeDAO;
import com.intramural.scheduling.dao.AvailabilityDAO;
import com.intramural.scheduling.dao.EmployeeExpertiseDAO;
import com.intramural.scheduling.model.*;
import com.intramural.scheduling.service.EmployeeManagementService;
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
import java.util.stream.Collectors;

public class CreateEmployeeView {
    private Stage dialogStage;
    private TextField firstNameField, lastNameField;
    private ComboBox<String> userComboBox;
    private CheckBox supervisorCheckBox;
    private Map<DayOfWeek, CheckBox> availabilityCheckboxes;
    private Map<Integer, CheckBox> sportCheckboxes;
    private List<Sport> availableSports;
    private List<User> availableUsers;
    
    private EmployeeManagementService employeeService;
    private SportDAO sportDAO;
    private UserDao userDao;
    private EmployeeDAO employeeDAO;
    private Runnable onSuccess;

    public CreateEmployeeView(Stage parentStage) {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle("Create New Employee");
        
        employeeService = new EmployeeManagementService();
        sportDAO = new SportDAO();
        userDao = new UserDao();
        employeeDAO = new EmployeeDAO();
        availabilityCheckboxes = new HashMap<>();
        sportCheckboxes = new HashMap<>();
        availableUsers = new ArrayList<>();
        
        loadSports();
        loadAvailableUsers();
    }
    
    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }
    
    private void loadSports() {
        try {
            availableSports = sportDAO.getAll();
        } catch (SQLException e) {
            availableSports = getDefaultSports();
        }
    }
    
    private void loadAvailableUsers() {
        try {
            // Get all users
            List<User> allUsers = userDao.getAllUsers();
            // Get all employees to filter out users who already have employee records
            List<Employee> employees = employeeDAO.getAll();
            Set<Integer> employeeUserIds = employees.stream()
                .map(Employee::getUserId)
                .collect(Collectors.toSet());
            
            // Filter to only show STAFF users without employee records
            availableUsers = allUsers.stream()
                .filter(u -> u.getRole() == User.UserRole.STAFF && !employeeUserIds.contains(u.getUserId()))
                .collect(Collectors.toList());
        } catch (SQLException e) {
            availableUsers = new ArrayList<>();
            e.printStackTrace();
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
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");
        
        // Create a grid layout for compact display
        GridPane mainGrid = new GridPane();
        mainGrid.setHgap(15);
        mainGrid.setVgap(10);
        mainGrid.setPadding(new Insets(15));
        
        // Left column - Employee Info + Availability
        VBox leftColumn = new VBox(10);
        leftColumn.getChildren().addAll(
            createCompactEmployeeInfo(),
            createCompactAvailability()
        );
        
        // Right column - Sports + Buttons
        VBox rightColumn = new VBox(10);
        rightColumn.getChildren().addAll(
            createCompactSports(),
            createButtonBox()
        );
        
        // Add columns to grid
        mainGrid.add(leftColumn, 0, 0);
        mainGrid.add(rightColumn, 1, 0);
        
        // Header at top
        root.setTop(createCompactHeader());
        root.setCenter(mainGrid);
        
        Scene scene = new Scene(root, 900, 600);
        
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);
        dialogStage.showAndWait();
    }
    
    private VBox createHeader() {
        VBox header = new VBox(8);
        
        Label titleLabel = new Label("Create New Employee");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #1f2937;");

        Label subtitleLabel = new Label("Enter employee name, availability, and sports");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setStyle("-fx-text-fill: #6b7280;");

        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }
    
    private VBox createCompactHeader() {
        VBox header = new VBox(5);
        header.setPadding(new Insets(15, 15, 10, 15));
        header.setStyle("-fx-background-color: #3b82f6;");
        
        Label titleLabel = new Label("➕ Create New Employee");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: white;");

        header.getChildren().add(titleLabel);
        return header;
    }
    
    private VBox createCompactEmployeeInfo() {
        VBox section = new VBox(8);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;");
        section.setPrefWidth(420);
        
        Label sectionTitle = new Label("📋 Employee Info");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // User Selection
        Label userLabel = new Label("User Account*:");
        userLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
        
        userComboBox = new ComboBox<>();
        userComboBox.setPromptText("Select user...");
        userComboBox.setPrefWidth(400);
        
        for (User user : availableUsers) {
            userComboBox.getItems().add(user.getUsername() + " (ID: " + user.getUserId() + ")");
        }

        // Name fields in horizontal layout
        HBox nameRow = new HBox(10);
        firstNameField = createCompactTextField("First Name*");
        lastNameField = createCompactTextField("Last Name*");
        
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("[a-zA-Z\\s]*")) {
                firstNameField.setText(oldVal);
            }
        });
        
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("[a-zA-Z\\s]*")) {
                lastNameField.setText(oldVal);
            }
        });
        
        HBox.setHgrow(firstNameField, Priority.ALWAYS);
        HBox.setHgrow(lastNameField, Priority.ALWAYS);
        nameRow.getChildren().addAll(firstNameField, lastNameField);

        supervisorCheckBox = new CheckBox("Supervisor Eligible");
        supervisorCheckBox.setFont(Font.font("Arial", 12));

        section.getChildren().addAll(sectionTitle, userLabel, userComboBox, nameRow, supervisorCheckBox);
        return section;
    }
    
    private VBox createCompactAvailability() {
        VBox section = new VBox(8);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 8;");
        section.setPrefWidth(420);
        
        Label sectionTitle = new Label("📅 Availability (6PM-12AM)");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        // Quick selection buttons
        HBox quickButtons = new HBox(5);
        Button allBtn = createSmallButton("All", "#3b82f6");
        allBtn.setOnAction(e -> selectAll());
        Button weekdaysBtn = createSmallButton("Weekdays", "#10b981");
        weekdaysBtn.setOnAction(e -> selectWeekdays());
        Button weekendsBtn = createSmallButton("Weekends", "#f59e0b");
        weekendsBtn.setOnAction(e -> selectWeekends());
        Button clearBtn = createSmallButton("Clear", "#ef4444");
        clearBtn.setOnAction(e -> clearAll());
        quickButtons.getChildren().addAll(allBtn, weekdaysBtn, weekendsBtn, clearBtn);
        
        // Compact day checkboxes
        FlowPane daysFlow = new FlowPane();
        daysFlow.setHgap(10);
        daysFlow.setVgap(8);
        
        for (DayOfWeek day : DayOfWeek.values()) {
            CheckBox cb = new CheckBox(day.toString().substring(0, 3));
            cb.setFont(Font.font("Arial", 11));
            availabilityCheckboxes.put(day, cb);
            daysFlow.getChildren().add(cb);
        }
        
        section.getChildren().addAll(sectionTitle, quickButtons, daysFlow);
        return section;
    }
    
    private VBox createCompactSports() {
        VBox section = new VBox(8);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 8;");
        section.setPrefWidth(420);
        
        Label sectionTitle = new Label("⚽ Sports");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        FlowPane sportsFlow = new FlowPane();
        sportsFlow.setHgap(10);
        sportsFlow.setVgap(8);
        
        for (Sport sport : availableSports) {
            CheckBox sportCheck = new CheckBox(sport.getSportName());
            sportCheck.setFont(Font.font("Arial", 11));
            sportCheckboxes.put(sport.getSportId(), sportCheck);
            sportsFlow.getChildren().add(sportCheck);
        }
        
        section.getChildren().addAll(sectionTitle, sportsFlow);
        return section;
    }
    
    private TextField createCompactTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle("-fx-font-size: 12px; -fx-padding: 6;");
        return field;
    }
    
    private Button createSmallButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                    "-fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 4;");
        return btn;
    }
    
    private VBox createEmployeeInfoSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 10;");
        
        Label sectionTitle = new Label("📋 Employee Information");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // User Selection Dropdown
        Label userLabel = new Label("Select User Account*:");
        userLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        
        userComboBox = new ComboBox<>();
        userComboBox.setPromptText("Choose existing user...");
        userComboBox.setPrefWidth(400);
        userComboBox.setStyle("-fx-font-size: 14px;");
        
        // Populate user dropdown
        for (User user : availableUsers) {
            userComboBox.getItems().add(user.getUsername() + " (ID: " + user.getUserId() + ")");
        }
        
        VBox userBox = new VBox(8);
        userBox.getChildren().addAll(userLabel, userComboBox);

        HBox nameRow = new HBox(15);
        firstNameField = createStyledTextField("First Name*", "John");
        lastNameField = createStyledTextField("Last Name*", "Doe");
        
        // Restrict numeric input for first name
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("[a-zA-Z\\s]*")) {
                firstNameField.setText(oldVal);
            }
        });
        
        // Restrict numeric input for last name
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("[a-zA-Z\\s]*")) {
                lastNameField.setText(oldVal);
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

        section.getChildren().addAll(sectionTitle, userBox, nameRow, maxHoursRow, supervisorCheckBox);
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
    
    private TextField createStyledTextField(String prompt, String example) {
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

        Button createBtn = new Button("Create Employee");
        createBtn.setPrefWidth(170);
        createBtn.setPrefHeight(45);
        createBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                          "-fx-background-radius: 6; -fx-font-weight: bold;");
        createBtn.setOnAction(e -> handleCreateEmployee());

        buttonBox.getChildren().addAll(cancelBtn, createBtn);
        return buttonBox;
    }
    
    private void handleCreateEmployee() {
        // Validate
        if (!validate()) return;
        
        // Get selected user
        int selectedUserIndex = userComboBox.getSelectionModel().getSelectedIndex();
        if (selectedUserIndex < 0) {
            showError("Please select a user account");
            return;
        }
        User selectedUser = availableUsers.get(selectedUserIndex);
        
        // Collect data
        List<Availability.Seasonal> availabilities = collectAvailability();
        List<Integer> selectedSports = collectSports();
        
        // Create employee linked to the selected user
        try {
            Employee employee = new Employee(0, selectedUser.getUserId(), 
                firstNameField.getText().trim(), 
                lastNameField.getText().trim());
            employee.setSupervisorEligible(supervisorCheckBox.isSelected());
            employee.setMaxHoursPerWeek(20);
            employee.setActiveStatus(true);
            
            // Insert employee
            employeeDAO.insert(employee);
            
            // Save availability
            AvailabilityDAO availabilityDAO = new AvailabilityDAO();
            if (availabilities != null && !availabilities.isEmpty()) {
                for (Availability.Seasonal avail : availabilities) {
                    Availability.Seasonal availWithId = new Availability.Seasonal(
                        employee.getEmployeeId(),
                        avail.getSeason(),
                        avail.getYear(),
                        avail.getDayOfWeek(),
                        avail.getStartTime(),
                        avail.getEndTime()
                    );
                    availabilityDAO.insert(availWithId);
                }
            }
            
            // Save sport expertise
            EmployeeExpertiseDAO expertiseDAO = new EmployeeExpertiseDAO();
            for (Integer sportId : selectedSports) {
                expertiseDAO.insert(employee.getEmployeeId(), sportId, Employee.ExpertiseLevel.INTERMEDIATE);
            }
            
            showSuccess("Employee " + employee.getFullName() + " linked to user '" + selectedUser.getUsername() + "' successfully!");
            
            if (onSuccess != null) onSuccess.run();
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
        if (!firstName.matches("[a-zA-Z\\s]+")) {
            showError("First name must contain only letters and spaces");
            return false;
        }
        if (lastName.isEmpty()) {
            showError("Last name is required");
            return false;
        }
        if (!lastName.matches("[a-zA-Z\\s]+")) {
            showError("Last name must contain only letters and spaces");
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
        LocalTime startTime = LocalTime.of(18, 0);  // 6 PM
        LocalTime endTime = LocalTime.of(0, 0);     // 12 AM
        int currentYear = java.time.LocalDate.now().getYear();
        
        for (Map.Entry<DayOfWeek, CheckBox> entry : availabilityCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                // Create for all seasons
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