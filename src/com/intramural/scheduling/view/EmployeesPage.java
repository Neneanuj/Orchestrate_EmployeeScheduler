package com.intramural.scheduling.view;

import com.intramural.scheduling.dao.AvailabilityDAO;
import com.intramural.scheduling.dao.EmployeeDAO;
import com.intramural.scheduling.dao.EmployeeExpertiseDAO;
import com.intramural.scheduling.dao.SportDAO;
import com.intramural.scheduling.dao.UserDao;
import com.intramural.scheduling.model.Availability;
import com.intramural.scheduling.model.Employee;
import com.intramural.scheduling.model.Sport;
import com.intramural.scheduling.model.User;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

/**
 * EmployeesPage - UPDATED with expandable availability dropdown for each employee
 * Shows employee cards that expand to reveal availability and sports data
 */
public class EmployeesPage {
    private Stage primaryStage;
    private String username;
    private int userId;
    private EmployeeDAO employeeDAO;
    private UserDao userDao;
    private GridPane employeeGrid;
    
    // NEW: Track expanded employees
    private Set<Integer> expandedEmployees = new HashSet<>();

    public EmployeesPage(Stage primaryStage, String username, int userId) {
        this.primaryStage = primaryStage;
        this.username = username;
        this.userId = userId;
        this.employeeDAO = new EmployeeDAO();
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

        Label logo = new Label("⚽");
        logo.setFont(Font.font(28));

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label("Intramural Scheduling");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        Label subtitleLabel = new Label("Employee Management");
        subtitleLabel.setFont(Font.font("Arial", 11));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        HBox navButtons = new HBox(15);
        navButtons.setAlignment(Pos.CENTER_LEFT);
        
        Button dashboardBtn = createNavButton("🏠 Dashboard", false);
        dashboardBtn.setOnAction(e -> {
            AdminDashboard dashboard = new AdminDashboard(primaryStage, username, userId);
            primaryStage.setScene(dashboard.createScene());
        });
        
        Button scheduleBtn = createNavButton("📅 Schedule", false);
        scheduleBtn.setOnAction(e -> {
            ScheduleBuilder scheduleView = new ScheduleBuilder(primaryStage, username, userId);
            primaryStage.setScene(scheduleView.createScene());
        });
        
        Button employeesBtn = createNavButton("👥 Employees", true);
        
        Button usersBtn = createNavButton("👤 Users", false);
        usersBtn.setOnAction(e -> {
            UserManagementView userView = new UserManagementView(primaryStage, username, userId);
            primaryStage.setScene(userView.createScene());
        });

        navButtons.getChildren().addAll(dashboardBtn, scheduleBtn, employeesBtn, usersBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(logo, titleBox, navButtons, spacer);
        return topBar;
    }

    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 14));
        btn.setPrefHeight(35);
        btn.setPadding(new Insets(5, 15, 5, 15));
        
        if (active) {
            btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                    "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-cursor: hand;");
        }
        return btn;
    }

    private ScrollPane createMainContent() {
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30, 40, 30, 40));

        mainContent.getChildren().addAll(
            createHeaderSection(),
            createStatsCards(),
            createEmployeeGridSection()
        );

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");
        return scrollPane;
    }

    private HBox createHeaderSection() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("All Employees");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Employee");
        addBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 12 24 12 24; -fx-cursor: hand; " +
                "-fx-font-size: 14px; -fx-font-weight: bold;");
        addBtn.setOnAction(e -> {
            AddEmployeeModal modal = new AddEmployeeModal(primaryStage);
            modal.setOnSuccess(() -> Platform.runLater(() -> loadEmployeeGrid()));
            modal.show();
        });

        header.getChildren().addAll(titleLabel, spacer, addBtn);
        return header;
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        try {
            List<Employee> allEmployees = employeeDAO.getAll();
            List<Employee> activeEmployees = employeeDAO.getAllActive();
            int inactive = allEmployees.size() - activeEmployees.size();
            long supervisors = activeEmployees.stream()
                .filter(Employee::isSupervisorEligible)
                .count();
            
            // Count STAFF users without employee records
            List<User> allUsers = userDao.getAllUsers();
            Set<Integer> employeeUserIds = allEmployees.stream()
                .map(Employee::getUserId)
                .collect(Collectors.toSet());
            long usersWithoutEmployees = allUsers.stream()
                .filter(u -> u.getRole() == User.UserRole.STAFF)
                .filter(u -> !employeeUserIds.contains(u.getUserId()))
                .count();

            VBox card1 = createStatCard("👥", "#dbeafe", "Total", String.valueOf(allEmployees.size()));
            VBox card2 = createStatCard("✅", "#d1fae5", "Active", String.valueOf(activeEmployees.size()));
            VBox card3 = createStatCard("⊗", "#fee2e2", "Inactive", String.valueOf(inactive));
            VBox card4 = createStatCard("🏅", "#fef3c7", "Supervisors", String.valueOf(supervisors));
            VBox card5 = createStatCard("⚠️", "#fed7aa", "Users w/o Employee", String.valueOf(usersWithoutEmployees));

            HBox.setHgrow(card1, Priority.ALWAYS);
            HBox.setHgrow(card2, Priority.ALWAYS);
            HBox.setHgrow(card3, Priority.ALWAYS);
            HBox.setHgrow(card4, Priority.ALWAYS);
            HBox.setHgrow(card5, Priority.ALWAYS);

            statsBox.getChildren().addAll(card1, card2, card3, card4, card5);
        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading stats");
            statsBox.getChildren().add(errorLabel);
        }

        return statsBox;
    }

    private VBox createStatCard(String icon, String iconBg, String title, String value) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(25));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setMaxWidth(Double.MAX_VALUE);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(32));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 13));
        titleLabel.setStyle("-fx-text-fill: #6b7280;");

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        return card;
    }

    private VBox createEmployeeGridSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        employeeGrid = new GridPane();
        employeeGrid.setHgap(20);
        employeeGrid.setVgap(20);
        loadEmployeeGrid();

        section.getChildren().add(employeeGrid);
        return section;
    }

    private void loadEmployeeGrid() {
        employeeGrid.getChildren().clear();
        
        try {
            List<Employee> employees = employeeDAO.getAllIncludingInactive();
            
            if (employees.isEmpty()) {
                Label emptyLabel = new Label("No employees yet. Click '+ Add Employee' to add one.");
                emptyLabel.setFont(Font.font("Arial", 14));
                emptyLabel.setStyle("-fx-text-fill: #9ca3af;");
                employeeGrid.add(emptyLabel, 0, 0);
                return;
            }

            int col = 0;
            int row = 0;
            for (Employee employee : employees) {
                VBox card = createExpandableEmployeeCard(employee);
                employeeGrid.add(card, col, row);
                
                col++;
                if (col > 2) {
                    col = 0;
                    row++;
                }
            }
        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading employees");
            employeeGrid.add(errorLabel, 0, 0);
        }
    }

    /**
     * NEW: Create expandable employee card with dropdown for availability
     */
    private VBox createExpandableEmployeeCard(Employee employee) {
        VBox card = new VBox();
        String cardStyle = employee.isActiveStatus() 
            ? "-fx-background-color: #f9fafb; -fx-background-radius: 8; -fx-border-color: #e5e7eb; -fx-border-radius: 8;"
            : "-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-opacity: 0.7;";
        card.setStyle(cardStyle);
        card.setPrefWidth(400);

        // Header (always visible, clickable)
        HBox header = createEmployeeCardHeader(employee);
        header.setStyle("-fx-padding: 20; -fx-cursor: hand;");
        header.setOnMouseClicked(e -> toggleEmployeeExpansion(employee.getEmployeeId(), card));
        
        // Hover effect
        header.setOnMouseEntered(e -> header.setStyle("-fx-padding: 20; -fx-cursor: hand; " +
                "-fx-background-color: white; -fx-background-radius: 8 8 0 0;"));
        header.setOnMouseExited(e -> header.setStyle("-fx-padding: 20; -fx-cursor: hand; " +
                "-fx-background-radius: 8 8 0 0;"));

        card.getChildren().add(header);
        
        // Check if expanded
        if (expandedEmployees.contains(employee.getEmployeeId())) {
            VBox details = createEmployeeAvailabilityDetails(employee);
            card.getChildren().add(details);
        }

        return card;
    }
    
    /**
     * NEW: Create employee card header with expand/collapse indicator
     */
    private HBox createEmployeeCardHeader(Employee employee) {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Expand/collapse indicator
        Label expandIcon = new Label(expandedEmployees.contains(employee.getEmployeeId()) ? "▼" : "▶");
        expandIcon.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        expandIcon.setStyle("-fx-text-fill: #6b7280;");
        expandIcon.setPrefWidth(20);

        Label avatar = new Label(employee.getFirstName().substring(0, 1).toUpperCase());
        avatar.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        avatar.setPrefSize(50, 50);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 25;");

        VBox nameBox = new VBox(3);
        Label nameLabel = new Label(employee.getFirstName() + " " + employee.getLastName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        Label roleLabel = new Label(employee.isSupervisorEligible() ? "Supervisor" : "Staff");
        roleLabel.setFont(Font.font("Arial", 12));
        roleLabel.setStyle("-fx-text-fill: #6b7280;");
        nameBox.getChildren().addAll(nameLabel, roleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(employee.isActiveStatus() ? "✓ Active" : "⊘ Inactive");
        badge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        badge.setPadding(new Insets(4, 10, 4, 10));
        badge.setStyle(employee.isActiveStatus() 
            ? "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-background-radius: 12;"
            : "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 12;");

        header.getChildren().addAll(expandIcon, avatar, nameBox, spacer, badge);
        return header;
    }
    
    /**
     * NEW: Toggle employee expansion
     */
    private void toggleEmployeeExpansion(int empId, VBox card) {
        if (expandedEmployees.contains(empId)) {
            expandedEmployees.remove(empId);
        } else {
            expandedEmployees.add(empId);
        }
        loadEmployeeGrid(); // Refresh to show/hide details
    }
    
    /**
     * NEW: Create employee availability details view
     * Shows: Hours, Sports, Availability by day
     */
    private VBox createEmployeeAvailabilityDetails(Employee employee) {
        VBox details = new VBox(15);
        details.setPadding(new Insets(20));
        details.setStyle("-fx-background-color: white; -fx-border-width: 1 0 0 0; " +
                "-fx-border-color: #e5e7eb;");

        // Hours info
        HBox hoursBox = new HBox(10);
        hoursBox.setAlignment(Pos.CENTER_LEFT);
        Label hoursIcon = new Label("⏰");
        hoursIcon.setFont(Font.font(16));
        Label hoursLabel = new Label("Maximum: " + employee.getMaxHoursPerWeek() + " hours per week");
        hoursLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        hoursLabel.setStyle("-fx-text-fill: #374151;");
        hoursBox.getChildren().addAll(hoursIcon, hoursLabel);
        details.getChildren().add(hoursBox);

        // Sports section
        try {
            EmployeeExpertiseDAO expertiseDAO = new EmployeeExpertiseDAO();
            SportDAO sportDAO = new SportDAO();
            List<Integer> sportIds = expertiseDAO.getSportIdsByEmployee(employee.getEmployeeId());
            
            if (!sportIds.isEmpty()) {
                VBox sportsSection = new VBox(10);
                
                HBox sportsHeader = new HBox(10);
                sportsHeader.setAlignment(Pos.CENTER_LEFT);
                Label sportsIcon = new Label("⚽");
                sportsIcon.setFont(Font.font(16));
                Label sportsTitle = new Label("Sports:");
                sportsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                sportsTitle.setStyle("-fx-text-fill: #374151;");
                sportsHeader.getChildren().addAll(sportsIcon, sportsTitle);
                
                FlowPane sportsFlow = new FlowPane();
                sportsFlow.setHgap(10);
                sportsFlow.setVgap(8);
                
                for (Integer sportId : sportIds) {
                    Sport sport = sportDAO.getById(sportId);
                    if (sport != null) {
                        Label sportBadge = new Label(sport.getSportName());
                        sportBadge.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 11));
                        sportBadge.setPadding(new Insets(5, 12, 5, 12));
                        sportBadge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; " +
                                "-fx-background-radius: 15;");
                        sportsFlow.getChildren().add(sportBadge);
                    }
                }
                
                sportsSection.getChildren().addAll(sportsHeader, sportsFlow);
                details.getChildren().add(sportsSection);
            }
            
            // Availability section
            AvailabilityDAO availDAO = new AvailabilityDAO();
            List<Availability.Seasonal> avails = availDAO.getAllByEmployee(employee.getEmployeeId());
            
            if (!avails.isEmpty()) {
                VBox availSection = new VBox(10);
                
                HBox availHeader = new HBox(10);
                availHeader.setAlignment(Pos.CENTER_LEFT);
                Label availIcon = new Label("📅");
                availIcon.setFont(Font.font(16));
                Label availTitle = new Label("Weekly Availability:");
                availTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                availTitle.setStyle("-fx-text-fill: #374151;");
                availHeader.getChildren().addAll(availIcon, availTitle);
                
                // Group by day
                Map<DayOfWeek, List<Availability.Seasonal>> byDay = new HashMap<>();
                for (Availability.Seasonal avail : avails) {
                    byDay.computeIfAbsent(avail.getDayOfWeek(), k -> new ArrayList<>()).add(avail);
                }
                
                VBox availList = new VBox(6);
                for (DayOfWeek day : DayOfWeek.values()) {
                    if (byDay.containsKey(day)) {
                        HBox dayRow = new HBox(10);
                        dayRow.setAlignment(Pos.CENTER_LEFT);
                        dayRow.setPadding(new Insets(6, 10, 6, 10));
                        dayRow.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 4;");
                        
                        Label dayLabel = new Label("• " + day.toString().substring(0, 3));
                        dayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                        dayLabel.setPrefWidth(50);
                        dayLabel.setStyle("-fx-text-fill: #1f2937;");
                        
                        // Show time slots for this day
                        VBox timesBox = new VBox(3);
                        for (Availability.Seasonal avail : byDay.get(day)) {
                            Label timeLabel = new Label(
                                avail.getStartTime().toString() + " - " + avail.getEndTime().toString() +
                                " (" + avail.getSeason() + " " + avail.getYear() + ")"
                            );
                            timeLabel.setFont(Font.font("Arial", 11));
                            timeLabel.setStyle("-fx-text-fill: #6b7280;");
                            timesBox.getChildren().add(timeLabel);
                        }
                        
                        dayRow.getChildren().addAll(dayLabel, timesBox);
                        availList.getChildren().add(dayRow);
                    }
                }
                
                availSection.getChildren().addAll(availHeader, availList);
                details.getChildren().add(availSection);
            } else {
                Label noAvail = new Label("💡 No availability set for this employee");
                noAvail.setFont(Font.font("Arial", 12));
                noAvail.setStyle("-fx-text-fill: #9ca3af;");
                details.getChildren().add(noAvail);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("❌ Error loading employee details");
            errorLabel.setStyle("-fx-text-fill: #ef4444;");
            details.getChildren().add(errorLabel);
        }

        // Edit and Status toggle buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button editBtn = new Button("✏️ Edit Employee");
        editBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand; " +
                "-fx-font-size: 12px; -fx-font-weight: bold;");
        editBtn.setOnAction(e -> editEmployee(employee));
        
        Button statusBtn = new Button(employee.isActiveStatus() ? "🚫 Deactivate" : "✅ Activate");
        statusBtn.setStyle("-fx-background-color: " + (employee.isActiveStatus() ? "#ef4444" : "#22c55e") + "; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand; " +
                "-fx-font-size: 12px; -fx-font-weight: bold;");
        statusBtn.setOnAction(e -> toggleEmployeeStatus(employee));
        
        buttonBox.getChildren().addAll(editBtn, statusBtn);
        details.getChildren().add(buttonBox);

        return details;
    }
    
    /**
     * Edit employee details
     */
    private void editEmployee(Employee employee) {
        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle("Edit Employee");
        dialog.setHeaderText("Edit " + employee.getFirstName() + " " + employee.getLastName());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField(employee.getFirstName());
        TextField lastNameField = new TextField(employee.getLastName());
        TextField maxHoursField = new TextField(String.valueOf(employee.getMaxHoursPerWeek()));
        CheckBox supervisorCheckBox = new CheckBox();
        supervisorCheckBox.setSelected(employee.isSupervisorEligible());
        CheckBox activeCheckBox = new CheckBox();
        activeCheckBox.setSelected(employee.isActiveStatus());

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Max Hours/Week:"), 0, 2);
        grid.add(maxHoursField, 1, 2);
        grid.add(new Label("Supervisor Eligible:"), 0, 3);
        grid.add(supervisorCheckBox, 1, 3);
        grid.add(new Label("Active:"), 0, 4);
        grid.add(activeCheckBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String oldFirstName = employee.getFirstName();
                    String oldLastName = employee.getLastName();
                    String newFirstName = firstNameField.getText().trim();
                    String newLastName = lastNameField.getText().trim();
                    
                    // Validate names
                    if (newFirstName.isEmpty() || newLastName.isEmpty()) {
                        throw new IllegalArgumentException("First name and last name cannot be empty");
                    }
                    
                    // Update employee
                    employee.setFirstName(newFirstName);
                    employee.setLastName(newLastName);
                    employee.setMaxHoursPerWeek(Integer.parseInt(maxHoursField.getText()));
                    employee.setSupervisorEligible(supervisorCheckBox.isSelected());
                    employee.setActiveStatus(activeCheckBox.isSelected());
                    
                    employeeDAO.updateEmployee(employee);
                    
                    // If name changed, update the associated user's username
                    if (!newFirstName.equals(oldFirstName) || !newLastName.equals(oldLastName)) {
                        try {
                            User user = userDao.findById(employee.getUserId());
                            if (user != null) {
                                // Generate new username from name (firstname.lastname)
                                String newUsername = (newFirstName + "." + newLastName).toLowerCase();
                                user.setUsername(newUsername);
                                userDao.updateUser(user);
                            }
                        } catch (Exception userEx) {
                            // Log but don't fail the whole operation
                            System.err.println("Warning: Failed to update associated user: " + userEx.getMessage());
                        }
                    }
                    
                    refreshEmployeeList();
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Success");
                    success.setHeaderText("Employee Updated");
                    success.setContentText("Employee information has been updated successfully." + 
                        ((!newFirstName.equals(oldFirstName) || !newLastName.equals(oldLastName)) ? 
                        "\n\nNote: The associated user's username has also been updated to: " + 
                        (newFirstName + "." + newLastName).toLowerCase() : ""));
                    success.showAndWait();
                } catch (NumberFormatException e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText("Invalid Input");
                    error.setContentText("Max Hours/Week must be a valid number.");
                    error.showAndWait();
                } catch (Exception e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText("Failed to update employee");
                    error.setContentText(e.getMessage());
                    error.showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
    
    /**
     * Toggle employee active status
     */
    private void toggleEmployeeStatus(Employee employee) {
        boolean newStatus = !employee.isActiveStatus();
        String action = newStatus ? "activate" : "deactivate";
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(action.substring(0, 1).toUpperCase() + action.substring(1) + " Employee");
        confirmAlert.setHeaderText(action.substring(0, 1).toUpperCase() + action.substring(1) + " " + employee.getFirstName() + " " + employee.getLastName() + "?");
        confirmAlert.setContentText(newStatus ? 
            "This will activate the employee and they will appear in active employee lists." :
            "This will mark the employee as inactive. They will no longer appear in active employee lists.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    employeeDAO.toggleActiveStatus(employee.getEmployeeId(), newStatus);
                    refreshEmployeeList();
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Success");
                    success.setHeaderText("Employee " + (newStatus ? "Activated" : "Deactivated"));
                    success.setContentText("Employee has been " + action + "d successfully.");
                    success.showAndWait();
                } catch (Exception e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText("Failed to " + action + " employee");
                    error.setContentText(e.getMessage());
                    error.showAndWait();
                }
            }
        });
    }
    
    /**
     * Refresh the employee list
     */
    private void refreshEmployeeList() {
        primaryStage.setScene(createScene());
    }
}
