package com.intramural.scheduling.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class EmployeesPage {
    private Stage primaryStage;
    private String username;

    public EmployeesPage(Stage primaryStage, String username) {
        this.primaryStage = primaryStage;
        this.username = username;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        root.setTop(createTopBar());
        root.setCenter(createMainContent());

        Scene scene = new Scene(root, 1400, 900);
        return scene;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 0 0 1 0;");

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

        HBox navButtons = new HBox(15);
        navButtons.setAlignment(Pos.CENTER_LEFT);
        
        Button dashboardBtn = createNavButton("🏠 Dashboard", false);
        Button scheduleBtn = createNavButton("📅 Schedule", false);
        Button employeesBtn = createNavButton("👥 Employees", true);
        Button analyticsBtn = createNavButton("📊 Analytics", false);
        
        navButtons.getChildren().addAll(dashboardBtn, scheduleBtn, employeesBtn, analyticsBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

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

        HBox headerSection = createHeaderSection();
        HBox statsCards = createStatsCards();
        VBox expertiseSection = createExpertiseSection();
        HBox searchSection = createSearchSection();
        HBox tabsSection = createTabsSection();
        GridPane employeeGrid = createEmployeeGrid();

        mainContent.getChildren().addAll(headerSection, statsCards, expertiseSection, 
                searchSection, tabsSection, employeeGrid);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");
        return scrollPane;
    }

    private HBox createHeaderSection() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(5);
        Label titleLabel = new Label("Employee Directory");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Label subtitleLabel = new Label("Manage your team members and their assignments");
        subtitleLabel.setFont(Font.font("Arial", 16));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("⬇️ Export");
        exportBtn.setStyle("-fx-background-color: white; -fx-text-fill: #2c3e50; " +
                "-fx-background-radius: 6; -fx-padding: 12 24 12 24; -fx-cursor: hand; " +
                "-fx-font-weight: bold; -fx-border-color: #e5e7eb; -fx-border-radius: 6;");

        Button addBtn = new Button("+ Add Employee");
        addBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 12 24 12 24; -fx-cursor: hand; " +
                "-fx-font-size: 14px; -fx-font-weight: bold;");
        addBtn.setOnAction(e -> {
            AddEmployeeModal modal = new AddEmployeeModal(primaryStage);
            modal.show();
        });

        HBox buttonBox = new HBox(15);
        buttonBox.getChildren().addAll(exportBtn, addBtn);

        header.getChildren().addAll(titleBox, spacer, buttonBox);
        return header;
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        VBox card1 = createStatCard("👥", "#dbeafe", "Total Team", "12");
        VBox card2 = createStatCard("✅", "#d1fae5", "Active", "11");
        VBox card3 = createStatCard("⊗", "#fee2e2", "Inactive", "1");
        VBox card4 = createStatCard("🏅", "#fef3c7", "Certifications", "156");

        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        HBox.setHgrow(card4, Priority.ALWAYS);

        statsBox.getChildren().addAll(card1, card2, card3, card4);
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
        iconLabel.setStyle("-fx-background-color: " + iconBg + "; -fx-background-radius: 10; -fx-padding: 10;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 13));
        titleLabel.setStyle("-fx-text-fill: #6b7280;");

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        valueLabel.setStyle("-fx-text-fill: #1f2937;");

        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        return card;
    }

    private VBox createExpertiseSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label titleLabel = new Label("Expertise Distribution");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #1f2937;");

        FlowPane expertiseFlow = new FlowPane();
        expertiseFlow.setHgap(10);
        expertiseFlow.setVgap(10);

        String[][] expertise = {
            {"Basketball", "4"}, {"Soccer", "5"}, {"Football", "3"},
            {"Hockey", "3"}, {"Baseball", "2"}, {"Volleyball", "2"}
        };

        for (String[] exp : expertise) {
            Label badge = new Label(exp[0] + " (" + exp[1] + ")");
            badge.setFont(Font.font("Arial", 13));
            badge.setPadding(new Insets(8, 16, 8, 16));
            badge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; " +
                    "-fx-background-radius: 20;");
            expertiseFlow.getChildren().add(badge);
        }

        section.getChildren().addAll(titleLabel, expertiseFlow);
        return section;
    }

    private HBox createSearchSection() {
        HBox searchBox = new HBox(15);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by name, role, or expertise...");
        searchField.setPrefHeight(40);
        searchField.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-font-size: 14px; " +
                "-fx-padding: 0 15 0 15;");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Status", "Active", "Inactive");
        statusFilter.setValue("All Status");
        statusFilter.setPrefHeight(40);
        statusFilter.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; " +
                "-fx-border-radius: 8;");

        ComboBox<String> roleFilter = new ComboBox<>();
        roleFilter.getItems().addAll("All Roles", "Referee", "Coordinator", "Official");
        roleFilter.setValue("All Roles");
        roleFilter.setPrefHeight(40);
        roleFilter.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; " +
                "-fx-border-radius: 8;");

        Button moreFilters = new Button("⚙️ More Filters");
        moreFilters.setPrefHeight(40);
        moreFilters.setStyle("-fx-background-color: white; -fx-text-fill: #2c3e50; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-cursor: hand;");

        searchBox.getChildren().addAll(searchField, statusFilter, roleFilter, moreFilters);
        return searchBox;
    }

    private HBox createTabsSection() {
        HBox tabs = new HBox(20);
        tabs.setAlignment(Pos.CENTER_LEFT);
        tabs.setPadding(new Insets(10, 0, 10, 0));

        Button allTab = createTabButton("All Employees", true);
        Button activeTab = createTabButton("Active (11)", false);
        Button inactiveTab = createTabButton("Inactive (1)", false);

        tabs.getChildren().addAll(allTab, activeTab, inactiveTab);
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
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-cursor: hand;");
        }
        return btn;
    }

    private GridPane createEmployeeGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        String[][] employees = {
            {"SJ", "Sarah Johnson", "Senior Referee", "Basketball, Soccer", "+1 (555) 123-4567", "sarah.j@example.com", "active"},
            {"MC", "Michael Chen", "Event Coordinator", "Football, Baseball", "+1 (555) 234-5678", "m.chen@example.com", "active"},
            {"ER", "Emily Rodriguez", "Team Lead", "Hockey, Volleyball", "+1 (555) 345-6789", "emily.r@example.com", "active"},
            {"DP", "David Park", "Sports Official", "Tennis, Badminton", "+1 (555) 456-7890", "david.p@example.com", "active"},
            {"JM", "Jessica Martinez", "Junior Referee", "Basketball, Volleyball", "+1 (555) 567-8901", "jessica.m@example.com", "active"},
            {"JW", "James Wilson", "Timekeeper", "Hockey, Soccer", "+1 (555) 678-9012", "james.w@example.com", "active"},
            {"LA", "Lisa Anderson", "Scorekeeper", "Baseball, Softball", "+1 (555) 789-0123", "lisa.a@example.com", "active"},
            {"RT", "Robert Taylor", "Field Manager", "Football, Soccer", "+1 (555) 890-1234", "robert.t@example.com", "active"},
            {"MG", "Maria Garcia", "Event Staff", "Volleyball, Tennis", "+1 (555) 901-2345", "maria.g@example.com", "inactive"}
        };

        int row = 0, col = 0;
        for (String[] emp : employees) {
            VBox card = createEmployeeCard(emp[0], emp[1], emp[2], emp[3], emp[4], emp[5], emp[6]);
            grid.add(card, col, row);
            
            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }

        return grid;
    }

    private VBox createEmployeeCard(String initials, String name, String role, String sports, 
                                     String phone, String email, String status) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setPrefWidth(400);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(initials);
        avatar.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        avatar.setStyle("-fx-background-color: #bfdbfe; -fx-text-fill: #1e40af; " +
                "-fx-background-radius: 30; -fx-min-width: 60; -fx-min-height: 60; -fx-alignment: center;");

        VBox nameBox = new VBox(3);
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setStyle("-fx-text-fill: #1f2937;");

        Label roleLabel = new Label(role);
        roleLabel.setFont(Font.font("Arial", 13));
        roleLabel.setStyle("-fx-text-fill: #6b7280;");

        nameBox.getChildren().addAll(nameLabel, roleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label(status);
        statusBadge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        statusBadge.setPadding(new Insets(4, 10, 4, 10));
        if (status.equals("active")) {
            statusBadge.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 12;");
        } else {
            statusBadge.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-background-radius: 12;");
        }

        topRow.getChildren().addAll(avatar, nameBox, spacer, statusBadge);

        Label sportsLabel = new Label("🏅 " + sports);
        sportsLabel.setFont(Font.font("Arial", 13));
        sportsLabel.setStyle("-fx-text-fill: #6b7280;");

        HBox contactBox = new HBox(20);
        Label phoneLabel = new Label("📞 " + phone);
        phoneLabel.setFont(Font.font("Arial", 12));
        phoneLabel.setStyle("-fx-text-fill: #6b7280;");

        contactBox.getChildren().add(phoneLabel);

        Label emailLabel = new Label("✉️ " + email);
        emailLabel.setFont(Font.font("Arial", 12));
        emailLabel.setStyle("-fx-text-fill: #6b7280;");

        card.getChildren().addAll(topRow, sportsLabel, contactBox, emailLabel);
        return card;
    }
}