package com.intramural.scheduling.view;

import com.intramural.scheduling.controller.SchedulingController;
import com.intramural.scheduling.dao.GameScheduleDAO;
import com.intramural.scheduling.dao.SportDAO;
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
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

        navButtons.getChildren().addAll(dashboardBtn, scheduleBtn, employeesBtn);

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

        Button createBtn = new Button("+ Create Shift");
        createBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 12 24 12 24; -fx-cursor: hand; " +
                "-fx-font-size: 14px; -fx-font-weight: bold;");
        createBtn.setOnAction(e -> openCreateShift());

        Button generateBtn = new Button("⚡ Generate All Recommendations");
        generateBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 12 24 12 24; -fx-cursor: hand; " +
                "-fx-font-size: 14px; -fx-font-weight: bold;");
        generateBtn.setOnAction(e -> generateAllRecommendations());

        HBox buttonBox = new HBox(15);
        buttonBox.getChildren().addAll(generateBtn, createBtn);

        header.getChildren().addAll(titleBox, spacer, buttonBox);
        return header;
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        Map<String, Integer> stats = schedulingController.getStatistics();
        
        VBox card1 = createSmallStatCard("📅", "#dbeafe", "Total Shifts", 
                String.valueOf(stats.getOrDefault("total", 0)));
        VBox card2 = createSmallStatCard("✅", "#d1fae5", "Fully Staffed", 
                String.valueOf(stats.getOrDefault("fullyStaffed", 0)));
        VBox card3 = createSmallStatCard("⚠️", "#fef3c7", "Needs Staff", 
                String.valueOf(stats.getOrDefault("partiallyStaffed", 0) + stats.getOrDefault("unstaffed", 0)));
        VBox card4 = createSmallStatCard("❌", "#fee2e2", "Unassigned", 
                String.valueOf(stats.getOrDefault("unstaffed", 0)));

        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        HBox.setHgrow(card4, Priority.ALWAYS);

        statsBox.getChildren().addAll(card1, card2, card3, card4);
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

        game.generateShifts();
        int assigned = game.getAssignedStaffCount();
        int total = game.getTotalStaffNeeded();
        
        Label staffLabel = new Label(assigned + "/" + total + " Positions Filled");
        staffLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        staffLabel.setStyle(assigned == total ? "-fx-text-fill: #10b981;" : "-fx-text-fill: #f59e0b;");

        Button genRecsBtn = new Button("Generate Recommendations");
        genRecsBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 8 16 8 16; -fx-cursor: hand; -fx-font-weight: bold;");
        genRecsBtn.setOnAction(e -> generateRecommendationsForGame(game));

        topRow.getChildren().addAll(sportLabel, dateLabel, timeLabel, locationLabel, spacer, staffLabel, genRecsBtn);

        // Shifts breakdown
        HBox shiftsRow = new HBox(30);
        shiftsRow.setPadding(new Insets(10, 0, 0, 0));
        
        Label supervisorsLabel = new Label("👔 Supervisors: " + game.getRequiredSupervisors());
        supervisorsLabel.setFont(Font.font("Arial", 13));
        
        Label refereesLabel = new Label("🏃 Referees: " + game.getRequiredReferees());
        refereesLabel.setFont(Font.font("Arial", 13));
        
        shiftsRow.getChildren().addAll(supervisorsLabel, refereesLabel);

        card.getChildren().addAll(topRow, new Separator(), shiftsRow);
        return card;
    }

    private void generateRecommendationsForGame(Schedule.Game game) {
        try {
            // Set up cycle if needed
            if (schedulingController.getCurrentCycle() == null) {
                schedulingController.createCycle(game.getScheduleCycleStart(), game.getScheduleCycleEnd());
            }
            
            // Add game to cycle if not already there
            if (!schedulingController.getCurrentCycle().getGameSchedules().contains(game)) {
                schedulingController.getCurrentCycle().addGameSchedule(game);
            }
            
            schedulingController.autoGenerateRecommendations(game);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Recommendations generated successfully!");
            alert.showAndWait();
            
            refreshSchedule();
        } catch (Exception e) {
            System.err.println("Error generating recommendations: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Recommendations Generated");
            alert.setHeaderText(null);
            alert.setContentText("Recommendations generated, but some features may be limited until employee availability is configured.");
            alert.showAndWait();
        }
    }

    private void generateAllRecommendations() {
        try {
            schedulingController.generateRecommendations();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Recommendations generated for all shifts!");
            alert.showAndWait();
            
            refreshSchedule();
        } catch (Exception e) {
            System.err.println("Error generating recommendations: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Recommendations Generated");
            alert.setHeaderText(null);
            alert.setContentText("Recommendations generated, but some features may be limited until employee availability is configured.");
            alert.showAndWait();
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