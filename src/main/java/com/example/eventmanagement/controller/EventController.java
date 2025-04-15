package com.example.eventmanagement.controller;

import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.service.EventService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class EventController {
    private final EventService eventService = new EventService();
    private final ParticipationController participationController = new ParticipationController();
    private Stage stage;

    public VBox getEventListView() {
        VBox eventListView = new VBox(10);
        eventListView.setPadding(new Insets(20));
        eventListView.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label("Events");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button createButton = new Button("Create Event");
        createButton.setStyle("-fx-font-size: 14px;");
        createButton.setOnAction(e -> showCreateEventForm());

        List<Event> events = eventService.getAllEvents();
        VBox eventsBox = new VBox(5);
        if (events.isEmpty()) {
            Label noEventsLabel = new Label("No events found. Create one!");
            noEventsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
            eventsBox.getChildren().add(noEventsLabel);
        } else {
            for (Event event : events) {
                HBox eventBox = new HBox(15);
                eventBox.setAlignment(Pos.CENTER_LEFT);
                Label eventTitle = new Label(event.getTitre() + " (" + (event.getType() != null ? event.getType() : "No type") + ")");
                eventTitle.setStyle("-fx-font-size: 14px;");
                eventTitle.setPrefWidth(200);
                Button viewButton = new Button("View");
                viewButton.setStyle("-fx-font-size: 12px;");
                Button editButton = new Button("Edit");
                editButton.setStyle("-fx-font-size: 12px;");
                Button deleteButton = new Button("Delete");
                deleteButton.setStyle("-fx-font-size: 12px;");
                viewButton.setOnAction(e -> showEventView(event.getId()));
                editButton.setOnAction(e -> showEditEventForm(event.getId()));
                deleteButton.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + event.getTitre() + "?", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(type -> {
                        if (type == ButtonType.YES) {
                            eventService.deleteEvent(event.getId());
                            refreshList();
                        }
                    });
                });
                eventBox.getChildren().addAll(eventTitle, viewButton, editButton, deleteButton);
                eventsBox.getChildren().add(eventBox);
            }
        }

        eventListView.getChildren().addAll(titleLabel, createButton, eventsBox);
        return eventListView;
    }

    private void showCreateEventForm() {
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);

        TextField titleField = new TextField();
        titleField.setPromptText("Title (5-255 characters)");
        TextArea descField = new TextArea();
        descField.setPromptText("Description (optional)");
        descField.setPrefRowCount(3);
        DatePicker dateField = new DatePicker();
        dateField.setPromptText("Select Date");
        TextField timeField = new TextField();
        timeField.setPromptText("HH:MM (e.g., 14:30)");
        TextField locationField = new TextField();
        locationField.setPromptText("Location (optional)");
        TextField typeField = new TextField();
        typeField.setPromptText("Type (optional)");
        TextField organizerField = new TextField();
        organizerField.setPromptText("Organizer Name (optional)");
        TextField maxParticipantsField = new TextField();
        maxParticipantsField.setPromptText("Max Participants (optional)");
        TextField statusField = new TextField();
        statusField.setPromptText("Status (optional)");
        TextField ticketPriceField = new TextField();
        ticketPriceField.setPromptText("Ticket Price (optional)");
        TextField registrationEndField = new TextField();
        registrationEndField.setPromptText("Registration End (optional)");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-font-size: 14px;");
        saveButton.setOnAction(e -> {
            try {
                Event event = new Event();
                event.setTitre(titleField.getText());
                event.setDescription(descField.getText().isEmpty() ? null : descField.getText());
                event.setDate(dateField.getValue());
                event.setHeure(timeField.getText().isEmpty() ? LocalTime.now() : LocalTime.parse(timeField.getText()));
                event.setLieu(locationField.getText().isEmpty() ? null : locationField.getText());
                event.setType(typeField.getText().isEmpty() ? null : typeField.getText());
                event.setOrganisateurNom(organizerField.getText().isEmpty() ? null : organizerField.getText());
                event.setNombreMaxParticipants(maxParticipantsField.getText().isEmpty() ? null : Integer.parseInt(maxParticipantsField.getText()));
                event.setStatus(statusField.getText().isEmpty() ? null : statusField.getText());
                event.setTicketPrix(ticketPriceField.getText().isEmpty() ? null : ticketPriceField.getText());
                event.setPeriodeInscriptionFin(registrationEndField.getText().isEmpty() ? null : registrationEndField.getText());
                eventService.createEvent(event);
                refreshList();
            } catch (IllegalArgumentException | java.time.format.DateTimeParseException ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        Label createLabel = new Label("Create Event");
        createLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        form.getChildren().addAll(
                createLabel,
                titleField, descField, dateField, timeField, locationField, typeField,
                organizerField, maxParticipantsField, statusField, ticketPriceField,
                registrationEndField, errorLabel, saveButton
        );

        stage.setScene(new Scene(form, 400, 600));
    }

    private void showEditEventForm(Integer id) {
        Optional<Event> eventOpt = eventService.getEventById(id);
        if (eventOpt.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Event not found");
            alert.showAndWait();
            return;
        }

        Event event = eventOpt.get();
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);

        TextField titleField = new TextField(event.getTitre());
        titleField.setPromptText("Title (5-255 characters)");
        TextArea descField = new TextArea(event.getDescription());
        descField.setPromptText("Description (optional)");
        descField.setPrefRowCount(3);
        DatePicker dateField = new DatePicker(event.getDate());
        TextField timeField = new TextField(event.getHeure() != null ? event.getHeure().toString() : "");
        timeField.setPromptText("HH:MM (e.g., 14:30)");
        TextField locationField = new TextField(event.getLieu());
        locationField.setPromptText("Location (optional)");
        TextField typeField = new TextField(event.getType());
        typeField.setPromptText("Type (optional)");
        TextField organizerField = new TextField(event.getOrganisateurNom());
        organizerField.setPromptText("Organizer Name (optional)");
        TextField maxParticipantsField = new TextField(event.getNombreMaxParticipants() != null ? event.getNombreMaxParticipants().toString() : "");
        maxParticipantsField.setPromptText("Max Participants (optional)");
        TextField statusField = new TextField(event.getStatus());
        statusField.setPromptText("Status (optional)");
        TextField ticketPriceField = new TextField(event.getTicketPrix());
        ticketPriceField.setPromptText("Ticket Price (optional)");
        TextField registrationEndField = new TextField(event.getPeriodeInscriptionFin());
        registrationEndField.setPromptText("Registration End (optional)");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button saveButton = new Button("Update");
        saveButton.setStyle("-fx-font-size: 14px;");
        saveButton.setOnAction(e -> {
            try {
                event.setTitre(titleField.getText());
                event.setDescription(descField.getText().isEmpty() ? null : descField.getText());
                event.setDate(dateField.getValue());
                event.setHeure(timeField.getText().isEmpty() ? LocalTime.now() : LocalTime.parse(timeField.getText()));
                event.setLieu(locationField.getText().isEmpty() ? null : locationField.getText());
                event.setType(typeField.getText().isEmpty() ? null : typeField.getText());
                event.setOrganisateurNom(organizerField.getText().isEmpty() ? null : organizerField.getText());
                event.setNombreMaxParticipants(maxParticipantsField.getText().isEmpty() ? null : Integer.parseInt(maxParticipantsField.getText()));
                event.setStatus(statusField.getText().isEmpty() ? null : statusField.getText());
                event.setTicketPrix(ticketPriceField.getText().isEmpty() ? null : ticketPriceField.getText());
                event.setPeriodeInscriptionFin(registrationEndField.getText().isEmpty() ? null : registrationEndField.getText());
                eventService.updateEvent(id, event);
                refreshList();
            } catch (IllegalArgumentException | java.time.format.DateTimeParseException ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        Label editLabel = new Label("Edit Event");
        editLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        form.getChildren().addAll(
                editLabel,
                titleField, descField, dateField, timeField, locationField, typeField,
                organizerField, maxParticipantsField, statusField, ticketPriceField,
                registrationEndField, errorLabel, saveButton
        );

        stage.setScene(new Scene(form, 400, 600));
    }

    private void showEventView(Integer id) {
        Optional<Event> eventOpt = eventService.getEventById(id);
        if (eventOpt.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Event not found");
            alert.showAndWait();
            return;
        }

        Event event = eventOpt.get();
        VBox view = new VBox(10);
        view.setPadding(new Insets(20));
        view.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Event: " + event.getTitre());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Text typeText = new Text("Type: " + (event.getType() != null ? event.getType() : "None"));
        typeText.setStyle("-fx-font-size: 14px;");

        Text descText = new Text("Description: " + (event.getDescription() != null ? event.getDescription() : "None"));
        descText.setStyle("-fx-font-size: 14px;");

        Text dateText = new Text("Date: " + (event.getDate() != null ? event.getDate() : "None"));
        dateText.setStyle("-fx-font-size: 14px;");

        Text timeText = new Text("Time: " + (event.getHeure() != null ? event.getHeure() : "None"));
        timeText.setStyle("-fx-font-size: 14px;");

        Text locationText = new Text("Location: " + (event.getLieu() != null ? event.getLieu() : "None"));
        locationText.setStyle("-fx-font-size: 14px;");

        Text organizerText = new Text("Organizer: " + (event.getOrganisateurNom() != null ? event.getOrganisateurNom() : "None"));
        organizerText.setStyle("-fx-font-size: 14px;");

        Text maxParticipantsText = new Text("Max Participants: " + (event.getNombreMaxParticipants() != null ? event.getNombreMaxParticipants() : "None"));
        maxParticipantsText.setStyle("-fx-font-size: 14px;");

        Text statusText = new Text("Status: " + (event.getStatus() != null ? event.getStatus() : "None"));
        statusText.setStyle("-fx-font-size: 14px;");

        Text ticketPriceText = new Text("Ticket Price: " + (event.getTicketPrix() != null ? event.getTicketPrix() : "None"));
        ticketPriceText.setStyle("-fx-font-size: 14px;");

        Text registrationEndText = new Text("Registration Ends: " + (event.getPeriodeInscriptionFin() != null ? event.getPeriodeInscriptionFin() : "None"));
        registrationEndText.setStyle("-fx-font-size: 14px;");

        view.getChildren().addAll(
                titleLabel, typeText, descText, dateText, timeText, locationText,
                organizerText, maxParticipantsText, statusText, ticketPriceText, registrationEndText
        );

        Button participationButton = new Button("Manage Participations");
        participationButton.setStyle("-fx-font-size: 14px;");
        participationButton.setOnAction(e -> {
            participationController.setStage(stage);
            participationController.setBackToEventView(() -> showEventView(id));
            stage.setScene(new Scene(participationController.getParticipationListView(id), 400, 500));
        });

        Button backButton = new Button("Back to List");
        backButton.setStyle("-fx-font-size: 14px;");
        backButton.setOnAction(e -> refreshList());

        view.getChildren().addAll(participationButton, backButton);
        stage.setScene(new Scene(view, 400, 600));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        participationController.setStage(stage);
    }

    private void refreshList() {
        stage.setScene(new Scene(getEventListView(), 800, 600));
    }
}