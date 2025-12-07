package com.intramural.scheduling.view;

import com.intramural.scheduling.controller.AdminDashboardController;
import com.intramural.scheduling.controller.SchedulingController;
import com.intramural.scheduling.dao.*;
import com.intramural.scheduling.model.*;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * FULLY ENHANCED AdminDashboard with:
 * - Expandable/collapsible shift cards
 * - Shows assigned employees after finalization
 * - Expandable employee cards with sports/availability
 * - Edit buttons for shifts and employees
 * - Referee/supervisor counts
 */
public class AdminDashboard {
    private Stage primaryStage;
    private String username;
    private int userId;
    private AdminDashboardController controller;
    private SchedulingController schedulingController;
    private VBox teamMembersContent;
    private VBox upcomingShiftsContent;
    
    // Track expanded state
    private Set<Integer> expandedGames = new HashSet<>();
    private Set<Integer> expandedEmployees = new HashSet<>();

    public AdminDashboard(Stage primaryStage, String username, int userId) {
        this.primaryStage = primaryStage;
        this.username = username;
        this.userId = userId;
        this.controller = new AdminDashboardController();
        this.schedulingController = new SchedulingController();
        
        LocalDate cycleStart = LocalDate.now().withDayOfMonth(1);
        LocalDate cycleEnd = cycleStart.plusMonths(1).minusDays(1);
        schedulingController.createCycle(cycleStart, cycleEnd);
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
        Label title = new Label("Intramural Scheduling");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        Label subtitle = new Label("Admin Dashboard");
        subtitle.setFont(Font.font("Arial", 11));
        subtitle.setStyle("-fx-text-fill: #7f8c8d;");
        titleBox.getChildren().addAll(title, subtitle);

        HBox navButtons = new HBox(15);
        navButtons.setAlignment(Pos.CENTER_LEFT);
        
        Button dashboardBtn = createNavButton("🏠 Dashboard", true);
        Button scheduleBtn = createNavButton("📅 Schedule", false);
        scheduleBtn.setOnAction(e -> openScheduleView());
        Button employeesBtn = createNavButton("👥 Employees", false);
        employeesBtn.setOnAction(e -> openEmployeesView());
        Button usersBtn = createNavButton("👤 Users", false);
        usersBtn.setOnAction(e -> openUsersView());
        
        navButtons.getChildren().addAll(dashboardBtn, scheduleBtn, employeesBtn, usersBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄");
        refreshBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshDashboard());
        refreshBtn.setTooltip(new Tooltip("Refresh Dashboard"));

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-cursor: hand; -fx-background-radius: 5;");
        logoutBtn.setOnAction(e -> logout());

        topBar.getChildren().addAll(logo, titleBox, navButtons, spacer, refreshBtn, logoutBtn);
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

        VBox welcomeBox = createWelcomeHeader();
        HBox statsCards = createStatsCards();
        
        HBox bottomSection = new HBox(25);
        VBox upcomingShifts = createUpcomingShiftsSection();
        VBox teamMembers = createTeamMembersSection();
        
        HBox.setHgrow(upcomingShifts, Priority.ALWAYS);
        bottomSection.getChildren().addAll(upcomingShifts, teamMembers);

        mainContent.getChildren().addAll(welcomeBox, statsCards, bottomSection);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");
        return scrollPane;
    }

    private VBox createWelcomeHeader() {
        VBox welcomeBox = new VBox(5);
        Label welcomeLabel = new Label("Welcome back, " + username);
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        
        Label subtitleLabel = new Label("Here's what's happening with your team today.");
        subtitleLabel.setFont(Font.font("Arial", 16));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        welcomeBox.getChildren().addAll(welcomeLabel, subtitleLabel);
        return welcomeBox;
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        try {
            Map<String, Object> stats = controller.getDashboardStats();
            
            int totalEmployees = (int) stats.getOrDefault("totalEmployees", 0);
            VBox card1 = createStatCard("👥", "#dbeafe", "Total Employees", String.valueOf(totalEmployees));
            
            long supervisors = (long) stats.getOrDefault("supervisors", 0L);
            VBox card2 = createStatCard("🎯", "#fef3c7", "Supervisors", String.valueOf(supervisors));
            
            int weekGames = (int) stats.getOrDefault("currentWeekGames", 0);
            VBox card3 = createStatCard("📅", "#d1fae5", "This Week's Games", String.valueOf(weekGames));

            HBox.setHgrow(card1, Priority.ALWAYS);
            HBox.setHgrow(card2, Priority.ALWAYS);
            HBox.setHgrow(card3, Priority.ALWAYS);

            statsBox.getChildren().addAll(card1, card2, card3);
        } catch (Exception e) {
            System.err.println("Error loading stats: " + e.getMessage());
            VBox card1 = createStatCard("👥", "#dbeafe", "Total Employees", "0");
            VBox card2 = createStatCard("🎯", "#fef3c7", "Supervisors", "0");
            VBox card3 = createStatCard("📅", "#d1fae5", "This Week's Games", "0");
            statsBox.getChildren().addAll(card1, card2, card3);
        }

        return statsBox;
    }

    private VBox createStatCard(String icon, String iconBg, String title, String value) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setAlignment(Pos.TOP_LEFT);
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

    private VBox createUpcomingShiftsSection() {
        VBox section = new VBox(20);
        section.setPrefWidth(750);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Schedule Overview");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button createBtn = new Button("+ Create Shift");
        createBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 6 16 6 16; -fx-cursor: hand; -fx-font-weight: bold;");
        createBtn.setOnAction(e -> openCreateShift());

        header.getChildren().addAll(titleLabel, spacer, createBtn);

        upcomingShiftsContent = new VBox(15);
        loadUpcomingShifts();

        section.getChildren().addAll(header, upcomingShiftsContent);
        return section;
    }

    private void loadUpcomingShifts() {
        upcomingShiftsContent.getChildren().clear();
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusMonths(1);
            List<Schedule.Game> games = new GameScheduleDAO().getByDateRange(today, endDate);
            
            if (games.isEmpty()) {
                Label emptyLabel = new Label("No upcoming shifts. Click '+ Create Shift' to add one.");
                emptyLabel.setFont(Font.font("Arial", 14));
                emptyLabel.setStyle("-fx-text-fill: #9ca3af;");
                upcomingShiftsContent.getChildren().add(emptyLabel);
                return;
            }

            for (Schedule.Game game : games) {
                VBox gameCard = createExpandableGameCard(game);
                upcomingShiftsContent.getChildren().add(gameCard);
            }
        } catch (SQLException e) {
            System.err.println("Error loading shifts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NEW: Create expandable/collapsible game card
     */
    private VBox createExpandableGameCard(Schedule.Game game) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

        // Header (always visible, clickable)
        HBox header = createGameCardHeader(game);
        header.setStyle("-fx-padding: 15; -fx-cursor: hand; -fx-background-radius: 8 8 0 0;");
        header.setOnMouseClicked(e -> toggleGameExpansion(game.getScheduleId(), card));
        
        // Hover effect
        header.setOnMouseEntered(e -> header.setStyle("-fx-padding: 15; -fx-cursor: hand; " +
                "-fx-background-color: #f9fafb; -fx-background-radius: 8 8 0 0;"));
        header.setOnMouseExited(e -> header.setStyle("-fx-padding: 15; -fx-cursor: hand; " +
                "-fx-background-radius: 8 8 0 0;"));

        card.getChildren().add(header);
        
        // Check if this game should be expanded
        if (expandedGames.contains(game.getScheduleId())) {
            VBox detailsBox = createGameDetails(game);
            card.getChildren().add(detailsBox);
        }

        return card;
    }
    
    /**
     * NEW: Create game card header with expand/collapse indicator
     */
    private HBox createGameCardHeader(Schedule.Game game) {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Expand/collapse indicator
        Label expandIcon = new Label(expandedGames.contains(game.getScheduleId()) ? "▼" : "▶");
        expandIcon.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        expandIcon.setStyle("-fx-text-fill: #6b7280;");
        expandIcon.setPrefWidth(20);

        // Get sport name
        String sportName = "Sport";
        try {
            Sport sport = new SportDAO().getById(game.getSportId());
            if (sport != null) {
                sportName = sport.getSportName();
            }
        } catch (SQLException e) {
            System.err.println("Error loading sport: " + e.getMessage());
        }

        Label sportLabel = new Label("⚽ " + sportName);
        sportLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        sportLabel.setStyle("-fx-text-fill: #3b82f6;");

        Label dateLabel = new Label(game.getGameDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label timeLabel = new Label(game.getStartTime().format(DateTimeFormatter.ofPattern("h:mm a")) + 
                                    " - " + game.getEndTime().format(DateTimeFormatter.ofPattern("h:mm a")));
        timeLabel.setFont(Font.font("Arial", 13));
        timeLabel.setStyle("-fx-text-fill: #6b7280;");

        Label locationLabel = new Label("📍 " + game.getLocation());
        locationLabel.setFont(Font.font("Arial", 13));
        locationLabel.setStyle("-fx-text-fill: #6b7280;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // NEW: Show supervisor and referee counts
        try {
            ShiftDAO shiftDAO = new ShiftDAO();
            List<Schedule.Shift> shifts = shiftDAO.getByGameSchedule(game.getScheduleId());
            
            long assignedSupervisors = shifts.stream()
                .filter(s -> s.getPositionType() == Schedule.PositionType.SUPERVISOR)
                .filter(s -> s.getAssignedEmployeeId() != null)
                .count();
            
            long assignedReferees = shifts.stream()
                .filter(s -> s.getPositionType() == Schedule.PositionType.REFEREE)
                .filter(s -> s.getAssignedEmployeeId() != null)
                .count();
            
            Label supervisorCount = new Label("👔 " + assignedSupervisors + "/" + game.getRequiredSupervisors());
            supervisorCount.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            supervisorCount.setStyle(assignedSupervisors == game.getRequiredSupervisors() ? 
                    "-fx-text-fill: #10b981;" : "-fx-text-fill: #f59e0b;");
            
            Label refereeCount = new Label("🏃 " + assignedReferees + "/" + game.getRequiredReferees());
            refereeCount.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            refereeCount.setStyle(assignedReferees == game.getRequiredReferees() ? 
                    "-fx-text-fill: #10b981;" : "-fx-text-fill: #f59e0b;");
            
            header.getChildren().addAll(expandIcon, sportLabel, dateLabel, timeLabel, locationLabel, 
                                       spacer, supervisorCount, refereeCount);
        } catch (SQLException e) {
            header.getChildren().addAll(expandIcon, sportLabel, dateLabel, timeLabel, locationLabel, spacer);
        }

        return header;
    }
    
    /**
     * NEW: Toggle game expansion
     */
    private void toggleGameExpansion(int gameId, VBox card) {
        if (expandedGames.contains(gameId)) {
            expandedGames.remove(gameId);
        } else {
            expandedGames.add(gameId);
        }
        refreshDashboard();
    }
    
    /**
     * NEW: Create detailed view for expanded game
     */
    private VBox createGameDetails(Schedule.Game game) {
        VBox details = new VBox(15);
        details.setPadding(new Insets(15));
        details.setStyle("-fx-background-color: #f9fafb; -fx-border-width: 1 0 0 0; " +
                "-fx-border-color: #e5e7eb;");

        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER_LEFT);
        
        Button genRecsBtn = new Button("⚡ Generate Recommendations");
        genRecsBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand; -fx-font-weight: bold;");
        genRecsBtn.setOnAction(e -> generateRecommendationsForGame(game));
        
        Button editBtn = new Button("✏️ Edit Shift");
        editBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand; -fx-font-weight: bold;");
        editBtn.setOnAction(e -> editGame(game));
        
        actionButtons.getChildren().addAll(genRecsBtn, editBtn);
        details.getChildren().add(actionButtons);

        // Show shifts with recommendations or assignments
        try {
            ShiftDAO shiftDAO = new ShiftDAO();
            List<Schedule.Shift> shifts = shiftDAO.getByGameSchedule(game.getScheduleId());
            
            if (shifts.isEmpty()) {
                Label noShifts = new Label("No shifts found for this game");
                noShifts.setStyle("-fx-text-fill: #9ca3af;");
                details.getChildren().add(noShifts);
                return details;
            }
            
            // Check if recommendations exist
            boolean hasRecommendations = shifts.stream()
                .anyMatch(s -> s.getRecommendationAId() != null);
            
            // Check if assignments exist
            boolean hasAssignments = shifts.stream()
                .anyMatch(s -> s.getAssignedEmployeeId() != null);
            
            if (hasAssignments) {
                // Show final assignments
                details.getChildren().add(createAssignmentsView(shifts));
            } else if (hasRecommendations) {
                // Show recommendations with radio buttons
                details.getChildren().add(createRecommendationsView(game, shifts));
            } else {
                Label noRecs = new Label("Click 'Generate Recommendations' to get staffing suggestions");
                noRecs.setFont(Font.font("Arial", 13));
                noRecs.setStyle("-fx-text-fill: #6b7280;");
                details.getChildren().add(noRecs);
            }
            
        } catch (SQLException e) {
            Label error = new Label("Error loading shift details");
            error.setStyle("-fx-text-fill: #ef4444;");
            details.getChildren().add(error);
        }

        return details;
    }
    
    /**
     * NEW: Show final assignments (after finalization)
     */
    private VBox createAssignmentsView(List<Schedule.Shift> shifts) {
        VBox assignmentsBox = new VBox(12);
        
        Label title = new Label("✅ Finalized Assignments:");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setStyle("-fx-text-fill: #10b981;");
        
        assignmentsBox.getChildren().add(title);
        
        try {
            EmployeeDAO empDAO = new EmployeeDAO();
            
            for (Schedule.Shift shift : shifts) {
                if (shift.getAssignedEmployeeId() != null) {
                    Employee emp = empDAO.getById(shift.getAssignedEmployeeId());
                    if (emp != null) {
                        HBox assignmentRow = new HBox(15);
                        assignmentRow.setAlignment(Pos.CENTER_LEFT);
                        assignmentRow.setPadding(new Insets(10));
                        assignmentRow.setStyle("-fx-background-color: #d1fae5; -fx-background-radius: 6;");
                        
                        Label posLabel = new Label(shift.getPositionType() + " #" + shift.getPositionNumber());
                        posLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                        posLabel.setPrefWidth(120);
                        
                        Label empLabel = new Label("→ " + emp.getFirstName() + " " + emp.getLastName());
                        empLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                        empLabel.setStyle("-fx-text-fill: #047857;");
                        
                        assignmentRow.getChildren().addAll(posLabel, empLabel);
                        assignmentsBox.getChildren().add(assignmentRow);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return assignmentsBox;
    }
    
    /**
     * NEW: Show recommendations with radio buttons (before finalization)
     */
    private VBox createRecommendationsView(Schedule.Game game, List<Schedule.Shift> shifts) {
        VBox recsBox = new VBox(12);
        
        Label title = new Label("📋 Recommended Assignments - Select to Assign:");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        title.setStyle("-fx-text-fill: #374151;");
        
        recsBox.getChildren().add(title);
        
        // Store toggle groups for assignment
        Map<Schedule.Shift, ToggleGroup> shiftToggleGroups = new HashMap<>();
        
        for (Schedule.Shift shift : shifts) {
            if (shift.getRecommendationAId() != null) {
                VBox recRow = createRecommendationRowWithRadio(shift, shiftToggleGroups);
                if (recRow != null) {
                    recsBox.getChildren().add(recRow);
                }
            }
        }
        
        // Finalize button
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button finalizeBtn = new Button("✓ Finalize Assignments");
        finalizeBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand; " +
                "-fx-font-weight: bold; -fx-font-size: 12px;");
        finalizeBtn.setOnAction(e -> finalizeAssignments(game, shifts, shiftToggleGroups));
        
        buttonBox.getChildren().add(finalizeBtn);
        recsBox.getChildren().add(buttonBox);
        
        return recsBox;
    }
    
    /**
     * Create recommendation row with radio buttons
     */
    private VBox createRecommendationRowWithRadio(Schedule.Shift shift, Map<Schedule.Shift, ToggleGroup> toggleGroups) {
        try {
            EmployeeDAO empDAO = new EmployeeDAO();
            Employee optionA = empDAO.getById(shift.getRecommendationAId());
            Employee optionB = shift.getRecommendationBId() != null ? 
                empDAO.getById(shift.getRecommendationBId()) : null;
            
            if (optionA == null) return null;
            
            VBox container = new VBox(8);
            container.setPadding(new Insets(8));
            container.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; " +
                    "-fx-border-radius: 6; -fx-background-radius: 6;");
            
            // Position header
            Label posLabel = new Label(shift.getPositionType() + " #" + shift.getPositionNumber());
            posLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            posLabel.setStyle("-fx-text-fill: #1f2937;");
            
            // Create toggle group
            ToggleGroup toggleGroup = new ToggleGroup();
            toggleGroups.put(shift, toggleGroup);
            
            // Options container
            VBox optionsContainer = new VBox(5);
            optionsContainer.getChildren().add(createRadioOptionBox(optionA, "Option A", toggleGroup, true));
            
            if (optionB != null && optionB.getEmployeeId()!=(optionA.getEmployeeId())) {
                optionsContainer.getChildren().add(createRadioOptionBox(optionB, "Option B", toggleGroup, false));
            }
            
            container.getChildren().addAll(posLabel, optionsContainer);
            return container;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Create radio button option box
     */
    private HBox createRadioOptionBox(Employee employee, String label, ToggleGroup group, boolean selected) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(8));
        box.setStyle("-fx-background-color: " + (label.equals("Option A") ? "#dbeafe" : "#fef3c7") + "; " +
                "-fx-background-radius: 4;");
        
        RadioButton radio = new RadioButton();
        radio.setToggleGroup(group);
        radio.setSelected(selected);
        radio.setUserData(employee.getEmployeeId());
        
        VBox infoBox = new VBox(3);
        Label nameLabel = new Label(label + ": " + employee.getFirstName() + " " + employee.getLastName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        nameLabel.setStyle("-fx-text-fill: " + (label.equals("Option A") ? "#1e40af" : "#92400e"));
        
        Label detailsLabel = new Label(
            (employee.isSupervisorEligible() ? "Supervisor • " : "Staff • ") +
            employee.getMaxHoursPerWeek() + " hrs/week max"
        );
        detailsLabel.setFont(Font.font("Arial", 10));
        detailsLabel.setStyle("-fx-text-fill: #6b7280;");
        
        infoBox.getChildren().addAll(nameLabel, detailsLabel);
        
        box.getChildren().addAll(radio, infoBox);
        return box;
    }
    
    /**
     * Finalize assignments based on radio button selections
     */
    private void finalizeAssignments(Schedule.Game game, List<Schedule.Shift> shifts, 
                                     Map<Schedule.Shift, ToggleGroup> toggleGroups) {
        try {
            int assignedCount = 0;
            StringBuilder message = new StringBuilder("Assignments:\n\n");
            
            for (Schedule.Shift shift : shifts) {
                ToggleGroup group = toggleGroups.get(shift);
                if (group != null && group.getSelectedToggle() != null) {
                    int employeeId = (int) group.getSelectedToggle().getUserData();
                    
                    // Assign the shift
                    schedulingController.assignShift(shift, employeeId, game);
                    
                    // Get employee name for feedback
                    EmployeeDAO empDAO = new EmployeeDAO();
                    Employee emp = empDAO.getById(employeeId);
                    message.append(shift.getPositionType()).append(" #").append(shift.getPositionNumber())
                           .append(" → ").append(emp.getFirstName()).append(" ").append(emp.getLastName()).append("\n");
                    
                    assignedCount++;
                }
            }
            
            if (assignedCount > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Assignments Finalized!");
                alert.setContentText(message.toString() + "\n" + assignedCount + " shifts assigned successfully.");
                alert.showAndWait();
                
                refreshDashboard();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Selections");
                alert.setHeaderText(null);
                alert.setContentText("Please select employees for the shifts before finalizing.");
                alert.showAndWait();
            }
            
        } catch (Exception e) {
            System.err.println("Error finalizing assignments: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to finalize assignments: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private void generateRecommendationsForGame(Schedule.Game game) {
        try {
            if (schedulingController.getCurrentCycle() == null) {
                schedulingController.createCycle(game.getScheduleCycleStart(), game.getScheduleCycleEnd());
            }
            
            if (!schedulingController.getCurrentCycle().getGameSchedules().contains(game)) {
                schedulingController.getCurrentCycle().addGameSchedule(game);
            }
            
            schedulingController.autoGenerateRecommendations(game);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Recommendations generated successfully!\n\n" +
                    "Different employees recommended for each position.\n" +
                    "Select your preferred option and click 'Finalize Assignments'.");
            alert.showAndWait();
            
            refreshDashboard();
        } catch (Exception e) {
            System.err.println("Error generating recommendations: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to generate recommendations: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * NEW: Edit game details
     */
    private void editGame(Schedule.Game game) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Shift");
        alert.setHeaderText("Edit functionality coming soon");
        alert.setContentText("This will allow you to modify:\n" +
                "- Date and time\n" +
                "- Location\n" +
                "- Number of supervisors/referees");
        alert.showAndWait();
        // TODO: Implement edit dialog
    }

    private VBox createTeamMembersSection() {
        VBox section = new VBox(20);
        section.setPrefWidth(350);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Team Members");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ Add");
        addBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 6 16; -fx-cursor: hand; -fx-font-weight: bold;");
        addBtn.setOnAction(e -> {
            AddEmployeeModal modal = new AddEmployeeModal(primaryStage);
            modal.setOnSuccess(() -> Platform.runLater(() -> refreshTeamMembers()));
            modal.show();
        });

        header.getChildren().addAll(titleLabel, spacer, addBtn);

        teamMembersContent = new VBox(15);
        loadTeamMembers();

        section.getChildren().addAll(header, teamMembersContent);
        return section;
    }

    private void loadTeamMembers() {
        teamMembersContent.getChildren().clear();
        
        try {
            List<Employee> employees = controller.getAllEmployees();
            
            if (employees.isEmpty()) {
                Label emptyLabel = new Label("No employees yet. Click '+ Add' to add your first employee!");
                emptyLabel.setFont(Font.font("Arial", 13));
                emptyLabel.setStyle("-fx-text-fill: #9ca3af;");
                emptyLabel.setWrapText(true);
                teamMembersContent.getChildren().add(emptyLabel);
                return;
            }

            for (Employee employee : employees) {
                VBox card = createExpandableEmployeeCard(employee);
                teamMembersContent.getChildren().add(card);
            }
        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading employees");
            teamMembersContent.getChildren().add(errorLabel);
        }
    }

    /**
     * NEW: Create expandable employee card
     */
    private VBox createExpandableEmployeeCard(Employee employee) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

        // Header (always visible, clickable)
        HBox header = createEmployeeCardHeader(employee);
        header.setStyle("-fx-padding: 15; -fx-cursor: hand;");
        header.setOnMouseClicked(e -> toggleEmployeeExpansion(employee.getEmployeeId(), card));
        
        // Hover effect
        header.setOnMouseEntered(e -> header.setStyle("-fx-padding: 15; -fx-cursor: hand; " +
                "-fx-background-color: #f9fafb;"));
        header.setOnMouseExited(e -> header.setStyle("-fx-padding: 15; -fx-cursor: hand;"));

        card.getChildren().add(header);
        
        // Check if expanded
        if (expandedEmployees.contains(employee.getEmployeeId())) {
            VBox details = createEmployeeDetails(employee);
            card.getChildren().add(details);
        }

        return card;
    }
    
    /**
     * NEW: Create employee card header
     */
    private HBox createEmployeeCardHeader(Employee employee) {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Expand indicator
        Label expandIcon = new Label(expandedEmployees.contains(employee.getEmployeeId()) ? "▼" : "▶");
        expandIcon.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        expandIcon.setStyle("-fx-text-fill: #6b7280;");
        expandIcon.setPrefWidth(15);

        Label avatar = new Label(employee.getFirstName().substring(0, 1).toUpperCase());
        avatar.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        avatar.setPrefSize(40, 40);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 20;");

        VBox nameBox = new VBox(3);
        Label nameLabel = new Label(employee.getFirstName() + " " + employee.getLastName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        Label roleLabel = new Label(employee.isSupervisorEligible() ? "Supervisor" : "Staff");
        roleLabel.setFont(Font.font("Arial", 11));
        roleLabel.setStyle("-fx-text-fill: #6b7280;");
        nameBox.getChildren().addAll(nameLabel, roleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label("Active");
        badge.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        badge.setPadding(new Insets(3, 8, 3, 8));
        badge.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 10;");

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
        refreshTeamMembers();
    }
    
    /**
     * NEW: Create employee details view (sports and availability)
     */
    private VBox createEmployeeDetails(Employee employee) {
        VBox details = new VBox(12);
        details.setPadding(new Insets(15));
        details.setStyle("-fx-background-color: #f9fafb; -fx-border-width: 1 0 0 0; -fx-border-color: #e5e7eb;");

        // Hours info
        Label hoursLabel = new Label("Max: " + employee.getMaxHoursPerWeek() + " hrs/week");
        hoursLabel.setFont(Font.font("Arial", 12));
        hoursLabel.setStyle("-fx-text-fill: #6b7280;");
        details.getChildren().add(hoursLabel);

        // Sports section
        try {
            EmployeeExpertiseDAO expertiseDAO = new EmployeeExpertiseDAO();
            SportDAO sportDAO = new SportDAO();
            List<Integer> sportIds = expertiseDAO.getSportIdsByEmployee(employee.getEmployeeId());
            
            if (!sportIds.isEmpty()) {
                Label sportsTitle = new Label("⚽ Sports:");
                sportsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                sportsTitle.setStyle("-fx-text-fill: #374151;");
                details.getChildren().add(sportsTitle);
                
                FlowPane sportsFlow = new FlowPane();
                sportsFlow.setHgap(8);
                sportsFlow.setVgap(8);
                
                for (Integer sportId : sportIds) {
                    Sport sport = sportDAO.getById(sportId);
                    if (sport != null) {
                        Label sportBadge = new Label(sport.getSportName());
                        sportBadge.setFont(Font.font("Arial", 10));
                        sportBadge.setPadding(new Insets(4, 10, 4, 10));
                        sportBadge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; " +
                                "-fx-background-radius: 12;");
                        sportsFlow.getChildren().add(sportBadge);
                    }
                }
                details.getChildren().add(sportsFlow);
            }
            
            // Availability section
            AvailabilityDAO availDAO = new AvailabilityDAO();
            List<Availability.Seasonal> avails = availDAO.getAllByEmployee(employee.getEmployeeId());
            
            if (!avails.isEmpty()) {
                Label availTitle = new Label("📅 Availability:");
                availTitle.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                availTitle.setStyle("-fx-text-fill: #374151;");
                details.getChildren().add(availTitle);
                
                // Group by day
                Map<DayOfWeek, List<Availability.Seasonal>> byDay = new HashMap<>();
                for (Availability.Seasonal avail : avails) {
                    byDay.computeIfAbsent(avail.getDayOfWeek(), k -> new ArrayList<>()).add(avail);
                }
                
                VBox availList = new VBox(4);
                for (DayOfWeek day : DayOfWeek.values()) {
                    if (byDay.containsKey(day)) {
                        Label dayLabel = new Label("• " + day.toString().substring(0, 3) + ": " +
                                byDay.get(day).size() + " slot(s)");
                        dayLabel.setFont(Font.font("Arial", 10));
                        dayLabel.setStyle("-fx-text-fill: #6b7280;");
                        availList.getChildren().add(dayLabel);
                    }
                }
                details.getChildren().add(availList);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Edit button
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(8, 0, 0, 0));
        
        Button editBtn = new Button("✏️ Edit Employee");
        editBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; " +
                "-fx-background-radius: 4; -fx-padding: 6 12; -fx-cursor: hand; " +
                "-fx-font-size: 11px; -fx-font-weight: bold;");
        editBtn.setOnAction(e -> editEmployee(employee));
        
        buttonBox.getChildren().add(editBtn);
        details.getChildren().add(buttonBox);

        return details;
    }
    
    /**
     * NEW: Edit employee details
     */
    private void editEmployee(Employee employee) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Employee");
        alert.setHeaderText("Edit functionality coming soon");
        alert.setContentText("This will allow you to modify:\n" +
                "- Name and supervisor status\n" +
                "- Sports expertise\n" +
                "- Availability schedule\n" +
                "- Maximum hours per week");
        alert.showAndWait();
        // TODO: Implement edit dialog
    }

    private void refreshTeamMembers() {
        loadTeamMembers();
    }
    
    private void refreshDashboard() {
        Scene scene = primaryStage.getScene();
        if (scene != null) {
            BorderPane root = (BorderPane) scene.getRoot();
            root.setCenter(createMainContent());
        }
    }
    
    private void openCreateShift() {
        CreateShiftView createShiftView = new CreateShiftView(primaryStage, userId);
        createShiftView.setOnSuccess(() -> Platform.runLater(() -> refreshDashboard()));
        createShiftView.show();
    }
    
    private void openScheduleView() {
        ScheduleBuilder scheduleView = new ScheduleBuilder(primaryStage, username, userId);
        Scene scene = scheduleView.createScene();
        primaryStage.setScene(scene);
    }
    
    private void openEmployeesView() {
        EmployeesPage employeesView = new EmployeesPage(primaryStage, username, userId);
        Scene scene = employeesView.createScene();
        primaryStage.setScene(scene);
    }

    private void openUsersView() {
        UserManagementView usersView = new UserManagementView(primaryStage, username, userId);
        Scene scene = usersView.createScene();
        primaryStage.setScene(scene);
    }

    private void logout() {
        LoginView loginView = new LoginView(primaryStage);
        primaryStage.setScene(loginView.createScene());
        primaryStage.setTitle("Employee Scheduling System - Login");
    }
}