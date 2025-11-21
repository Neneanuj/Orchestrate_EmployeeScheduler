package com.intramural.scheduling.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class ScheduleBuilder {
    private Stage primaryStage;
    private String username;
    private LocalDate currentMonth;

    public ScheduleBuilder(Stage primaryStage, String username) {
        this.primaryStage = primaryStage;
        this.username = username;
        this.currentMonth = LocalDate.now();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Top Navigation Bar
        root.setTop(createTopBar());

        // Main Content
        root.setCenter(createMainContent());

        Scene scene = new Scene(root, 1400, 900);
        return scene;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 0 0 1 0;");

        // Logo and Title
        Label logo = new Label("📅");
        logo.setFont(Font.font(28));
        
        VBox titleBox = new VBox(2);
        Label title = new Label("ShiftFlow");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: #2c3e50;");
        
        Label subtitle = new Label("Employment Shift Scheduling");
        subtitle.setFont(Font.font("Arial", 11));
        subtitle.setStyle("-fx-text-fill: #7f8c8d;");
        
        titleBox.getChildren().addAll(title, subtitle);

        // Navigation Buttons
        HBox navButtons = new HBox(15);
        navButtons.setAlignment(Pos.CENTER_LEFT);
        
        Button dashboardBtn = createNavButton("🏠 Dashboard", false);
        Button scheduleBtn = createNavButton("📅 Schedule", true);
        Button employeesBtn = createNavButton("👥 Employees", false);
        Button analyticsBtn = createNavButton("📊 Analytics", false);
        
        navButtons.getChildren().addAll(dashboardBtn, scheduleBtn, employeesBtn, analyticsBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Notification Bell
        Button notificationBtn = new Button("🔔");
        notificationBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand;");
        Label notificationBadge = new Label("3");
        notificationBadge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-padding: 2 6 2 6; -fx-font-size: 10px; -fx-font-weight: bold;");
        StackPane notificationStack = new StackPane(notificationBtn);
        StackPane.setAlignment(notificationBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(notificationBadge, new Insets(5, 5, 0, 0));
        notificationStack.getChildren().add(notificationBadge);

        Button settingsBtn = new Button("⚙️");
        settingsBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand;");

        topBar.getChildren().addAll(logo, titleBox, navButtons, spacer, notificationStack, settingsBtn);
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
            btn.setOnMouseEntered(e -> btn.setStyle(
                    "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-background-radius: 5; -fx-cursor: hand;"));
            btn.setOnMouseExited(e -> btn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-cursor: hand;"));
        }
        return btn;
    }

    private ScrollPane createMainContent() {
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30, 40, 30, 40));

        // Header Section
        HBox headerSection = createHeaderSection();
        
        // Calendar Section
        VBox calendarSection = createCalendarSection();
        
        // Stats Cards
        HBox statsCards = createStatsCards();
        
        // Shifts List Section
        VBox shiftsListSection = createShiftsListSection();

        mainContent.getChildren().addAll(headerSection, calendarSection, statsCards, shiftsListSection);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");
        return scrollPane;
    }

    private HBox createHeaderSection() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));

        VBox titleBox = new VBox(5);
        Label titleLabel = new Label("Schedule Management");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Label subtitleLabel = new Label("Manage game schedules and assign team members");
        subtitleLabel.setFont(Font.font("Arial", 16));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button createShiftBtn = new Button("+ Create New Shift");
        createShiftBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 12 24 12 24; -fx-cursor: hand; " +
                "-fx-font-size: 14px; -fx-font-weight: bold;");
        createShiftBtn.setOnMouseEntered(e -> createShiftBtn.setStyle(
                "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 6; " +
                "-fx-padding: 12 24 12 24; -fx-cursor: hand; -fx-font-size: 14px; -fx-font-weight: bold;"));
        createShiftBtn.setOnMouseExited(e -> createShiftBtn.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 6; " +
                "-fx-padding: 12 24 12 24; -fx-cursor: hand; -fx-font-size: 14px; -fx-font-weight: bold;"));

        header.getChildren().addAll(titleBox, spacer, createShiftBtn);
        return header;
    }

    private VBox createCalendarSection() {
        VBox calendarBox = new VBox(20);
        calendarBox.setPadding(new Insets(25));
        calendarBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        // Calendar Header
        HBox calendarHeader = new HBox();
        calendarHeader.setAlignment(Pos.CENTER);
        calendarHeader.setSpacing(20);

        Button prevMonthBtn = new Button("<");
        prevMonthBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: #3498db;");

        Label monthYearLabel = new Label(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + currentMonth.getYear());
        monthYearLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        monthYearLabel.setStyle("-fx-text-fill: #2c3e50;");

        Button nextMonthBtn = new Button(">");
        nextMonthBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: #3498db;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button todayBtn = new Button("📅 Today");
        todayBtn.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #2c3e50; " +
                "-fx-background-radius: 6; -fx-padding: 8 16 8 16; -fx-cursor: hand; -fx-font-weight: bold;");

        calendarHeader.getChildren().addAll(prevMonthBtn, monthYearLabel, nextMonthBtn, spacer, todayBtn);

        // Calendar Grid (Week View)
        HBox weekView = createWeekView();

        calendarBox.getChildren().addAll(calendarHeader, weekView);
        return calendarBox;
    }

    private HBox createWeekView() {
        HBox weekView = new HBox(15);
        weekView.setAlignment(Pos.CENTER);
        
        LocalDate startOfWeek = currentMonth.minusDays(currentMonth.getDayOfWeek().getValue() - 1);
        
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        int[] shiftsPerDay = {2, 1, 3, 2, 4, 3, 1};
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            VBox dayCard = createDayCard(days[i], date.getDayOfMonth(), shiftsPerDay[i], date.equals(LocalDate.now()));
            HBox.setHgrow(dayCard, Priority.ALWAYS);
            weekView.getChildren().add(dayCard);
        }
        
        return weekView;
    }

    private VBox createDayCard(String dayName, int dayNumber, int shiftCount, boolean isToday) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20, 15, 20, 15));
        card.setMaxWidth(Double.MAX_VALUE);
        
        if (isToday) {
            card.setStyle("-fx-background-color: #dbeafe; -fx-background-radius: 8; " +
                    "-fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 8;");
        } else {
            card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;");
        }

        Label dayLabel = new Label(dayName);
        dayLabel.setFont(Font.font("Arial", 13));
        dayLabel.setStyle("-fx-text-fill: #6b7280;");

        Label dayNumLabel = new Label(String.valueOf(dayNumber));
        dayNumLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        dayNumLabel.setStyle("-fx-text-fill: #1f2937;");

        Label shiftsLabel = new Label(shiftCount + " shift" + (shiftCount != 1 ? "s" : ""));
        shiftsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        shiftsLabel.setStyle("-fx-text-fill: #3498db;");

        card.getChildren().addAll(dayLabel, dayNumLabel, shiftsLabel);
        return card;
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        VBox card1 = createSmallStatCard("📅", "#dbeafe", "Total Shifts", "7");
        VBox card2 = createSmallStatCard("🕐", "#d1fae5", "Upcoming", "5");
        VBox card3 = createSmallStatCard("👥", "#fef3c7", "Needs Staff", "2");
        VBox card4 = createSmallStatCard("📍", "#e0e7ff", "Locations", "8");

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
        iconLabel.setStyle("-fx-background-color: " + iconBg + "; -fx-background-radius: 8; -fx-padding: 8;");

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        valueLabel.setStyle("-fx-text-fill: #1f2937;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 13));
        titleLabel.setStyle("-fx-text-fill: #6b7280;");

        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        return card;
    }

    private VBox createShiftsListSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        // Search and Filter Bar
        HBox searchBar = createSearchBar();

        // Tabs
        HBox tabs = createTabsBar();

        // Shift Cards
        VBox shiftsList = new VBox(15);
        shiftsList.getChildren().addAll(
                createShiftCard("Basketball Championship", "Arena A - Main Court", "Dec 15, 2024 • 6:00 PM - 9:00 PM", 
                        "8 / 8 staff", "Fully Staffed", "#10b981", "upcoming"),
                createShiftCard("Soccer League Match", "Field 2 - North Stadium", "Dec 15, 2024 • 3:00 PM - 6:00 PM", 
                        "5 / 6 staff", "Needs Staff", "#e74c3c", "upcoming"),
                createShiftCard("Hockey Practice Session", "Rink 1 - Ice Arena", "Dec 16, 2024 • 8:00 AM - 11:00 AM", 
                        "4 / 4 staff", "Fully Staffed", "#10b981", "upcoming"),
                createShiftCard("Volleyball Tournament", "Court 3 - Sports Complex", "Dec 16, 2024 • 2:00 PM - 7:00 PM", 
                        "7 / 10 staff", "Needs Staff", "#e74c3c", "upcoming"),
                createShiftCard("Baseball Game", "Diamond 1 - West Field", "Dec 17, 2024 • 1:00 PM - 4:00 PM", 
                        "7 / 7 staff", "Fully Staffed", "#10b981", "upcoming"),
                createShiftCard("Football Practice", "Field 1 - Training Ground", "Dec 14, 2024 • 4:00 PM - 6:00 PM", 
                        "5 / 5 staff", "Fully Staffed", "#10b981", "completed"),
                createShiftCard("Tennis Exhibition", "Court 5 - Tennis Center", "Dec 14, 2024 • 10:00 AM - 2:00 PM", 
                        "4 / 4 staff", "Fully Staffed", "#10b981", "completed")
        );

        section.getChildren().addAll(searchBar, tabs, shiftsList);
        return section;
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox(15);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by sport, location, or date...");
        searchField.setPrefHeight(40);
        searchField.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-font-size: 14px; -fx-padding: 0 15 0 15;");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button filterBtn = new Button("🔽 Filters");
        filterBtn.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #2c3e50; " +
                "-fx-background-radius: 8; -fx-padding: 10 20 10 20; -fx-cursor: hand; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-font-weight: bold;");

        searchBar.getChildren().addAll(searchField, filterBtn);
        return searchBar;
    }

    private HBox createTabsBar() {
        HBox tabs = new HBox(20);
        tabs.setAlignment(Pos.CENTER_LEFT);
        tabs.setPadding(new Insets(10, 0, 10, 0));

        Button allTab = createTabButton("All Shifts", true);
        Button upcomingTab = createTabButton("Upcoming", false);
        Button completedTab = createTabButton("Completed", false);

        tabs.getChildren().addAll(allTab, upcomingTab, completedTab);
        return tabs;
    }

    private Button createTabButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setPadding(new Insets(8, 0, 8, 0));
        
        if (active) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2c3e50; " +
                    "-fx-border-color: transparent transparent #3498db transparent; " +
                    "-fx-border-width: 0 0 3 0; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; " +
                    "-fx-cursor: hand;");
        }
        return btn;
    }

    private HBox createShiftCard(String title, String location, String time, String staffing, 
                                  String status, String statusColor, String badge) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;");

        VBox infoBox = new VBox(8);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #1f2937;");

        Label locationLabel = new Label("📍 " + location);
        locationLabel.setFont(Font.font("Arial", 13));
        locationLabel.setStyle("-fx-text-fill: #6b7280;");

        Label timeLabel = new Label("🕐 " + time);
        timeLabel.setFont(Font.font("Arial", 13));
        timeLabel.setStyle("-fx-text-fill: #6b7280;");

        Label staffLabel = new Label("👥 " + staffing);
        staffLabel.setFont(Font.font("Arial", 13));
        staffLabel.setStyle("-fx-text-fill: #6b7280;");

        infoBox.getChildren().addAll(titleLabel, locationLabel, timeLabel, staffLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox badgeBox = new VBox(8);
        badgeBox.setAlignment(Pos.CENTER_RIGHT);

        Label statusBadge = new Label(badge);
        statusBadge.setFont(Font.font("Arial", 11));
        statusBadge.setPadding(new Insets(4, 10, 4, 10));
        String badgeColor = badge.equals("upcoming") ? "#dbeafe" : "#d1fae5";
        String badgeTextColor = badge.equals("upcoming") ? "#3498db" : "#10b981";
        statusBadge.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: " + badgeTextColor + "; " +
                "-fx-background-radius: 12;");

        Label staffStatusBadge = new Label(status);
        staffStatusBadge.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        staffStatusBadge.setPadding(new Insets(6, 12, 6, 12));
        staffStatusBadge.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; " +
                "-fx-background-radius: 15;");

        badgeBox.getChildren().addAll(statusBadge, staffStatusBadge);

        card.getChildren().addAll(infoBox, spacer, badgeBox);
        return card;
    }
}