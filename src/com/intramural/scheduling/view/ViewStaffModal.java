package com.intramural.scheduling.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ViewStaffModal {
    private Stage dialogStage;
    private String shiftName;
    private String shiftDetails;
    private String[][] assignedStaff;

    public ViewStaffModal(Stage parentStage, String shiftName, String shiftDetails, String[][] assignedStaff) {
        this.shiftName = shiftName;
        this.shiftDetails = shiftDetails;
        this.assignedStaff = assignedStaff;
        
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle("Assigned Staff - " + shiftName);
        dialogStage.setResizable(false);
    }

    public void show() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(550);

        // Header
        VBox header = new VBox(8);
        Label titleLabel = new Label(shiftName);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label detailsLabel = new Label(shiftDetails);
        detailsLabel.setFont(Font.font("Arial", 14));
        detailsLabel.setStyle("-fx-text-fill: #7f8c8d;");

        header.getChildren().addAll(titleLabel, detailsLabel);

        // Separator
        Separator separator = new Separator();

        // Assigned Staff Section
        Label assignedLabel = new Label("Assigned Staff (" + assignedStaff.length + ")");
        assignedLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        assignedLabel.setStyle("-fx-text-fill: #2c3e50;");

        VBox staffList = new VBox(12);
        for (String[] staff : assignedStaff) {
            HBox staffCard = createStaffCard(staff[0], staff[1], staff[2], staff[3]);
            staffList.getChildren().add(staffCard);
        }

        ScrollPane scrollPane = new ScrollPane(staffList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Close Button
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button closeBtn = new Button("Close");
        closeBtn.setPrefWidth(120);
        closeBtn.setPrefHeight(40);
        closeBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialogStage.close());

        buttonBox.getChildren().add(closeBtn);

        root.getChildren().addAll(header, separator, assignedLabel, scrollPane, buttonBox);

        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private HBox createStaffCard(String name, String role, String expertise, String phone) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;");

        // Avatar with initials
        String[] nameParts = name.split(" ");
        String initials = nameParts[0].substring(0, 1) + nameParts[1].substring(0, 1);
        
        Label avatar = new Label(initials);
        avatar.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        avatar.setStyle("-fx-background-color: #bfdbfe; -fx-text-fill: #1e40af; " +
                "-fx-background-radius: 25; -fx-min-width: 50; -fx-min-height: 50; -fx-alignment: center;");

        // Info
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        nameLabel.setStyle("-fx-text-fill: #1f2937;");

        Label roleLabel = new Label(role);
        roleLabel.setFont(Font.font("Arial", 12));
        roleLabel.setStyle("-fx-text-fill: #6b7280;");

        Label expertiseLabel = new Label("🏅 " + expertise);
        expertiseLabel.setFont(Font.font("Arial", 12));
        expertiseLabel.setStyle("-fx-text-fill: #6b7280;");

        Label phoneLabel = new Label("📞 " + phone);
        phoneLabel.setFont(Font.font("Arial", 12));
        phoneLabel.setStyle("-fx-text-fill: #6b7280;");

        infoBox.getChildren().addAll(nameLabel, roleLabel, expertiseLabel, phoneLabel);

        card.getChildren().addAll(avatar, infoBox);
        return card;
    }
}