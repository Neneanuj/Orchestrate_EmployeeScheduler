package com.intramural.scheduling.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class AnalyticsPage {
    private Stage primaryStage;
    private String username;

    public AnalyticsPage(Stage primaryStage, String username) {
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
        Button employeesBtn = createNavButton("👥 Employees", false);
        Button analyticsBtn = createNavButton("📊 Analytics", true);
        
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
        HBox tabsSection = createTabsSection();
        HBox chartsRow1 = createChartsRow1();
        HBox chartsRow2 = createChartsRow2();

        mainContent.getChildren().addAll(headerSection, statsCards, tabsSection, chartsRow1, chartsRow2);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");
        return scrollPane;
    }

    private HBox createHeaderSection() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(5);
        Label titleLabel = new Label("Analytics & Reports");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Label subtitleLabel = new Label("Track performance metrics and workforce insights");
        subtitleLabel.setFont(Font.font("Arial", 16));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ComboBox<String> periodFilter = new ComboBox<>();
        periodFilter.getItems().addAll("Last Month", "Last 3 Months", "Last 6 Months", "Last Year");
        periodFilter.setValue("Last Month");
        periodFilter.setPrefHeight(40);
        periodFilter.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; " +
                "-fx-border-radius: 6; -fx-font-size: 14px;");

        Button exportBtn = new Button("⬇️ Export Report");
        exportBtn.setStyle("-fx-background-color: white; -fx-text-fill: #2c3e50; " +
                "-fx-background-radius: 6; -fx-padding: 12 24 12 24; -fx-cursor: hand; " +
                "-fx-font-weight: bold; -fx-border-color: #e5e7eb; -fx-border-radius: 6;");

        HBox controls = new HBox(15);
        controls.getChildren().addAll(periodFilter, exportBtn);

        header.getChildren().addAll(titleBox, spacer, controls);
        return header;
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        VBox card1 = createStatCard("📅", "#dbeafe", "Total Shifts This Year", "745", "+12%", true);
        VBox card2 = createStatCard("🕐", "#fef3c7", "Total Hours Worked", "5,960", "+8%", true);
        VBox card3 = createStatCard("🎯", "#d1fae5", "Average Fill Rate", "96%", "+2%", true);
        VBox card4 = createStatCard("🏅", "#fef3c7", "Avg Performance Rating", "4.7", "-3%", false);

        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        HBox.setHgrow(card4, Priority.ALWAYS);

        statsBox.getChildren().addAll(card1, card2, card3, card4);
        return statsBox;
    }

    private VBox createStatCard(String icon, String iconBg, String title, String value, 
                                 String change, boolean positive) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(25));
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setMaxWidth(Double.MAX_VALUE);

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(28));
        iconLabel.setStyle("-fx-background-color: " + iconBg + "; -fx-background-radius: 10; -fx-padding: 10;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label changeLabel = new Label(change);
        changeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        if (positive) {
            changeLabel.setStyle("-fx-text-fill: #10b981;");
        } else {
            changeLabel.setStyle("-fx-text-fill: #ef4444;");
        }

        topRow.getChildren().addAll(iconLabel, spacer, changeLabel);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        valueLabel.setStyle("-fx-text-fill: #1f2937;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 13));
        titleLabel.setStyle("-fx-text-fill: #6b7280;");

        card.getChildren().addAll(topRow, valueLabel, titleLabel);
        return card;
    }

    private HBox createTabsSection() {
        HBox tabs = new HBox(20);
        tabs.setAlignment(Pos.CENTER_LEFT);
        tabs.setPadding(new Insets(10, 0, 10, 0));

        Button overviewTab = createTabButton("Overview", true);
        Button performanceTab = createTabButton("Performance", false);
        Button trendsTab = createTabButton("Trends", false);

        tabs.getChildren().addAll(overviewTab, performanceTab, trendsTab);
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

    private HBox createChartsRow1() {
        HBox row = new HBox(20);
        
        VBox barChartBox = createBarChartSection();
        VBox pieChartBox = createPieChartSection();
        
        HBox.setHgrow(barChartBox, Priority.ALWAYS);
        row.getChildren().addAll(barChartBox, pieChartBox);
        
        return row;
    }

    private HBox createChartsRow2() {
        HBox row = new HBox(20);
        
        VBox lineChartBox = createLineChartSection();
        HBox.setHgrow(lineChartBox, Priority.ALWAYS);
        row.getChildren().add(lineChartBox);
        
        return row;
    }

    private VBox createBarChartSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label titleLabel = new Label("Monthly Shift Volume");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #1f2937;");

        Canvas canvas = new Canvas(650, 300);
        drawBarChart(canvas);

        section.getChildren().addAll(titleLabel, canvas);
        return section;
    }

    private void drawBarChart(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        double[] values = {45, 52, 48, 62, 55, 58, 65, 59, 68, 62, 72, 65};
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        
        double maxValue = 80;
        double barWidth = 40;
        double spacing = 15;
        double chartHeight = 250;
        double baseY = 270;
        
        // Draw bars
        for (int i = 0; i < values.length; i++) {
            double barHeight = (values[i] / maxValue) * chartHeight;
            double x = 40 + i * (barWidth + spacing);
            double y = baseY - barHeight;
            
            gc.setFill(Color.web("#3498db"));
            gc.fillRoundRect(x, y, barWidth, barHeight, 4, 4);
            
            // Month labels
            gc.setFill(Color.web("#6b7280"));
            gc.setFont(Font.font("Arial", 11));
            gc.fillText(months[i], x + 10, baseY + 15);
        }
        
        // Legend
        gc.setFill(Color.web("#3498db"));
        gc.fillRect(40, 285, 12, 12);
        gc.setFill(Color.web("#1f2937"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText("Shifts", 58, 295);
    }

    private VBox createPieChartSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        section.setPrefWidth(450);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label titleLabel = new Label("Sport Distribution");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #1f2937;");

        Canvas canvas = new Canvas(400, 300);
        drawPieChart(canvas);

        section.getChildren().addAll(titleLabel, canvas);
        return section;
    }

    private void drawPieChart(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        double centerX = 150;
        double centerY = 150;
        double radius = 100;
        
        String[] labels = {"Basketball 28%", "Soccer 24%", "Football 18%", "Hockey 15%", "Baseball 10%"};
        double[] percentages = {28, 24, 18, 15, 10};
        String[] colors = {"#3498db", "#f39c12", "#2ecc71", "#e74c3c", "#9b59b6"};
        
        double startAngle = 0;
        for (int i = 0; i < percentages.length; i++) {
            double angle = (percentages[i] / 100) * 360;
            
            gc.setFill(Color.web(colors[i]));
            gc.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 
                      startAngle, angle, javafx.scene.shape.ArcType.ROUND);
            
            // Labels
            double labelAngle = Math.toRadians(startAngle + angle / 2);
            double labelX = centerX + Math.cos(labelAngle) * (radius + 60);
            double labelY = centerY - Math.sin(labelAngle) * (radius + 60);
            
            gc.setFill(Color.web(colors[i]));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            gc.fillText(labels[i], labelX + 10, labelY + 5);
            
            startAngle += angle;
        }
    }

    private VBox createLineChartSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label titleLabel = new Label("Hours Worked Trend");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #1f2937;");

        Canvas canvas = new Canvas(1280, 300);
        drawLineChart(canvas);

        section.getChildren().addAll(titleLabel, canvas);
        return section;
    }

    private void drawLineChart(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        double[] values = {320, 380, 420, 350, 480, 450, 520, 480, 550, 520, 580, 550};
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        
        double maxValue = 650;
        double chartHeight = 250;
        double baseY = 270;
        double spacing = 100;
        
        // Draw grid lines
        gc.setStroke(Color.web("#e5e7eb"));
        gc.setLineWidth(1);
        for (int i = 0; i <= 4; i++) {
            double y = baseY - (chartHeight / 4) * i;
            gc.strokeLine(60, y, 1220, y);
        }
        
        // Draw line
        gc.setStroke(Color.web("#f39c12"));
        gc.setLineWidth(3);
        
        for (int i = 0; i < values.length - 1; i++) {
            double x1 = 80 + i * spacing;
            double y1 = baseY - (values[i] / maxValue) * chartHeight;
            double x2 = 80 + (i + 1) * spacing;
            double y2 = baseY - (values[i + 1] / maxValue) * chartHeight;
            
            gc.strokeLine(x1, y1, x2, y2);
            
            // Draw points
            gc.setFill(Color.web("#f39c12"));
            gc.fillOval(x1 - 4, y1 - 4, 8, 8);
            
            if (i == values.length - 2) {
                gc.fillOval(x2 - 4, y2 - 4, 8, 8);
            }
        }
        
        // Month labels
        gc.setFill(Color.web("#6b7280"));
        gc.setFont(Font.font("Arial", 11));
        for (int i = 0; i < months.length; i++) {
            double x = 70 + i * spacing;
            gc.fillText(months[i], x, baseY + 20);
        }
        
        // Legend
        gc.setStroke(Color.web("#f39c12"));
        gc.setLineWidth(3);
        gc.strokeLine(40, 290, 60, 290);
        gc.setFill(Color.web("#1f2937"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText("Hours", 70, 295);
    }
}