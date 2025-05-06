package com.example.eventmanagement.controller;

import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.service.EventService;
import com.example.eventmanagement.service.ParticipationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class EventCalendarController {
    private final EventService eventService;
    private final ParticipationService participationService;
    private Stage stage;

    @FXML private VBox root;
    @FXML private ComboBox<String> viewComboBox;
    @FXML private Button todayButton, prevButton, nextButton, backButton;
    @FXML private TextField filterField;
    @FXML private GridPane calendarGrid;
    @FXML private TextField titleField, descField, timeField, locationField, organizerField, maxParticipantsField, statusField, ticketPriceField, registrationEndField;
    @FXML private DatePicker datePicker;
    @FXML private ChoiceBox<String> typeField;
    @FXML private Button saveButton, deleteButton, clearButton, shareFacebookButton;

    private LocalDate currentDate = LocalDate.now();
    private String currentView = "Month";
    private Event selectedEvent;

    public EventCalendarController(EventService eventService, ParticipationService participationService) {
        this.eventService = eventService;
        this.participationService = participationService;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        viewComboBox.setItems(FXCollections.observableArrayList("Month", "Week"));
        viewComboBox.setValue("Month");
        viewComboBox.setOnAction(e -> {
            currentView = viewComboBox.getValue();
            renderCalendar();
        });
        typeField.setItems(FXCollections.observableArrayList("", "Entertainment", "Cultural", "Educational", "Sports"));
        typeField.setValue("");
        deleteButton.setDisable(true);
        shareFacebookButton.setDisable(true); // Disable until an event is selected
        renderCalendar();
    }

    @FXML
    private void handleToday() {
        currentDate = LocalDate.now();
        renderCalendar();
    }

    @FXML
    private void handlePrevious() {
        if (currentView.equals("Month")) currentDate = currentDate.minusMonths(1);
        else if (currentView.equals("Week")) currentDate = currentDate.minusWeeks(1);
        renderCalendar();
    }

    @FXML
    private void handleNext() {
        if (currentView.equals("Month")) currentDate = currentDate.plusMonths(1);
        else if (currentView.equals("Week")) currentDate = currentDate.plusWeeks(1);
        renderCalendar();
    }

    @FXML
    private void handleFilter() {
        renderCalendar();
    }

    @FXML
    private void handleSaveEvent() {
        try {
            Event event = selectedEvent != null ? selectedEvent : new Event();
            event.setTitre(titleField.getText());
            event.setDescription(descField.getText().isEmpty() ? null : descField.getText());
            event.setDate(datePicker.getValue());
            event.setHeure(timeField.getText().isEmpty() ? null : LocalTime.parse(timeField.getText(), DateTimeFormatter.ofPattern("HH:mm")));
            event.setLieu(locationField.getText().isEmpty() ? null : locationField.getText());
            String typeValue = typeField.getValue();
            event.setType(typeValue.isEmpty() ? null : typeValue);
            event.setOrganisateurNom(organizerField.getText().isEmpty() ? null : organizerField.getText());
            event.setNombreMaxParticipants(maxParticipantsField.getText().isEmpty() ? null : Integer.parseInt(maxParticipantsField.getText()));
            event.setStatus(statusField.getText().isEmpty() ? null : statusField.getText());
            event.setTicketPrix(ticketPriceField.getText().isEmpty() ? null : ticketPriceField.getText());
            event.setPeriodeInscriptionFin(registrationEndField.getText().isEmpty() ? null : registrationEndField.getText());

            if (selectedEvent == null) {
                eventService.createEvent(event);
            } else {
                eventService.updateEvent(event.getEventId(), event);
            }
            clearForm();
            renderCalendar();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Event saved successfully.");
        } catch (IllegalArgumentException | DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid input: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteEvent() {
        if (selectedEvent != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selectedEvent.getTitre() + "?", ButtonType.YES, ButtonType.NO);
            confirm.getDialogPane().getStyleClass().add("alert");
            confirm.showAndWait().ifPresent(type -> {
                if (type == ButtonType.YES) {
                    eventService.deleteEvent(selectedEvent.getEventId());
                    clearForm();
                    renderCalendar();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Event deleted successfully.");
                }
            });
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    @FXML
    private void handleBackToEvents() throws IOException {
        EventController eventController = new EventController(eventService, participationService);
        eventController.setStage(stage);
        eventController.showEventListView();
    }

    @FXML
    private void handleShareToFacebook() {
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No event selected to share.");
            return;
        }

        try {
            // Construct the post message with event details
            StringBuilder postMessage = new StringBuilder();
            postMessage.append("Join us for ").append(selectedEvent.getTitre()).append("!\n");
            if (selectedEvent.getDescription() != null && !selectedEvent.getDescription().isEmpty()) {
                postMessage.append(selectedEvent.getDescription()).append("\n");
            }
            postMessage.append("📅 Date: ").append(selectedEvent.getDate());
            if (selectedEvent.getHeure() != null) {
                postMessage.append(" at ").append(selectedEvent.getHeure());
            }
            postMessage.append("\n📍 Location: ").append(selectedEvent.getLieu() != null ? selectedEvent.getLieu() : "TBA").append("\n");
            if (selectedEvent.getTicketPrix() != null && !selectedEvent.getTicketPrix().isEmpty()) {
                postMessage.append("🎟️ Ticket Price: ").append(selectedEvent.getTicketPrix());
            }

            // Encode the post message for URL
            String encodedMessage = URLEncoder.encode(postMessage.toString(), StandardCharsets.UTF_8);

            // Construct the Facebook sharing URL
            String facebookShareUrl = "https://www.facebook.com/sharer/sharer.php?u=" +
                    URLEncoder.encode("https://EventBadro-site.com", StandardCharsets.UTF_8) +
                    "&quote=" + encodedMessage;

            // Open the URL in the default browser
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(facebookShareUrl));
            } else {
                // Fallback: Show the URL in an alert for manual copying
                showAlert(Alert.AlertType.WARNING, "Browser Not Supported",
                        "Unable to open browser. Please copy this URL and paste it in your browser:\n" + facebookShareUrl);
            }
        } catch (IOException | java.net.URISyntaxException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to share event to Facebook: " + e.getMessage());
        }
    }

    private void renderCalendar() {
        calendarGrid.getChildren().clear();
        List<Event> events = eventService.getAllEvents();
        String filter = filterField.getText().toLowerCase();
        if (currentView.equals("Month")) {
            renderMonthView(events, filter);
        } else if (currentView.equals("Week")) {
            renderWeekView(events, filter);
        }
    }

    private void renderMonthView(List<Event> events, String filter) {
        LocalDate firstDay = currentDate.withDayOfMonth(1);
        int startDay = firstDay.getDayOfWeek().getValue() % 7; // Sunday = 0
        int daysInMonth = firstDay.lengthOfMonth();

        // Headers
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.getStyleClass().add("calendar-header");
            calendarGrid.add(dayLabel, i, 0);
        }

        // Days and events
        int row = 1;
        int col = startDay;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = firstDay.withDayOfMonth(day);
            VBox dayBox = new VBox();
            dayBox.getStyleClass().add("calendar-day");
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.getStyleClass().add("day-number");
            dayBox.getChildren().add(dayLabel);

            // Filter and add events
            for (Event event : events) {
                if (event.getDate() != null && event.getDate().equals(date) &&
                        (filter.isEmpty() || event.getTitre().toLowerCase().contains(filter) || (event.getLieu() != null && event.getLieu().toLowerCase().contains(filter)))) {
                    Label eventLabel = new Label(event.getTitre());
                    eventLabel.getStyleClass().add("event-label");
                    if (event.getType() != null && !event.getType().isEmpty()) {
                        eventLabel.getStyleClass().add(event.getType().toLowerCase());
                    }
                    dayBox.getChildren().add(eventLabel);
                    setupEventHandlers(dayBox, event);
                }
            }

            calendarGrid.add(dayBox, col, row);
            col++;
            if (col == 7) {
                col = 0;
                row++;
            }

            // Drag-and-drop target
            dayBox.setOnDragOver(e -> {
                if (e.getGestureSource() != dayBox && e.getDragboard().hasString()) {
                    e.acceptTransferModes(TransferMode.MOVE);
                }
                e.consume();
            });

            dayBox.setOnDragDropped(e -> {
                String eventId = e.getDragboard().getString();
                Event event = events.stream().filter(ev -> ev.getEventId().toString().equals(eventId)).findFirst().orElse(null);
                if (event != null) {
                    event.setDate(date);
                    eventService.updateEvent(event.getEventId(), event);
                    renderCalendar();
                }
                e.setDropCompleted(true);
                e.consume();
            });
        }
    }

    private void renderWeekView(List<Event> events, String filter) {
        LocalDate startOfWeek = currentDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY));
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i] + " " + startOfWeek.plusDays(i).getDayOfMonth());
            dayLabel.getStyleClass().add("calendar-header");
            calendarGrid.add(dayLabel, i, 0);
        }

        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            VBox dayBox = new VBox();
            dayBox.getStyleClass().add("calendar-day");
            for (Event event : events) {
                if (event.getDate() != null && event.getDate().equals(date) &&
                        (filter.isEmpty() || event.getTitre().toLowerCase().contains(filter) || (event.getLieu() != null && event.getLieu().toLowerCase().contains(filter)))) {
                    Label eventLabel = new Label(event.getTitre());
                    eventLabel.getStyleClass().add("event-label");
                    if (event.getType() != null && !event.getType().isEmpty()) {
                        eventLabel.getStyleClass().add(event.getType().toLowerCase());
                    }
                    dayBox.getChildren().add(eventLabel);
                    setupEventHandlers(dayBox, event);
                }
            }
            calendarGrid.add(dayBox, i, 1);
        }
    }

    private void setupEventHandlers(VBox dayBox, Event event) {
        dayBox.setOnMouseClicked(e -> {
            selectedEvent = event;
            titleField.setText(event.getTitre());
            descField.setText(event.getDescription());
            datePicker.setValue(event.getDate());
            timeField.setText(event.getHeure() != null ? event.getHeure().toString() : "");
            locationField.setText(event.getLieu());
            typeField.setValue(event.getType() != null ? event.getType() : "");
            organizerField.setText(event.getOrganisateurNom());
            maxParticipantsField.setText(event.getNombreMaxParticipants() != null ? event.getNombreMaxParticipants().toString() : "");
            statusField.setText(event.getStatus());
            ticketPriceField.setText(event.getTicketPrix());
            registrationEndField.setText(event.getPeriodeInscriptionFin());
            deleteButton.setDisable(false);
            shareFacebookButton.setDisable(false); // Enable sharing when an event is selected
        });

        dayBox.setOnDragDetected(e -> {
            Dragboard db = dayBox.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(event.getEventId().toString());
            db.setContent(content);
            e.consume();
        });
    }

    private void clearForm() {
        titleField.clear();
        descField.clear();
        datePicker.setValue(null);
        timeField.clear();
        locationField.clear();
        typeField.setValue("");
        organizerField.clear();
        maxParticipantsField.clear();
        statusField.clear();
        ticketPriceField.clear();
        registrationEndField.clear();
        selectedEvent = null;
        deleteButton.setDisable(true);
        shareFacebookButton.setDisable(true); // Disable sharing when form is cleared
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("alert");
        alert.showAndWait();
    }
}