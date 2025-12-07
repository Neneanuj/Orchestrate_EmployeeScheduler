package com.intramural.scheduling.view;

import com.intramural.scheduling.dao.ShiftDAO;
import com.intramural.scheduling.model.Schedule;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class StaffDashboard {
    private Stage primaryStage;
    private String username;
    private int employeeId;
    private ShiftDAO shiftDAO;
    private List<Schedule.Game> assignedShifts;
    private VBox mainContent;
    
    public StaffDashboard(Stage primaryStage, String username, int employeeId) {
        this.primaryStage = primaryStage;
        this.username = username;
        this.employeeId = employeeId;
        this.shiftDAO = new ShiftDAO();
        this.assignedShifts = new ArrayList<>();
        loadShifts();
    }
    
    private void loadShifts() {
        try {
            assignedShifts = shiftDAO.getShiftsByEmployee(employeeId);
        } catch (Exception e) {
            e.printStackTrace();
            assignedShifts = new ArrayList<>();
        }
    }
    
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4f8;");
        
        // Top Navigation Bar
        root.setTop(createTopBar());
        
        // Main Content Area
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f0f4f8; -fx-background-color: #f0f4f8;");
        
        mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30));
        
        // Welcome Section
        mainContent.getChildren().add(createWelcomeSection());
        
        // Weekly Summary Cards
        mainContent.getChildren().add(createWeeklySummary());
        
        // This Week's Schedule
        mainContent.getChildren().add(createThisWeekSchedule());
        
        // Upcoming Shifts
        mainContent.getChildren().add(createUpcomingShifts());
        
        scrollPane.setContent(mainContent);
        root.setCenter(scrollPane);
        
        Scene scene = new Scene(root, 1200, 800);
        
        // Make responsive
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            scrollPane.setPrefWidth(newVal.doubleValue());
        });
        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            scrollPane.setPrefHeight(newVal.doubleValue() - 80);
        });
        
        return scene;
    }
    
    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setStyle("-fx-background-color: #2c3e50;");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(20);
        
        Label titleLabel = new Label("🏃 Staff Dashboard");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.WHITE);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label userLabel = new Label("👤 " + username);
        userLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        userLabel.setTextFill(Color.web("#ecf0f1"));
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        refreshBtn.setOnAction(e -> refreshDashboard());
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                          "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        logoutBtn.setOnAction(e -> logout());
        
        topBar.getChildren().addAll(titleLabel, spacer, userLabel, refreshBtn, logoutBtn);
        return topBar;
    }
    
    private VBox createWelcomeSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); " +
                        "-fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);");
        
        Label welcome = new Label("Welcome back, " + username + "!");
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        welcome.setTextFill(Color.WHITE);
        
        Label subtitle = new Label("Here's your schedule overview");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.web("#ecf0f1"));
        
        section.getChildren().addAll(welcome, subtitle);
        return section;
    }
    
    private HBox createWeeklySummary() {
        HBox summary = new HBox(20);
        summary.setAlignment(Pos.CENTER);
        
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        List<Schedule.Game> thisWeekShifts = assignedShifts.stream()
            .filter(shift -> !shift.getGameDate().isBefore(weekStart) && 
                           !shift.getGameDate().isAfter(weekEnd))
            .collect(Collectors.toList());
        
        double totalHours = thisWeekShifts.stream()
            .mapToDouble(Schedule.Game::getDurationHours)
            .sum();
        
        long daysWorking = thisWeekShifts.stream()
            .map(Schedule.Game::getGameDate)
            .distinct()
            .count();
        
        // Total Shifts Card
        summary.getChildren().add(createSummaryCard(
            "📅", 
            String.valueOf(thisWeekShifts.size()), 
            "Shifts This Week", 
            "#3498db"
        ));
        
        // Total Hours Card
        summary.getChildren().add(createSummaryCard(
            "⏰", 
            String.format("%.1f hrs", totalHours), 
            "Total Hours", 
            "#27ae60"
        ));
        
        // Days Working Card
        summary.getChildren().add(createSummaryCard(
            "📆", 
            String.valueOf(daysWorking), 
            "Days Working", 
            "#f39c12"
        ));
        
        // Upcoming Shifts Card
        long upcoming = assignedShifts.stream()
            .filter(shift -> shift.getGameDate().isAfter(today))
            .count();
        
        summary.getChildren().add(createSummaryCard(
            "🔜", 
            String.valueOf(upcoming), 
            "Upcoming Shifts", 
            "#9b59b6"
        ));
        
        return summary;
    }
    
    private VBox createSummaryCard(String icon, String value, String label, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(25));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        card.setPrefWidth(250);
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(40));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        valueLabel.setTextFill(Color.web(color));
        
        Label descLabel = new Label(label);
        descLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        descLabel.setTextFill(Color.web("#7f8c8d"));
        
        card.getChildren().addAll(iconLabel, valueLabel, descLabel);
        return card;
    }
    
    private VBox createThisWeekSchedule() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        Label title = new Label("📅 This Week's Schedule");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2c3e50"));
        
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        Label weekLabel = new Label(weekStart.format(DateTimeFormatter.ofPattern("MMM dd")) + 
                                   " - " + weekEnd.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        weekLabel.setFont(Font.font("Arial", 14));
        weekLabel.setTextFill(Color.web("#7f8c8d"));
        
        Separator separator = new Separator();
        
        VBox scheduleList = new VBox(10);
        
        Map<DayOfWeek, List<Schedule.Game>> weeklySchedule = assignedShifts.stream()
            .filter(shift -> !shift.getGameDate().isBefore(weekStart) && 
                           !shift.getGameDate().isAfter(weekEnd))
            .collect(Collectors.groupingBy(shift -> shift.getGameDate().getDayOfWeek()));
        
        for (DayOfWeek day : DayOfWeek.values()) {
            LocalDate date = weekStart.plusDays(day.getValue() - 1);
            List<Schedule.Game> dayShifts = weeklySchedule.getOrDefault(day, new ArrayList<>());
            
            scheduleList.getChildren().add(createDayScheduleRow(day, date, dayShifts));
        }
        
        section.getChildren().addAll(title, weekLabel, separator, scheduleList);
        return section;
    }
    
    private HBox createDayScheduleRow(DayOfWeek day, LocalDate date, List<Schedule.Game> shifts) {
        HBox row = new HBox(20);
        row.setPadding(new Insets(15));
        row.setAlignment(Pos.CENTER_LEFT);
        
        boolean isToday = date.equals(LocalDate.now());
        
        if (isToday) {
            row.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 8; " +
                        "-fx-border-color: #27ae60; -fx-border-width: 2; -fx-border-radius: 8;");
        } else if (!shifts.isEmpty()) {
            row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");
        } else {
            row.setStyle("-fx-background-color: transparent;");
        }
        
        VBox dayInfo = new VBox(5);
        dayInfo.setPrefWidth(150);
        
        Label dayLabel = new Label(day.toString());
        dayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        dayLabel.setTextFill(isToday ? Color.web("#27ae60") : Color.web("#2c3e50"));
        
        Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dateLabel.setFont(Font.font("Arial", 12));
        dateLabel.setTextFill(Color.web("#7f8c8d"));
        
        dayInfo.getChildren().addAll(dayLabel, dateLabel);
        
        VBox shiftsInfo = new VBox(8);
        HBox.setHgrow(shiftsInfo, Priority.ALWAYS);
        
        if (shifts.isEmpty()) {
            Label noShift = new Label("No shifts scheduled");
            noShift.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
            noShift.setTextFill(Color.web("#95a5a6"));
            shiftsInfo.getChildren().add(noShift);
        } else {
            for (Schedule.Game shift : shifts) {
                shiftsInfo.getChildren().add(createShiftInfoBox(shift));
            }
        }
        
        row.getChildren().addAll(dayInfo, shiftsInfo);
        return row;
    }
    
    private HBox createShiftInfoBox(Schedule.Game shift) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 6; " +
                    "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 6;");
        
        Label timeLabel = new Label("🕐 " + shift.getStartTime().format(DateTimeFormatter.ofPattern("h:mm a")) +
                                   " - " + shift.getEndTime().format(DateTimeFormatter.ofPattern("h:mm a")));
        timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        timeLabel.setTextFill(Color.web("#2c3e50"));
        timeLabel.setPrefWidth(180);
        
        Label locationLabel = new Label("📍 " + shift.getLocation());
        locationLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        locationLabel.setTextFill(Color.web("#3498db"));
        locationLabel.setPrefWidth(200);
        
        Label durationLabel = new Label(String.format("%.1f hrs", shift.getDurationHours()));
        durationLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        durationLabel.setTextFill(Color.WHITE);
        durationLabel.setPadding(new Insets(5, 10, 5, 10));
        durationLabel.setStyle("-fx-background-color: #27ae60; -fx-background-radius: 12;");
        
        box.getChildren().addAll(timeLabel, locationLabel, durationLabel);
        return box;
    }
    
    private VBox createUpcomingShifts() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        Label title = new Label("🔜 Upcoming Shifts (Next 30 Days)");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2c3e50"));
        
        Separator separator = new Separator();
        
        LocalDate today = LocalDate.now();
        LocalDate futureLimit = today.plusDays(30);
        
        List<Schedule.Game> upcomingShifts = assignedShifts.stream()
            .filter(shift -> shift.getGameDate().isAfter(today) && 
                           !shift.getGameDate().isAfter(futureLimit))
            .sorted(Comparator.comparing(Schedule.Game::getGameDate)
                             .thenComparing(Schedule.Game::getStartTime))
            .collect(Collectors.toList());
        
        VBox shiftsList = new VBox(12);
        
        if (upcomingShifts.isEmpty()) {
            Label noShifts = new Label("No upcoming shifts in the next 30 days");
            noShifts.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
            noShifts.setTextFill(Color.web("#95a5a6"));
            noShifts.setPadding(new Insets(20));
            shiftsList.getChildren().add(noShifts);
        } else {
            for (Schedule.Game shift : upcomingShifts) {
                shiftsList.getChildren().add(createUpcomingShiftCard(shift));
            }
        }
        
        section.getChildren().addAll(title, separator, shiftsList);
        return section;
    }
    
    private HBox createUpcomingShiftCard(Schedule.Game shift) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                     "-fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 10;");
        
        long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), shift.getGameDate());
        
        VBox dateBox = new VBox(5);
        dateBox.setPrefWidth(120);
        dateBox.setAlignment(Pos.CENTER);
        dateBox.setPadding(new Insets(10));
        dateBox.setStyle("-fx-background-color: #3498db; -fx-background-radius: 8;");
        
        Label monthLabel = new Label(shift.getGameDate().format(DateTimeFormatter.ofPattern("MMM")));
        monthLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        monthLabel.setTextFill(Color.WHITE);
        
        Label dayLabel = new Label(shift.getGameDate().format(DateTimeFormatter.ofPattern("dd")));
        dayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        dayLabel.setTextFill(Color.WHITE);
        
        Label dayOfWeekLabel = new Label(shift.getGameDate().getDayOfWeek().toString());
        dayOfWeekLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
        dayOfWeekLabel.setTextFill(Color.web("#ecf0f1"));
        
        dateBox.getChildren().addAll(monthLabel, dayLabel, dayOfWeekLabel);
        
        VBox detailsBox = new VBox(8);
        HBox.setHgrow(detailsBox, Priority.ALWAYS);
        
        Label timeLabel = new Label("🕐 " + shift.getStartTime().format(DateTimeFormatter.ofPattern("h:mm a")) +
                                   " - " + shift.getEndTime().format(DateTimeFormatter.ofPattern("h:mm a")));
        timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        timeLabel.setTextFill(Color.web("#2c3e50"));
        
        Label locationLabel = new Label("📍 " + shift.getLocation());
        locationLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        locationLabel.setTextFill(Color.web("#3498db"));
        
        Label daysLabel = new Label("In " + daysUntil + " day" + (daysUntil != 1 ? "s" : ""));
        daysLabel.setFont(Font.font("Arial", 12));
        daysLabel.setTextFill(Color.web("#7f8c8d"));
        
        detailsBox.getChildren().addAll(timeLabel, locationLabel, daysLabel);
        
        VBox statsBox = new VBox(10);
        statsBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label hoursLabel = new Label(String.format("%.1f hrs", shift.getDurationHours()));
        hoursLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        hoursLabel.setTextFill(Color.web("#27ae60"));
        
        statsBox.getChildren().add(hoursLabel);
        
        card.getChildren().addAll(dateBox, detailsBox, statsBox);
        return card;
    }
    
    private void refreshDashboard() {
        loadShifts();
        mainContent.getChildren().clear();
        mainContent.getChildren().add(createWelcomeSection());
        mainContent.getChildren().add(createWeeklySummary());
        mainContent.getChildren().add(createThisWeekSchedule());
        mainContent.getChildren().add(createUpcomingShifts());
    }
    
    private void logout() {
        LoginView loginView = new LoginView(primaryStage);
        primaryStage.setScene(loginView.createScene());
        primaryStage.setTitle("Employee Scheduling System - Login");
    }
}
