package com.intramural.scheduling.view;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

/**
 * UPDATED ScheduleBuilder - Shows actual assignments and includes delete functionality
 * - Removed "Generate Recommendations" buttons
 * - Shows WHO is assigned to each position
 * - Added "Delete Shift" functionality
 * - Fixed stats to show real-time database values
 */
public class ScheduleBuilder {
    private Stage primaryStage;
    private String username;
    private int userId;
    private LocalDate currentMonth;
    private SchedulingController schedulingController;
    private VBox scheduleContent;

    public ScheduleBuilder(Stage primaryStage, String username, int userId) {
        this.primaryStage = primaryStage;
        this.username = username;
        this.userId = userId;
        this.currentMonth = LocalDate.now();
        this.schedulingController = new SchedulingController();
        
        LocalDate cycleStart = LocalDate.now().withDayOfMonth(1);
        LocalDate cycleEnd = cycleStart.plusMonths(1).minusDays(1);
        schedulingController.createCycle(cycleStart, cycleEnd);
        
        try {
            schedulingController.loadGameSchedules();
        } catch (SQLException e) {
            System.err.println("Failed to load schedules: " + e.getMessage());
        }
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");
        root.setTop(createTopBar());
        root.setCenter(createMainContent());
        return new Scene(root, 1400, 900);
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
        Label subtitleLabel = new Label("Schedule Management");
        subtitleLabel.setFont(Font.font("Arial", 11));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        HBox navButtons = new HBox(15);
        navButtons.setAlignment(Pos.CENTER_LEFT);
        
        Button dashboardBtn = createNavButton("🏠 Dashboard", false);
        dashboardBtn.setOnAction(e -> navigateToDashboard());
        
        Button scheduleBtn = createNavButton("📅 Schedule", true);
        
        Button employeesBtn = createNavButton("👥 Employees", false);
        employeesBtn.setOnAction(e -> {
            EmployeesPage employeesView = new EmployeesPage(primaryStage, username, userId);
            primaryStage.setScene(employeesView.createScene());
        });
        
        Button usersBtn = createNavButton("👤 Users", false);
        usersBtn.setOnAction(e -> {
            UserManagementView userView = new UserManagementView(primaryStage, username, userId);
            primaryStage.setScene(userView.createScene());
        });

        navButtons.getChildren().addAll(dashboardBtn, scheduleBtn, employeesBtn, usersBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄");
        refreshBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshSchedule());
        refreshBtn.setTooltip(new Tooltip("Refresh Schedule"));

        topBar.getChildren().addAll(logo, titleBox, navButtons, spacer, refreshBtn);
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

        HBox header = createHeaderSection();
        HBox statsCards = createStatsCards();
        VBox calendarSection = createCalendarSection();

        mainContent.getChildren().addAll(header, statsCards, calendarSection);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");
        return scrollPane;
    }

    private HBox createHeaderSection() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(5);
        Label titleLabel = new Label("Schedule Dashboard");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        
        Label subtitleLabel = new Label("Manage game schedules and staff assignments");
        subtitleLabel.setFont(Font.font("Arial", 16));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // REMOVED: "Generate All Recommendations" button
        Button createBtn = new Button("+ Create Shift");
        createBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 12 24 12 24; -fx-cursor: hand; " +
                "-fx-font-size: 14px; -fx-font-weight: bold;");
        createBtn.setOnAction(e -> openCreateShift());

        header.getChildren().addAll(titleBox, spacer, createBtn);
        return header;
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        try {
            // Load actual data from database for current month
            GameScheduleDAO gameDAO = new GameScheduleDAO();
            ShiftDAO shiftDAO = new ShiftDAO();
            
            LocalDate monthStart = currentMonth.withDayOfMonth(1);
            LocalDate monthEnd = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth());
            
            List<Schedule.Game> games = gameDAO.getByDateRange(monthStart, monthEnd);
            
            // Calculate real-time stats
            int totalShifts = 0;
            int fullyStaffed = 0;
            int partiallyStaffed = 0;
            int unstaffed = 0;
            
            for (Schedule.Game game : games) {
                // Load actual shifts from database
                List<Schedule.Shift> shifts = shiftDAO.getByGameSchedule(game.getScheduleId());
                
                if (!shifts.isEmpty()) {
                    // Count assigned vs total positions
                    long assignedCount = shifts.stream()
                        .filter(s -> s.getAssignedEmployeeId() != null)
                        .count();
                    
                    int totalPositions = shifts.size();
                    
                    totalShifts++;
                    
                    if (assignedCount == totalPositions && totalPositions > 0) {
                        fullyStaffed++;
                    } else if (assignedCount > 0) {
                        partiallyStaffed++;
                    } else {
                        unstaffed++;
                    }
                }
            }
            
            int needsStaff = partiallyStaffed + unstaffed;
            
            VBox card1 = createSmallStatCard("📅", "#dbeafe", "Total Shifts", 
                                            String.valueOf(totalShifts));
            VBox card2 = createSmallStatCard("✅", "#d1fae5", "Fully Staffed", 
                                            String.valueOf(fullyStaffed));
            VBox card3 = createSmallStatCard("⚠️", "#fef3c7", "Needs Staff", 
                                            String.valueOf(needsStaff));
            VBox card4 = createSmallStatCard("❌", "#fee2e2", "Unassigned", 
                                            String.valueOf(unstaffed));

            HBox.setHgrow(card1, Priority.ALWAYS);
            HBox.setHgrow(card2, Priority.ALWAYS);
            HBox.setHgrow(card3, Priority.ALWAYS);
            HBox.setHgrow(card4, Priority.ALWAYS);

            statsBox.getChildren().addAll(card1, card2, card3, card4);
            
        } catch (SQLException e) {
            System.err.println("Error loading stats: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to zeros on error
            VBox card1 = createSmallStatCard("📅", "#dbeafe", "Total Shifts", "0");
            VBox card2 = createSmallStatCard("✅", "#d1fae5", "Fully Staffed", "0");
            VBox card3 = createSmallStatCard("⚠️", "#fef3c7", "Needs Staff", "0");
            VBox card4 = createSmallStatCard("❌", "#fee2e2", "Unassigned", "0");
            
            statsBox.getChildren().addAll(card1, card2, card3, card4);
        }
        
        return statsBox;
    }

    private VBox createSmallStatCard(String icon, String iconBg, String title, String value) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setMaxWidth(Double.MAX_VALUE);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(32));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 13));
        titleLabel.setStyle("-fx-text-fill: #6b7280;");

        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        return card;
    }

    private VBox createCalendarSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        HBox calendarHeader = new HBox();
        calendarHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label monthLabel = new Label(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + 
                                     " " + currentMonth.getYear());
        monthLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button prevBtn = new Button("◀");
        prevBtn.setStyle("-fx-background-color: #f3f4f6; -fx-cursor: hand;");
        prevBtn.setOnAction(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshSchedule();
        });

        Button nextBtn = new Button("▶");
        nextBtn.setStyle("-fx-background-color: #f3f4f6; -fx-cursor: hand;");
        nextBtn.setOnAction(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshSchedule();
        });

        HBox navButtons = new HBox(10);
        navButtons.getChildren().addAll(prevBtn, nextBtn);

        calendarHeader.getChildren().addAll(monthLabel, spacer, navButtons);

        scheduleContent = new VBox(15);
        loadScheduleContent();

        section.getChildren().addAll(calendarHeader, scheduleContent);
        return section;
    }

    private void loadScheduleContent() {
        scheduleContent.getChildren().clear();
        
        try {
            // Load all games from database directly
            GameScheduleDAO gameDAO = new GameScheduleDAO();
            LocalDate monthStart = currentMonth.withDayOfMonth(1);
            LocalDate monthEnd = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth());
            
            List<Schedule.Game> games = gameDAO.getByDateRange(monthStart, monthEnd);
            
            System.out.println("Loaded " + games.size() + " games for " + currentMonth.getMonth());
            
            if (games.isEmpty()) {
                Label emptyLabel = new Label("No shifts scheduled for this period. Click '+ Create Shift' to add one.");
                emptyLabel.setFont(Font.font("Arial", 14));
                emptyLabel.setStyle("-fx-text-fill: #9ca3af;");
                scheduleContent.getChildren().add(emptyLabel);
                return;
            }

            for (Schedule.Game game : games) {
                VBox gameCard = createDetailedGameCard(game);
                scheduleContent.getChildren().add(gameCard);
            }
        } catch (SQLException e) {
            System.err.println("Error loading schedule: " + e.getMessage());
            e.printStackTrace();
            Label errorLabel = new Label("Error loading schedule: " + e.getMessage());
            scheduleContent.getChildren().add(errorLabel);
        }
    }

    /**
     * UPDATED: Now shows actual assignments from database
     */
    private VBox createDetailedGameCard(Schedule.Game game) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; " +
                "-fx-border-radius: 10; -fx-background-radius: 10;");

        // Top row with sport, date, time, location
        HBox topRow = new HBox(20);
        topRow.setAlignment(Pos.CENTER_LEFT);

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
        sportLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        sportLabel.setStyle("-fx-text-fill: #3b82f6;");

        Label dateLabel = new Label(game.getGameDate().format(DateTimeFormatter.ofPattern("EEE, MMM dd")));
        dateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label timeLabel = new Label(game.getStartTime().format(DateTimeFormatter.ofPattern("h:mm a")) + 
                                    " - " + game.getEndTime().format(DateTimeFormatter.ofPattern("h:mm a")));
        timeLabel.setFont(Font.font("Arial", 14));
        timeLabel.setStyle("-fx-text-fill: #6b7280;");

        Label locationLabel = new Label("📍 " + game.getLocation());
        locationLabel.setFont(Font.font("Arial", 14));
        locationLabel.setStyle("-fx-text-fill: #6b7280;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Load actual shifts from database to get accurate counts
        int assigned = 0;
        int total = 0;
        try {
            ShiftDAO shiftDAO = new ShiftDAO();
            List<Schedule.Shift> shifts = shiftDAO.getByGameSchedule(game.getScheduleId());
            total = shifts.size();
            assigned = (int) shifts.stream()
                .filter(s -> s.getAssignedEmployeeId() != null)
                .count();
        } catch (SQLException e) {
            System.err.println("Error loading shifts: " + e.getMessage());
        }
        
        Label staffLabel = new Label(assigned + "/" + total + " Positions Filled");
        staffLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        staffLabel.setStyle(assigned == total && total > 0 ? "-fx-text-fill: #10b981;" : "-fx-text-fill: #f59e0b;");

        // CHANGED: "Delete Shift" button instead of "Generate Recommendations"
        Button deleteBtn = new Button("🗑️ Delete Shift");
        deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 8 16 8 16; -fx-cursor: hand; -fx-font-weight: bold;");
        deleteBtn.setOnAction(e -> deleteShift(game));

        topRow.getChildren().addAll(sportLabel, dateLabel, timeLabel, locationLabel, spacer, staffLabel, deleteBtn);

        // NEW: Show actual assignments
        VBox assignmentsSection = createAssignmentsSection(game);
        
        // Shifts breakdown
        HBox shiftsRow = new HBox(30);
        shiftsRow.setPadding(new Insets(10, 0, 0, 0));
        
        Label supervisorsLabel = new Label("🏆 Supervisors: " + game.getRequiredSupervisors());
        supervisorsLabel.setFont(Font.font("Arial", 13));
        
        Label refereesLabel = new Label("🏃 Referees: " + game.getRequiredReferees());
        refereesLabel.setFont(Font.font("Arial", 13));
        
        shiftsRow.getChildren().addAll(supervisorsLabel, refereesLabel);

        card.getChildren().addAll(topRow, new Separator(), shiftsRow, assignmentsSection);
        return card;
    }

    /**
     * NEW: Create assignments section showing WHO is assigned to each position
     */
    private VBox createAssignmentsSection(Schedule.Game game) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10, 0, 0, 0));
        
        try {
            ShiftDAO shiftDAO = new ShiftDAO();
            EmployeeDAO empDAO = new EmployeeDAO();
            List<Schedule.Shift> shifts = shiftDAO.getByGameSchedule(game.getScheduleId());
            
            if (shifts.isEmpty()) {
                Label noShifts = new Label("No positions created for this game");
                noShifts.setFont(Font.font("Arial", 12));
                noShifts.setStyle("-fx-text-fill: #9ca3af;");
                section.getChildren().add(noShifts);
                return section;
            }
            
            // Check if any assignments exist
            boolean hasAssignments = shifts.stream()
                .anyMatch(s -> s.getAssignedEmployeeId() != null);
            
            if (!hasAssignments) {
                Label noAssignments = new Label("💡 No staff assigned yet. Assign staff in Admin Dashboard.");
                noAssignments.setFont(Font.font("Arial", 12));
                noAssignments.setStyle("-fx-text-fill: #6b7280; -fx-font-style: italic;");
                section.getChildren().add(noAssignments);
                return section;
            }
            
            // Show assignments
            Label assignmentsTitle = new Label("📋 Assigned Staff:");
            assignmentsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            assignmentsTitle.setStyle("-fx-text-fill: #374151;");
            section.getChildren().add(assignmentsTitle);
            
            VBox assignmentsList = new VBox(8);
            
            for (Schedule.Shift shift : shifts) {
                if (shift.getAssignedEmployeeId() != null) {
                    Employee emp = empDAO.getById(shift.getAssignedEmployeeId());
                    if (emp != null) {
                        HBox assignmentRow = createAssignmentRow(shift, emp);
                        assignmentsList.getChildren().add(assignmentRow);
                    }
                }
            }
            
            section.getChildren().add(assignmentsList);
            
        } catch (SQLException e) {
            e.printStackTrace();
            Label error = new Label("Error loading assignments");
            error.setStyle("-fx-text-fill: #ef4444;");
            section.getChildren().add(error);
        }
        
        return section;
    }

    /**
     * NEW: Create assignment row showing position and assigned employee
     */
    private HBox createAssignmentRow(Schedule.Shift shift, Employee employee) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-color: #d1fae5; -fx-background-radius: 6;");
        
        // Position label
        Label posLabel = new Label(shift.getPositionType() + " #" + shift.getPositionNumber());
        posLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        posLabel.setStyle("-fx-text-fill: #065f46;");
        posLabel.setPrefWidth(120);
        
        // Arrow
        Label arrow = new Label("→");
        arrow.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        arrow.setStyle("-fx-text-fill: #047857;");
        
        // Employee name
        Label empLabel = new Label(employee.getFirstName() + " " + employee.getLastName());
        empLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        empLabel.setStyle("-fx-text-fill: #047857;");
        
//        // Role badge
//        Label roleBadge = new Label(employee.isSupervisorEligible() ? "Supervisor" : "Staff");
//        roleBadge.setFont(Font.font("Arial", FontWeight.BOLD, 10));
//        roleBadge.setPadding(new Insets(3, 8, 3, 8));
//        roleBadge.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-background-radius: 10;");
//        
        row.getChildren().addAll(posLabel, arrow, empLabel);
        return row;
    }

    /**
     * NEW: Delete shift functionality
     */
    private void deleteShift(Schedule.Game game) {
        // Confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Shift");
        confirmation.setHeaderText("Are you sure you want to delete this shift?");
        confirmation.setContentText(
            "Game: " + game.getLocation() + "\n" +
            "Date: " + game.getGameDate() + "\n" +
            "Time: " + game.getStartTime() + " - " + game.getEndTime() + "\n\n" +
            "This will delete all positions and cannot be undone."
        );
        
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmation.getButtonTypes().setAll(deleteButton, cancelButton);
        
        Optional<ButtonType> result = confirmation.showAndWait();
        
        if (result.isPresent() && result.get() == deleteButton) {
            try {
                // Delete shifts first (foreign key constraint)
                ShiftDAO shiftDAO = new ShiftDAO();
                shiftDAO.deleteByGameSchedule(game.getScheduleId());
                
                // Delete game schedule
                GameScheduleDAO gameDAO = new GameScheduleDAO();
                gameDAO.delete(game.getScheduleId());
                
                // Show success
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("✅ Shift deleted successfully!");
                success.showAndWait();
                
                // Refresh the schedule
                refreshSchedule();
                
            } catch (SQLException e) {
                System.err.println("Error deleting shift: " + e.getMessage());
                e.printStackTrace();
                
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("Failed to delete shift");
                error.setContentText("Error: " + e.getMessage());
                error.showAndWait();
            }
        }
    }

    private void refreshSchedule() {
        Scene scene = primaryStage.getScene();
        if (scene != null) {
            BorderPane root = (BorderPane) scene.getRoot();
            root.setCenter(createMainContent());
        }
    }

    private void openCreateShift() {
        CreateShiftView createShiftView = new CreateShiftView(primaryStage, userId);
        createShiftView.setOnSuccess(() -> Platform.runLater(() -> refreshSchedule()));
        createShiftView.show();
    }

    private void navigateToDashboard() {
        AdminDashboard dashboard = new AdminDashboard(primaryStage, username, userId);
        Scene dashboardScene = dashboard.createScene();
        primaryStage.setScene(dashboardScene);
    }
}