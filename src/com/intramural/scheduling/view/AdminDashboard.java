package com.intramural.scheduling.view;

import com.intramural.scheduling.controller.AdminDashboardController;
import com.intramural.scheduling.controller.SchedulingController;
import com.intramural.scheduling.dao.EmployeeDAO;
import com.intramural.scheduling.dao.GameScheduleDAO;
import com.intramural.scheduling.dao.ShiftDAO;
import com.intramural.scheduling.dao.SportDAO;
import com.intramural.scheduling.model.Employee;
import com.intramural.scheduling.model.Schedule;
import com.intramural.scheduling.model.Sport;
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
import java.util.List;
import java.util.Map;

public class AdminDashboard {
    private Stage primaryStage;
    private String username;
    private int userId;
    private AdminDashboardController controller;
    private SchedulingController schedulingController;
    private VBox teamMembersContent;
    private VBox upcomingShiftsContent;

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
        
        navButtons.getChildren().addAll(dashboardBtn, scheduleBtn, employeesBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄");
        refreshBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshDashboard());
        refreshBtn.setTooltip(new Tooltip("Refresh Dashboard"));

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
            
            System.out.println("Loaded " + games.size() + " games from database");
            
            if (games.isEmpty()) {
                Label emptyLabel = new Label("No upcoming shifts. Click '+ Create Shift' to add one.");
                emptyLabel.setFont(Font.font("Arial", 14));
                emptyLabel.setStyle("-fx-text-fill: #9ca3af;");
                upcomingShiftsContent.getChildren().add(emptyLabel);
                return;
            }

            for (Schedule.Game game : games) {
                VBox gameCard = createGameCard(game);
                upcomingShiftsContent.getChildren().add(gameCard);
            }
        } catch (SQLException e) {
            System.err.println("Error loading shifts: " + e.getMessage());
            e.printStackTrace();
            Label errorLabel = new Label("Error loading shifts: " + e.getMessage());
            upcomingShiftsContent.getChildren().add(errorLabel);
        }
    }

    private VBox createGameCard(Schedule.Game game) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

        HBox topRow = new HBox(15);
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

        game.generateShifts();
        int assigned = game.getAssignedStaffCount();
        int total = game.getTotalStaffNeeded();
        
        Label staffLabel = new Label(assigned + "/" + total + " Staffed");
        staffLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        staffLabel.setStyle(assigned == total ? "-fx-text-fill: #10b981;" : "-fx-text-fill: #f59e0b;");

        Button genRecsBtn = new Button("Generate Recommendations");
        genRecsBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-background-radius: 4; -fx-padding: 4 12 4 12; -fx-font-size: 11px;");
        genRecsBtn.setOnAction(e -> generateRecommendationsForGame(game));

        topRow.getChildren().addAll(sportLabel, dateLabel, timeLabel, locationLabel, spacer, staffLabel, genRecsBtn);

        // Show recommendations if they exist
        VBox recommendationsBox = new VBox(8);
        recommendationsBox.setPadding(new Insets(10, 0, 0, 0));
        
        try {
            ShiftDAO shiftDAO = new ShiftDAO();
            List<Schedule.Shift> shifts = shiftDAO.getByGameSchedule(game.getScheduleId());
            boolean hasRecommendations = false;
            
            for (Schedule.Shift shift : shifts) {
                if (shift.getRecommendationAId() != null) {
                    hasRecommendations = true;
                    HBox recRow = createRecommendationRow(shift);
                    if (recRow != null) {
                        recommendationsBox.getChildren().add(recRow);
                    }
                }
            }
            
            if (hasRecommendations) {
                Label recTitle = new Label("Recommended Assignments:");
                recTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                recTitle.setStyle("-fx-text-fill: #374151;");
                recommendationsBox.getChildren().add(0, recTitle);
                card.getChildren().addAll(topRow, new Separator(), recommendationsBox);
            } else {
                card.getChildren().add(topRow);
            }
        } catch (SQLException e) {
            card.getChildren().add(topRow);
        }

        return card;
    }
    
    private HBox createRecommendationRow(Schedule.Shift shift) {
        try {
            EmployeeDAO empDAO = new EmployeeDAO();
            Employee optionA = empDAO.getById(shift.getRecommendationAId());
            Employee optionB = shift.getRecommendationBId() != null ? 
                empDAO.getById(shift.getRecommendationBId()) : null;
            
            if (optionA == null) return null;
            
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            
            Label posLabel = new Label(shift.getPositionType() + " #" + shift.getPositionNumber() + ":");
            posLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            posLabel.setStyle("-fx-text-fill: #6b7280;");
            posLabel.setPrefWidth(120);
            
            Label optionALabel = new Label("Option A: " + optionA.getFirstName() + " " + optionA.getLastName());
            optionALabel.setFont(Font.font("Arial", 11));
            optionALabel.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; " +
                    "-fx-padding: 4 10 4 10; -fx-background-radius: 4;");
            
            if (optionB != null && !optionB.getEmployeeId().equals(optionA.getEmployeeId())) {
                Label optionBLabel = new Label("Option B: " + optionB.getFirstName() + " " + optionB.getLastName());
                optionBLabel.setFont(Font.font("Arial", 11));
                optionBLabel.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; " +
                        "-fx-padding: 4 10 4 10; -fx-background-radius: 4;");
                row.getChildren().addAll(posLabel, optionALabel, optionBLabel);
            } else {
                row.getChildren().addAll(posLabel, optionALabel);
            }
            
            return row;
        } catch (SQLException e) {
            return null;
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
                    "Option A and Option B have been assigned to each position.");
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
                "-fx-background-radius: 6; -fx-padding: 6 16 6 16; -fx-cursor: hand; -fx-font-weight: bold;");
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
                VBox card = createEmployeeCard(employee);
                teamMembersContent.getChildren().add(card);
            }
        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading employees");
            teamMembersContent.getChildren().add(errorLabel);
        }
    }

    private VBox createEmployeeCard(Employee employee) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

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

        topRow.getChildren().addAll(avatar, nameBox, spacer, badge);

        Label hoursLabel = new Label("Max: " + employee.getMaxHoursPerWeek() + " hrs/week");
        hoursLabel.setFont(Font.font("Arial", 11));
        hoursLabel.setStyle("-fx-text-fill: #6b7280;");

        card.getChildren().addAll(topRow, hoursLabel);
        return card;
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
}