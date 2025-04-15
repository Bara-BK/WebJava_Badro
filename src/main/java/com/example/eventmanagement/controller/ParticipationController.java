package com.example.eventmanagement.controller;

import com.example.eventmanagement.entity.Participation;
import com.example.eventmanagement.service.EventService;
import com.example.eventmanagement.service.ParticipationService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ParticipationController {
    private final ParticipationService participationService = new ParticipationService();
    private Stage stage;
    private Runnable backToEventView;

    public VBox getParticipationListView(Integer eventId) {
        VBox view = new VBox(10);
        view.setPadding(new Insets(20));
        view.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Participations");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button createButton = new Button("Add Participation");
        createButton.setStyle("-fx-font-size: 14px;");
        createButton.setOnAction(e -> showCreateParticipationForm(eventId));

        List<Participation> participations = participationService.getParticipationsByEventId(eventId);
        VBox participationsBox = new VBox(5);
        if (participations.isEmpty()) {
            Label noParticipationsLabel = new Label("No participations found. Add one!");
            noParticipationsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
            participationsBox.getChildren().add(noParticipationsLabel);
        } else {
            for (Participation p : participations) {
                HBox participationBox = new HBox(15);
                Label nameLabel = new Label(p.getNomParticipant());
                nameLabel.setStyle("-fx-font-size: 14px;");
                nameLabel.setPrefWidth(200);
                Button editButton = new Button("Edit");
                editButton.setStyle("-fx-font-size: 12px;");
                Button deleteButton = new Button("Delete");
                deleteButton.setStyle("-fx-font-size: 12px;");
                editButton.setOnAction(e -> showEditParticipationForm(p.getId()));
                deleteButton.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + p.getNomParticipant() + "?", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(type -> {
                        if (type == ButtonType.YES) {
                            participationService.deleteParticipation(p.getId());
                            refreshList(eventId);
                        }
                    });
                });
                participationBox.getChildren().addAll(nameLabel, editButton, deleteButton);
                participationsBox.getChildren().add(participationBox);
            }
        }

        Button backButton = new Button("Back to Event");
        backButton.setStyle("-fx-font-size: 14px;");
        backButton.setOnAction(e -> backToEventView.run());

        view.getChildren().addAll(titleLabel, createButton, participationsBox, backButton);
        return view;
    }

    private void showCreateParticipationForm(Integer eventId) {
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);

        TextField nameField = new TextField();
        nameField.setPromptText("Participant Name (2-255 characters)");
        DatePicker dateField = new DatePicker();
        dateField.setPromptText("Registration Date (optional)");
        TextField eventNameField = new TextField();
        eventNameField.setPromptText("Event Name (optional)");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number (optional)");
        TextField ticketCodeField = new TextField();
        ticketCodeField.setPromptText("Ticket Code (optional)");
        TextField paymentMethodField = new TextField();
        paymentMethodField.setPromptText("Payment Method (optional)");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-font-size: 14px;");
        saveButton.setOnAction(e -> {
            try {
                Participation p = new Participation();
                p.setIdEvent(eventId);
                p.setNomParticipant(nameField.getText());
                p.setDateInscription(dateField.getValue());
                p.setEvenementNom(eventNameField.getText().isEmpty() ? null : eventNameField.getText());
                p.setTelephoneNumber(phoneField.getText().isEmpty() ? null : Integer.parseInt(phoneField.getText()));
                p.setTicketCode(ticketCodeField.getText().isEmpty() ? null : ticketCodeField.getText());
                p.setPaimentMethod(paymentMethodField.getText().isEmpty() ? null : paymentMethodField.getText());
                participationService.createParticipation(p);
                refreshList(eventId);
            } catch (IllegalArgumentException ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        Label addLabel = new Label("Add Participation");
        addLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        form.getChildren().addAll(
                addLabel,
                nameField, dateField, eventNameField, phoneField, ticketCodeField, paymentMethodField,
                errorLabel, saveButton
        );
        stage.setScene(new Scene(form, 400, 500));
    }

    private void showEditParticipationForm(Integer id) {
        Optional<Participation> participationOpt = participationService.getParticipationById(id);
        if (participationOpt.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Participation not found");
            alert.showAndWait();
            return;
        }

        Participation p = participationOpt.get();
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);

        TextField nameField = new TextField(p.getNomParticipant());
        nameField.setPromptText("Participant Name (2-255 characters)");
        DatePicker dateField = new DatePicker(p.getDateInscription());
        dateField.setPromptText("Registration Date (optional)");
        TextField eventNameField = new TextField(p.getEvenementNom());
        eventNameField.setPromptText("Event Name (optional)");
        TextField phoneField = new TextField(p.getTelephoneNumber() != null ? p.getTelephoneNumber().toString() : "");
        phoneField.setPromptText("Phone Number (optional)");
        TextField ticketCodeField = new TextField(p.getTicketCode());
        ticketCodeField.setPromptText("Ticket Code (optional)");
        TextField paymentMethodField = new TextField(p.getPaimentMethod());
        paymentMethodField.setPromptText("Payment Method (optional)");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button saveButton = new Button("Update");
        saveButton.setStyle("-fx-font-size: 14px;");
        saveButton.setOnAction(e -> {
            try {
                p.setNomParticipant(nameField.getText());
                p.setDateInscription(dateField.getValue());
                p.setEvenementNom(eventNameField.getText().isEmpty() ? null : eventNameField.getText());
                p.setTelephoneNumber(phoneField.getText().isEmpty() ? null : Integer.parseInt(phoneField.getText()));
                p.setTicketCode(ticketCodeField.getText().isEmpty() ? null : ticketCodeField.getText());
                p.setPaimentMethod(paymentMethodField.getText().isEmpty() ? null : paymentMethodField.getText());
                participationService.updateParticipation(id, p);
                refreshList(p.getIdEvent());
            } catch (IllegalArgumentException ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        Label editLabel = new Label("Edit Participation");
        editLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        form.getChildren().addAll(
                editLabel,
                nameField, dateField, eventNameField, phoneField, ticketCodeField, paymentMethodField,
                errorLabel, saveButton
        );
        stage.setScene(new Scene(form, 400, 500));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setBackToEventView(Runnable backToEventView) {
        this.backToEventView = backToEventView;
    }

    private void refreshList(Integer eventId) {
        stage.setScene(new Scene(getParticipationListView(eventId), 400, 500));
    }
}