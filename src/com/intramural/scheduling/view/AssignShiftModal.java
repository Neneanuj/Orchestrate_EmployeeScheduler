package com.intramural.scheduling.view;

import com.intramural.scheduling.controller.SchedulingController;
import com.intramural.scheduling.model.Employee;
import com.intramural.scheduling.model.Schedule;
import com.intramural.scheduling.model.SchedulingRecommendation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignShiftModal {
    private Stage dialogStage;
    private SchedulingController schedulingController;
    private Schedule.Game game;
    private Map<Schedule.Shift, ToggleGroup> shiftAssignments;
    private boolean assignmentsFinalized = false;

    public AssignShiftModal(Stage parentStage, SchedulingController controller, Schedule.Game game) {
        this.schedulingController = controller;
        this.game = game;
        this.shiftAssignments = new HashMap<>();
        
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle("Assign Staff - " + game.getLocation());
        dialogStage.setResizable(false);
    }

    public void show() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(700);
        root.setMaxHeight(600);

        // Header
        VBox header = createHeader();

        // ScrollPane for shifts
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        
        VBox shiftsBox = new VBox(15);
        shiftsBox.setPadding(new Insets(10, 0, 10, 0));
        
        // Create assignment UI for each shift
        for (Schedule.Shift shift : game.getShifts()) {
            VBox shiftCard = createShiftAssignmentCard(shift);
            shiftsBox.getChildren().add(shiftCard);
        }
        
        scrollPane.setContent(shiftsBox);

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setStyle("-fx-background-color: white; -fx-text-fill: #2c3e50; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-font-weight: bold; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialogStage.close());

        Button finalizeBtn = new Button("Finalize Assignments");
        finalizeBtn.setPrefWidth(180);
        finalizeBtn.setPrefHeight(40);
        finalizeBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        finalizeBtn.setOnAction(e -> handleFinalize());

        buttonBox.getChildren().addAll(cancelBtn, finalizeBtn);

        root.getChildren().addAll(header, scrollPane, buttonBox);

        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private VBox createHeader() {
        VBox header = new VBox(5);
        
        Label titleLabel = new Label("Assign Staff to Shifts");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitleLabel = new Label(game.getLocation() + " • " + 
                game.getGameDate() + " • " + game.getStartTime() + " - " + game.getEndTime());
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");

        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }

    private VBox createShiftAssignmentCard(Schedule.Shift shift) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 8;");

        // Shift header
        Label shiftLabel = new Label(shift.getPositionLabel());
        shiftLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        shiftLabel.setStyle("-fx-text-fill: #1f2937;");

        // Get recommendations
        List<SchedulingRecommendation> recommendations = 
                schedulingController.getRecommendations(shift.getShiftId());

        if (recommendations.isEmpty()) {
            Label noRecsLabel = new Label("No recommendations available");
            noRecsLabel.setStyle("-fx-text-fill: #e74c3c;");
            card.getChildren().addAll(shiftLabel, noRecsLabel);
            return card;
        }

        // Create toggle group for this shift
        ToggleGroup toggleGroup = new ToggleGroup();
        shiftAssignments.put(shift, toggleGroup);

        VBox optionsBox = new VBox(10);
        
        // Add up to 2 recommendations
        for (int i = 0; i < Math.min(2, recommendations.size()); i++) {
            SchedulingRecommendation rec = recommendations.get(i);
            HBox optionCard = createOptionCard(rec, toggleGroup, i == 0);
            optionsBox.getChildren().add(optionCard);
        }

        card.getChildren().addAll(shiftLabel, optionsBox);
        return card;
    }

    private HBox createOptionCard(SchedulingRecommendation rec, ToggleGroup group, boolean isFirst) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 6; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 6;");

        RadioButton radioBtn = new RadioButton();
        radioBtn.setToggleGroup(group);
        radioBtn.setUserData(rec.getEmployee().getEmployeeId());
        if (isFirst) {
            radioBtn.setSelected(true); // Pre-select first option
        }

        Employee emp = rec.getEmployee();
        
        // Avatar
        String initials = emp.getFirstName().substring(0, 1) + emp.getLastName().substring(0, 1);
        Label avatar = new Label(initials);
        avatar.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        avatar.setStyle("-fx-background-color: #bfdbfe; -fx-text-fill: #1e40af; " +
                "-fx-background-radius: 20; -fx-min-width: 40; -fx-min-height: 40; -fx-alignment: center;");

        // Info
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(emp.getFullName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nameLabel.setStyle("-fx-text-fill: #1f2937;");

        Label scoreLabel = new Label(String.format("Score: %.1f", rec.getScore()));
        scoreLabel.setFont(Font.font("Arial", 12));
        scoreLabel.setStyle("-fx-text-fill: #6b7280;");

        // Show warnings if any
        if (!rec.getWarnings().isEmpty()) {
            Label warningLabel = new Label("⚠️ " + rec.getWarnings().get(0));
            warningLabel.setFont(Font.font("Arial", 11));
            warningLabel.setStyle("-fx-text-fill: #f59e0b;");
            infoBox.getChildren().addAll(nameLabel, scoreLabel, warningLabel);
        } else {
            infoBox.getChildren().addAll(nameLabel, scoreLabel);
        }

        card.getChildren().addAll(radioBtn, avatar, infoBox);
        return card;
    }

    private void handleFinalize() {
        try {
            // Assign each shift based on selections
            for (Map.Entry<Schedule.Shift, ToggleGroup> entry : shiftAssignments.entrySet()) {
                Schedule.Shift shift = entry.getKey();
                ToggleGroup group = entry.getValue();
                
                if (group.getSelectedToggle() != null) {
                    int employeeId = (int) group.getSelectedToggle().getUserData();
                    // schedulingController.assignShift(shift, employeeId, game);
                    // TODO: Implement shift assignment
                    System.out.println("Assigning shift " + shift.getShiftId() + " to employee " + employeeId);
                }
            }
            
            assignmentsFinalized = true;
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("All shifts have been successfully assigned!");
            alert.showAndWait();
            
            dialogStage.close();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to assign shifts: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public boolean wereAssignmentsFinalized() {
        return assignmentsFinalized;
    }
}