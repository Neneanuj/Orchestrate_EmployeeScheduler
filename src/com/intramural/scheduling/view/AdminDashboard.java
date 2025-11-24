package com.intramural.scheduling.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class AdminDashboard {
    private Stage primaryStage;
    private String username;

    public AdminDashboard(Stage primaryStage, String username) {
        this.primaryStage = primaryStage;
        this.username = username;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Top Navigation Bar
        root.setTop(createTopBar());

        // Main Content
        root.setCenter(createMainContent());

        Scene scene = new Scene(root, 1400, 800);
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
        
        Button dashboardBtn = createNavButton("🏠 Dashboard", true);
        Button scheduleBtn = createNavButton("📅 Schedule", false);
        Button employeesBtn = createNavButton("👥 Employees", false);
        Button analyticsBtn = createNavButton("📊 Analytics", false);
        
        navButtons.getChildren().addAll(dashboardBtn, scheduleBtn, employeesBtn, analyticsBtn);

        // Right side - Notifications and Settings
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

        // Settings Button
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
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; " +
                    "-fx-cursor: hand;");
            btn.setOnMouseEntered(e -> btn.setStyle(
                    "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; " +
                    "-fx-background-radius: 5; -fx-cursor: hand;"));
            btn.setOnMouseExited(e -> btn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-cursor: hand;"));
        }
        return btn;
    }

    private ScrollPane createMainContent() {
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30, 40, 30, 40));

        // Welcome Header
        VBox welcomeBox = new VBox(5);
        Label welcomeLabel = new Label("Welcome back, " + username);
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        welcomeLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Label subtitleLabel = new Label("Here's what's happening with your team today.");
        subtitleLabel.setFont(Font.font("Arial", 16));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        welcomeBox.getChildren().addAll(welcomeLabel, subtitleLabel);

        // Statistics Cards
        HBox statsCards = createStatsCards();

        // Bottom Section - Shifts and Team Members
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

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        // Card 1: Total Employees
        VBox card1 = createStatCard("👥", "#dbeafe", "Total Employees", "248", "↑ +12% from last month", "#10b981");
        
        // Card 2: Active Shifts
        VBox card2 = createStatCard("📅", "#fef3c7", "Active Shifts", "64", "↑ +8% from last week", "#10b981");
        
        // Card 3: Hours Scheduled
        VBox card3 = createStatCard("🕐", "#d1fae5", "Hours Scheduled", "1,856", "", "");
        
        // Card 4: Fill Rate
        VBox card4 = createStatCard("📈", "#fce7f3", "Fill Rate", "94%", "↑ +2% from last month", "#10b981");

        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        HBox.setHgrow(card4, Priority.ALWAYS);

        statsBox.getChildren().addAll(card1, card2, card3, card4);
        return statsBox;
    }

    private VBox createStatCard(String icon, String iconBg, String title, String value, String change, String changeColor) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setMaxWidth(Double.MAX_VALUE);

        // Icon
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(32));
        iconLabel.setStyle("-fx-background-color: " + iconBg + "; -fx-background-radius: 10; " +
                "-fx-padding: 10;");

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 13));
        titleLabel.setStyle("-fx-text-fill: #6b7280;");

        // Value
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        valueLabel.setStyle("-fx-text-fill: #1f2937;");

        // Change indicator
        if (!change.isEmpty()) {
            Label changeLabel = new Label(change);
            changeLabel.setFont(Font.font("Arial", 12));
            changeLabel.setStyle("-fx-text-fill: " + changeColor + ";");
            card.getChildren().addAll(iconLabel, titleLabel, valueLabel, changeLabel);
        } else {
            card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        }

        return card;
    }

    private VBox createUpcomingShiftsSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Upcoming Shifts");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #1f2937;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button createBtn = new Button("+ Create Shift");
        createBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 8 16 8 16; -fx-cursor: hand; -fx-font-weight: bold;");

        header.getChildren().addAll(titleLabel, spacer, createBtn);

        // Shift Cards
        VBox shiftsList = new VBox(15);
        shiftsList.getChildren().addAll(
                createShiftCard("Basketball Game", "Arena A", "Dec 15, 2024 • 6:00 PM - 9:00 PM", 
                        "8 / 8 staff", "Fully Staffed", "#10b981"),
                createShiftCard("Soccer Match", "Field 2", "Dec 16, 2024 • 2:00 PM - 5:00 PM", 
                        "4 / 6 staff", "Needs Staff", "#e74c3c"),
                createShiftCard("Hockey Practice", "Rink 1", "Dec 17, 2024 • 8:00 AM - 11:00 AM", 
                        "4 / 4 staff", "Fully Staffed", "#10b981")
        );

        // View All Link
        Hyperlink viewAllLink = new Hyperlink("View All Shifts →");
        viewAllLink.setStyle("-fx-text-fill: #3498db; -fx-font-size: 14px; -fx-font-weight: bold;");
        viewAllLink.setAlignment(Pos.CENTER);

        section.getChildren().addAll(header, shiftsList, viewAllLink);
        return section;
    }

    private HBox createShiftCard(String name, String location, String time, String staffing, String status, String statusColor) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;");

        VBox infoBox = new VBox(8);
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setStyle("-fx-text-fill: #1f2937;");

        Label locationLabel = new Label("📍 " + location);
        locationLabel.setFont(Font.font("Arial", 13));
        locationLabel.setStyle("-fx-text-fill: #6b7280;");

        Label timeLabel = new Label("🕐 " + time);
        timeLabel.setFont(Font.font("Arial", 13));
        timeLabel.setStyle("-fx-text-fill: #6b7280;");

        Label staffLabel = new Label("👥 " + staffing);
        staffLabel.setFont(Font.font("Arial", 13));
        staffLabel.setStyle("-fx-text-fill: #6b7280;");

        infoBox.getChildren().addAll(nameLabel, locationLabel, timeLabel, staffLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label(status);
        statusBadge.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statusBadge.setPadding(new Insets(6, 12, 6, 12));
        statusBadge.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; " +
                "-fx-background-radius: 15;");

        Label upcomingBadge = new Label("upcoming");
        upcomingBadge.setFont(Font.font("Arial", 11));
        upcomingBadge.setPadding(new Insets(4, 10, 4, 10));
        upcomingBadge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #3498db; " +
                "-fx-background-radius: 12;");

        VBox badgeBox = new VBox(5);
        badgeBox.setAlignment(Pos.CENTER_RIGHT);
        badgeBox.getChildren().addAll(upcomingBadge, statusBadge);

        card.getChildren().addAll(infoBox, spacer, badgeBox);
        return card;
    }

    private VBox createTeamMembersSection() {
        VBox section = new VBox(20);
        section.setPrefWidth(350);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Team Members");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #1f2937;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Hyperlink viewAllLink = new Hyperlink("View All");
        viewAllLink.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");

        header.getChildren().addAll(titleLabel, spacer, viewAllLink);

        // Team Member Cards
        VBox membersList = new VBox(15);
        membersList.getChildren().addAll(
                createTeamMemberCard("MC", "Michael Chen", "Event Coordinator", "🏀 Football, Baseball", 
                        "+1 (555) 234-5678", "m.chen@example.com"),
                createTeamMemberCard("ER", "Emily Rodriguez", "Team Lead", "🏒 Hockey, Volleyball", 
                        "+1 (555) 345-6789", "emily.r@example.com"),
                createTeamMemberCard("SJ", "Sarah Johnson", "Senior Referee", "🏀 Basketball, Soccer", 
                        "+1 (555) 123-4567", "sarah.j@example.com")
        );

        section.getChildren().addAll(header, membersList);
        return section;
    }

    private VBox createTeamMemberCard(String initials, String name, String role, String sports, 
            String phone, String email) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;");

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Avatar with initials
        Label avatar = new Label(initials);
        avatar.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        avatar.setStyle("-fx-background-color: #bfdbfe; -fx-text-fill: #1e40af; " +
                "-fx-background-radius: 25; -fx-min-width: 50; -fx-min-height: 50; " +
                "-fx-alignment: center;");

        VBox nameBox = new VBox(3);
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        nameLabel.setStyle("-fx-text-fill: #1f2937;");

        Label roleLabel = new Label(role);
        roleLabel.setFont(Font.font("Arial", 12));
        roleLabel.setStyle("-fx-text-fill: #6b7280;");

        nameBox.getChildren().addAll(nameLabel, roleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label activeBadge = new Label("active");
        activeBadge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        activeBadge.setPadding(new Insets(4, 10, 4, 10));
        activeBadge.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 12;");

        topRow.getChildren().addAll(avatar, nameBox, spacer, activeBadge);

        // Sports
        Label sportsLabel = new Label(sports);
        sportsLabel.setFont(Font.font("Arial", 12));
        sportsLabel.setStyle("-fx-text-fill: #6b7280;");

        // Contact info
        Label phoneLabel = new Label("📞 " + phone);
        phoneLabel.setFont(Font.font("Arial", 12));
        phoneLabel.setStyle("-fx-text-fill: #6b7280;");

        Label emailLabel = new Label("✉️ " + email);
        emailLabel.setFont(Font.font("Arial", 12));
        emailLabel.setStyle("-fx-text-fill: #6b7280;");

        card.getChildren().addAll(topRow, sportsLabel, phoneLabel, emailLabel);
        return card;
    }
}