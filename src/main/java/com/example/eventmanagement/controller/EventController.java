package com.example.eventmanagement.controller;

import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.service.EventService;
import com.example.eventmanagement.service.ParticipationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EventController {
    private final EventService eventService;
    private final ParticipationService participationService;
    private final ParticipationController participationController;
    private Stage stage;
    private Integer currentEventId;
    private FilteredList<Event> filteredEvents;
    private SortedList<Event> sortedEvents;

    // Common data
    public ObservableList<String> typeItems = FXCollections.observableArrayList("", "Entertainment", "Cultural", "Educational", "Sports");
    private ObservableList<String> sortOptions = FXCollections.observableArrayList("Sort by Title", "Sort by Date", "Sort by ID");

    // EventListView.fxml
    @FXML private BorderPane root;
    @FXML private ImageView logoView;
    @FXML private Label titleLabel;
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> sortChoiceBox;
    @FXML private Button createButton;
    @FXML private Button statisticsButton;
    @FXML private Button calendarButton;
    @FXML private VBox eventsBox;

    // CreateEventForm.fxml / EditEventForm.fxml
    @FXML private VBox form;
    @FXML private TextField titleField;
    @FXML private TextArea descField;
    @FXML private DatePicker dateField;
    @FXML private TextField timeField;
    @FXML private TextField locationField;
    @FXML private ChoiceBox<String> typeField;
    @FXML private TextField organizerField;
    @FXML private TextField maxParticipantsField;
    @FXML private TextField statusField;
    @FXML private TextField ticketPriceField;
    @FXML private TextField registrationEndField;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // EventView.fxml
    @FXML private VBox view;
    @FXML private Label titleLabelView;
    @FXML private Text typeText;
    @FXML private Text descText;
    @FXML private Text dateText;
    @FXML private Text timeText;
    @FXML private Text locationText;
    @FXML private Text organizerText;
    @FXML private Text maxParticipantsText;
    @FXML private Text statusText;
    @FXML private Text ticketPriceText;
    @FXML private Text registrationEndText;
    @FXML private Button participationButton;
    @FXML private Button backButton;

    // StatisticsView.fxml
    @FXML private VBox statisticsRoot;
    @FXML private ImageView statsLogoView;
    @FXML private Label statsTitleLabel;
    @FXML private PieChart eventTypePieChart;
    @FXML private Button statsBackButton;
    @FXML private Button showEventTypeChartButton;
    @FXML private Button showParticipantChartButton;
    @FXML private StackPane chartContainer;
    @FXML private BarChart<String, Number> participantBarChart;
    @FXML private CategoryAxis participantXAxis;
    @FXML private NumberAxis participantYAxis;

    public EventController(EventService eventService, ParticipationService participationService) {
        this.eventService = eventService;
        this.participationService = participationService;
        this.participationController = new ParticipationController(eventService, participationService);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        participationController.setStage(stage);
    }

    public void showEventListView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventListView.fxml"));
        loader.setController(this);
        Scene scene = new Scene(loader.load(), 800, 600);
        java.net.URL cssUrl = getClass().getResource("/css/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Warning: /css/styles.css not found");
        }
        stage.setScene(scene);
        stage.setTitle("Event Management - Events");
        stage.show();
        initializeEventList();
        initializeSortChoiceBox();
        populateEventList();
    }

    @FXML
    private void showCalendarView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventCalendarView.fxml"));
        EventCalendarController calendarController = new EventCalendarController(eventService, participationService);
        calendarController.setStage(stage);
        loader.setController(calendarController);
        Scene scene = new Scene(loader.load(), 800, 600);
        java.net.URL cssUrl = getClass().getResource("/css/calendar.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Warning: /css/calendar.css not found");
        }
        stage.setScene(scene);
        stage.setTitle("Event Management - Calendar");
    }

    private void initializeEventList() {
        List<Event> allEvents = eventService.getAllEvents();
        ObservableList<Event> observableEvents = FXCollections.observableArrayList(allEvents);
        filteredEvents = new FilteredList<>(observableEvents, p -> true);
        sortedEvents = new SortedList<>(filteredEvents);
        sortedEvents.setComparator(Comparator.comparing(Event::getTitre, String.CASE_INSENSITIVE_ORDER));
    }

    private void initializeSortChoiceBox() {
        sortChoiceBox.setItems(sortOptions);
        sortChoiceBox.setValue("Sort by Title");
    }

    @FXML
    private void sortEvents() {
        String selectedSort = sortChoiceBox.getValue();
        switch (selectedSort) {
            case "Sort by Title":
                sortedEvents.setComparator(Comparator.comparing(Event::getTitre, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Sort by Date":
                sortedEvents.setComparator(Comparator.comparing(Event::getDate, Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case "Sort by ID":
                sortedEvents.setComparator(Comparator.comparing(Event::getEventId));
                break;
        }
        populateEventList();
    }

    @FXML
    private void filterEvents() {
        String searchText = searchField.getText().toLowerCase().trim();
        filteredEvents.setPredicate(event -> {
            if (searchText.isEmpty()) {
                return true;
            }
            return event.getTitre().toLowerCase().contains(searchText);
        });
        populateEventList();
    }

    private void populateEventList() {
        eventsBox.getChildren().clear();
        if (sortedEvents.isEmpty()) {
            Label noEventsLabel = new Label("No events found. Create one!");
            noEventsLabel.getStyleClass().add("no-events");
            eventsBox.getChildren().add(noEventsLabel);
        } else {
            for (Event event : sortedEvents) {
                HBox eventBox = new HBox(15);
                eventBox.getStyleClass().add("event-item");
                eventBox.setAlignment(Pos.CENTER_LEFT);

                Label eventTitle = new Label(event.getTitre() + " (" + (event.getType() != null ? event.getType() : "No type") + ")");
                eventTitle.getStyleClass().add("event-title");
                if (event.getType() != null && !event.getType().isEmpty()) {
                    eventTitle.getStyleClass().add(event.getType().toLowerCase());
                }

                Button viewButton = new Button("View");
                viewButton.getStyleClass().add("primary-button");
                viewButton.setOnAction(e -> {
                    try {
                        showEventView(event.getEventId());
                    } catch (IOException ex) {
                        showError("Failed to load event view");
                    }
                });

                Button editButton = new Button("Edit");
                editButton.getStyleClass().add("primary-button");
                editButton.setOnAction(e -> {
                    try {
                        showEditEventForm(event.getEventId());
                    } catch (IOException ex) {
                        showError("Failed to load edit form");
                    }
                });

                Button deleteButton = new Button("Delete");
                deleteButton.getStyleClass().add("delete-button");
                deleteButton.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + event.getTitre() + "?", ButtonType.YES, ButtonType.NO);
                    confirm.getDialogPane().getStyleClass().add("alert");
                    confirm.showAndWait().ifPresent(type -> {
                        if (type == ButtonType.YES) {
                            eventService.deleteEvent(event.getEventId());
                            initializeEventList();
                            populateEventList();
                        }
                    });
                });

                eventBox.getChildren().addAll(eventTitle, viewButton, editButton, deleteButton);
                eventsBox.getChildren().add(eventBox);
            }
        }
    }

    @FXML
    private void handleCreateEvent() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        java.net.URL fxmlUrl = getClass().getResource("/fxml/CreateEventForm.fxml");
        if (fxmlUrl == null) {
            throw new IOException("Cannot find /fxml/CreateEventForm.fxml");
        }
        System.out.println("Loading FXML: " + fxmlUrl);
        loader.setLocation(fxmlUrl);
        loader.setController(this);
        Scene scene = new Scene(loader.load(), 400, 600);
        java.net.URL cssUrl = getClass().getResource("/css/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Warning: /css/styles.css not found");
        }
        typeField.setItems(typeItems);
        typeField.setValue("");
        stage.setScene(scene);
    }

    @FXML
    private void handleSaveEvent() {
        try {
            Event event = new Event();
            event.setTitre(titleField.getText());
            event.setDescription(descField.getText().isEmpty() ? null : descField.getText());
            event.setDate(dateField.getValue());
            event.setHeure(timeField.getText().isEmpty() ? LocalTime.now() : LocalTime.parse(timeField.getText()));
            event.setLieu(locationField.getText().isEmpty() ? null : locationField.getText());
            String typeValue = typeField.getValue();
            event.setType(typeValue.isEmpty() ? null : typeValue);
            event.setOrganisateurNom(organizerField.getText().isEmpty() ? null : organizerField.getText());
            event.setNombreMaxParticipants(maxParticipantsField.getText().isEmpty() ? null : Integer.parseInt(maxParticipantsField.getText()));
            event.setStatus(statusField.getText().isEmpty() ? null : statusField.getText());
            event.setTicketPrix(ticketPriceField.getText().isEmpty() ? null : ticketPriceField.getText());
            event.setPeriodeInscriptionFin(registrationEndField.getText().isEmpty() ? null : registrationEndField.getText());
            eventService.createEvent(event);
            showEventListView();
        } catch (IllegalArgumentException | java.time.format.DateTimeParseException ex) {
            errorLabel.setText(ex.getMessage());
        } catch (IOException ex) {
            showError("Failed to return to event list");
        }
    }

    private void showEditEventForm(Integer id) throws IOException {
        Optional<Event> eventOpt = eventService.getEventById(id);
        if (eventOpt.isEmpty()) {
            showError("Event not found");
            return;
        }

        currentEventId = id;
        Event event = eventOpt.get();
        FXMLLoader loader = new FXMLLoader();
        java.net.URL fxmlUrl = getClass().getResource("/fxml/EditEventForm.fxml");
        if (fxmlUrl == null) {
            throw new IOException("Cannot find /fxml/EditEventForm.fxml");
        }
        System.out.println("Loading FXML: " + fxmlUrl);
        loader.setLocation(fxmlUrl);
        loader.setController(this);
        Scene scene = new Scene(loader.load(), 400, 600);
        java.net.URL cssUrl = getClass().getResource("/css/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Warning: /css/styles.css not found");
        }
        typeField.setItems(typeItems);
        titleField.setText(event.getTitre());
        descField.setText(event.getDescription());
        dateField.setValue(event.getDate());
        timeField.setText(event.getHeure() != null ? event.getHeure().toString() : "");
        locationField.setText(event.getLieu());
        typeField.setValue(event.getType() != null ? event.getType() : "");
        organizerField.setText(event.getOrganisateurNom());
        maxParticipantsField.setText(event.getNombreMaxParticipants() != null ? event.getNombreMaxParticipants().toString() : "");
        statusField.setText(event.getStatus());
        ticketPriceField.setText(event.getTicketPrix());
        registrationEndField.setText(event.getPeriodeInscriptionFin());
        stage.setScene(scene);
    }

    @FXML
    private void handleUpdateEvent() {
        try {
            Optional<Event> eventOpt = eventService.getEventById(currentEventId);
            if (eventOpt.isEmpty()) {
                showError("Event not found");
                return;
            }
            Event event = eventOpt.get();
            event.setTitre(titleField.getText());
            event.setDescription(descField.getText().isEmpty() ? null : descField.getText());
            event.setDate(dateField.getValue());
            event.setHeure(timeField.getText().isEmpty() ? LocalTime.now() : LocalTime.parse(timeField.getText()));
            event.setLieu(locationField.getText().isEmpty() ? null : locationField.getText());
            String typeValue = typeField.getValue();
            event.setType(typeValue.isEmpty() ? null : typeValue);
            event.setOrganisateurNom(organizerField.getText().isEmpty() ? null : organizerField.getText());
            event.setNombreMaxParticipants(maxParticipantsField.getText().isEmpty() ? null : Integer.parseInt(maxParticipantsField.getText()));
            event.setStatus(statusField.getText().isEmpty() ? null : statusField.getText());
            event.setTicketPrix(ticketPriceField.getText().isEmpty() ? null : ticketPriceField.getText());
            event.setPeriodeInscriptionFin(registrationEndField.getText().isEmpty() ? null : registrationEndField.getText());
            eventService.updateEvent(currentEventId, event);
            showEventListView();
        } catch (IllegalArgumentException | java.time.format.DateTimeParseException ex) {
            errorLabel.setText(ex.getMessage());
        } catch (IOException ex) {
            showError("Failed to return to event list");
        }
    }

    private void showEventView(Integer id) throws IOException {
        Optional<Event> eventOpt = eventService.getEventById(id);
        if (eventOpt.isEmpty()) {
            showError("Event not found");
            return;
        }

        Event event = eventOpt.get();
        currentEventId = id;
        FXMLLoader loader = new FXMLLoader();
        java.net.URL fxmlUrl = getClass().getResource("/fxml/EventView.fxml");
        if (fxmlUrl == null) {
            throw new IOException("Cannot find /fxml/EventView.fxml");
        }
        System.out.println("Loading FXML: " + fxmlUrl);
        loader.setLocation(fxmlUrl);
        loader.setController(this);
        Scene scene = new Scene(loader.load(), 400, 600);
        java.net.URL cssUrl = getClass().getResource("/css/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Warning: /css/styles.css not found");
        }
        titleLabelView.setText("Event: " + event.getTitre());
        typeText.setText("Type: " + (event.getType() != null ? event.getType() : "None"));
        descText.setText("Description: " + (event.getDescription() != null ? event.getDescription() : "None"));
        dateText.setText("Date: " + (event.getDate() != null ? event.getDate() : "None"));
        timeText.setText("Time: " + (event.getHeure() != null ? event.getHeure() : "None"));
        locationText.setText("Location: " + (event.getLieu() != null ? event.getLieu() : "None"));
        organizerText.setText("Organizer: " + (event.getOrganisateurNom() != null ? event.getOrganisateurNom() : "None"));
        maxParticipantsText.setText("Max Participants: " + (event.getNombreMaxParticipants() != null ? event.getNombreMaxParticipants() : "None"));
        statusText.setText("Status: " + (event.getStatus() != null ? event.getStatus() : "None"));
        ticketPriceText.setText("Ticket Price: " + (event.getTicketPrix() != null ? event.getTicketPrix() : "None"));
        registrationEndText.setText("Registration Ends: " + (event.getPeriodeInscriptionFin() != null ? event.getPeriodeInscriptionFin() : "None"));
        stage.setScene(scene);
    }

    @FXML
    private void handleManageParticipations() throws IOException {
        participationController.setStage(stage);
        participationController.setBackToEventView(() -> {
            try {
                showEventView(currentEventId);
            } catch (IOException ex) {
                showError("Failed to return to event view");
            }
        });
        stage.setScene(new Scene(participationController.getParticipationListView(currentEventId), 400, 500));
    }

    @FXML
    private void handleCancel() throws IOException {
        showEventListView();
    }

    @FXML
    private void showStatisticsView() throws IOException {
        System.out.println("showStatisticsView called");
        FXMLLoader loader = new FXMLLoader();
        java.net.URL fxmlUrl = getClass().getResource("/fxml/StatisticsView.fxml");
        if (fxmlUrl == null) {
            System.err.println("Cannot find /fxml/StatisticsView.fxml");
            throw new IOException("Cannot find /fxml/StatisticsView.fxml");
        }
        System.out.println("Loading FXML: " + fxmlUrl);
        loader.setLocation(fxmlUrl);
        loader.setController(this);
        try {
            Scene scene = new Scene(loader.load(), 600, 600);
            java.net.URL cssUrl = getClass().getResource("/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("Warning: /css/styles.css not found");
            }
            populatePieChart();
            showEventTypeChart(); // Show pie chart by default
            stage.setScene(scene);
            stage.show();
            System.out.println("Statistics view loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading statistics view: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load statistics view: " + e.getMessage());
        }
    }

    private void populatePieChart() {
        System.out.println("populatePieChart called");
        try {
            Map<String, Long> typeDistribution = eventService.getEventTypeDistribution();
            System.out.println("Type distribution: " + typeDistribution);
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            typeDistribution.forEach((type, count) -> {
                pieChartData.add(new PieChart.Data(type + " (" + count + ")", count));
            });
            if (eventTypePieChart == null) {
                System.err.println("eventTypePieChart is null");
            } else {
                eventTypePieChart.setData(pieChartData);
                eventTypePieChart.setLabelsVisible(true);
                eventTypePieChart.setLegendVisible(true);
                System.out.println("Pie chart data set: " + pieChartData.size() + " entries");
            }
        } catch (Exception e) {
            System.err.println("Error populating pie chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showEventTypeChart() {
        System.out.println("showEventTypeChart called");
        try {
            populatePieChart();
            eventTypePieChart.setVisible(true);
            participantBarChart.setVisible(false);
            statsTitleLabel.setText("Event Type Distribution");
        } catch (Exception e) {
            System.err.println("Error showing event type chart: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to show event type chart");
        }
    }

    @FXML
    private void showParticipantChart() {
        System.out.println("showParticipantChart called");
        try {
            populateParticipantBarChart();
            eventTypePieChart.setVisible(false);
            participantBarChart.setVisible(true);
            statsTitleLabel.setText("Participants by Event Type");
        } catch (Exception e) {
            System.err.println("Error showing participant chart: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to show participant chart");
        }
    }

    private void populateParticipantBarChart() {
        System.out.println("populateParticipantBarChart called");
        try {
            Map<String, Long> participantDistribution = participationService.getParticipantCountByEventType();
            System.out.println("Participant distribution: " + participantDistribution);
            ObservableList<XYChart.Series<String, Number>> barChartData = FXCollections.observableArrayList();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Participants");
            participantDistribution.forEach((type, count) -> {
                series.getData().add(new XYChart.Data<>(type, count));
            });
            barChartData.add(series);
            if (participantBarChart == null) {
                System.err.println("participantBarChart is null");
            } else {
                participantBarChart.getData().clear();
                participantBarChart.setData(barChartData);
                participantXAxis.setCategories(FXCollections.observableArrayList(participantDistribution.keySet()));
                System.out.println("Bar chart data set: " + participantDistribution.size() + " entries");
            }
        } catch (Exception e) {
            System.err.println("Error populating bar chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToEvents() throws IOException {
        System.out.println("handleBackToEvents called");
        showEventListView();
    }

    private void showError(String message) {
        System.out.println("Showing error: " + message);
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.getDialogPane().getStyleClass().add("alert");
        alert.showAndWait();
    }
}