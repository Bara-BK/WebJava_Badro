package tn.badro.Controllers;

import tn.badro.entities.Event;
import tn.badro.services.EventService;
import tn.badro.services.ParticipationService;
import tn.badro.services.SessionManager;
import tn.badro.entities.User;
import tn.badro.entities.Notification;
import tn.badro.entities.NotificationManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
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
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Popup;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Priority;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EventController {
    private final EventService eventService;
    private final ParticipationService participationService;
    private final ParticipationController participationController;
    private Stage stage;
    private MainMenuController mainMenuController;
    private Integer currentEventId;
    private FilteredList<Event> filteredEvents;
    private SortedList<Event> sortedEvents;
    private User currentUser;
    private SessionManager sessionManager = SessionManager.getInstance();
    private Popup notificationPopup;

    // Common data
    public ObservableList<String> typeItems = FXCollections.observableArrayList("", "Entertainment", "Cultural", "Educational", "Sports");
    private ObservableList<String> sortOptions = FXCollections.observableArrayList("Sort by Title", "Sort by Date", "Sort by ID");

    // EventListView.fxml
    @FXML private BorderPane root;
    @FXML private ImageView logoView;
    @FXML private Label titleLabel;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ChoiceBox<String> sortChoiceBox;
    @FXML private Button createButton;
    @FXML private Button statisticsButton;
    @FXML private Button calendarButton;
    @FXML private VBox eventsBox;
    @FXML private TabPane tabPane;
    @FXML private Label userDisplayName;
    @FXML private Button profileButton;
    @FXML private ImageView NotificationIcon;
    @FXML private Label notificationBadge;
    @FXML private StackPane notificationIconContainer;

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
    @FXML private BorderPane view;
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
    
    public void setMainMenuController(MainMenuController mainMenuController) {
        this.mainMenuController = mainMenuController;
    }
    
    public void setUserInfo(User user) {
        this.currentUser = user;
        updateUIForUser();
    }
    
    private void updateUIForUser() {
        if (currentUser != null && userDisplayName != null) {
            userDisplayName.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            userDisplayName.setVisible(true);
            profileButton.setVisible(true);
            NotificationIcon.setVisible(true);
            notificationIconContainer.setVisible(true);
            updateNotificationBadge();
        } else if (userDisplayName != null) {
            userDisplayName.setText("");
            userDisplayName.setVisible(false);
            profileButton.setVisible(false);
            NotificationIcon.setVisible(false);
            notificationIconContainer.setVisible(false);
        }
    }
    
    private void updateNotificationBadge() {
        if (notificationBadge != null) {
            long unread = NotificationManager.getInstance().getUnreadCount();
            notificationBadge.setText(unread > 0 ? String.valueOf(unread) : "");
            notificationBadge.setVisible(unread > 0);
        }
    }

    @FXML
    private void handleNotificationIconClick() {
        if (notificationPopup != null && notificationPopup.isShowing()) {
            notificationPopup.hide();
            return;
        }
        
        notificationPopup = new Popup();
        notificationPopup.setAutoHide(true);

        VBox notificationBox = new VBox();
        notificationBox.setSpacing(0);
        notificationBox.setStyle("-fx-background-color: white; -fx-padding: 0; -fx-border-color: #307D91; -fx-border-width: 2; -fx-background-radius: 12; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, #024F65, 10, 0.5, 0, 2);");
        notificationBox.setPrefWidth(380);
        notificationBox.setMaxWidth(380);
        notificationBox.setMinWidth(320);

        Label title = new Label("Notifications");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 16 16 12 16; -fx-text-fill: #307D91;");
        notificationBox.getChildren().add(title);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(340);
        VBox notifList = new VBox(0);
        notifList.setStyle("-fx-padding: 0 0 8 0;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Notification n : NotificationManager.getInstance().getNotifications()) {
            VBox notifItem = new VBox();
            notifItem.setStyle("-fx-padding: 12 16 10 16; -fx-background-color: " + (n.isRead() ? "#f8f9fa;" : "#eaf6fa;") + "-fx-background-radius: 8; -fx-border-width: 0 0 1 0; -fx-border-color: #f0f0f0;");
            Label msg = new Label(n.getMessage());
            msg.setWrapText(true);
            msg.setStyle("-fx-font-size: 15px; -fx-text-fill: #222; -fx-font-weight: normal;");
            Label time = new Label(n.getTimestamp().format(formatter));
            time.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-padding: 4 0 0 0;");
            notifItem.getChildren().addAll(msg, time);
            notifList.getChildren().add(notifItem);
        }
        scrollPane.setContent(notifList);
        notificationBox.getChildren().add(scrollPane);

        notificationPopup.getContent().clear();
        notificationPopup.getContent().add(notificationBox);

        // Position the popup at the top right, aligned with the notification icon
        Point2D iconScreenPos = NotificationIcon.localToScreen(0, 0);
        notificationPopup.show(NotificationIcon, iconScreenPos.getX() - 320, iconScreenPos.getY() + NotificationIcon.getFitHeight() + 8);
        NotificationManager.getInstance().markAllAsRead();
        updateNotificationBadge();
    }
    
    @FXML
    private void showProfile() {
        try {
            if (currentUser == null) {
                showLoginRequiredAlert();
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/profile.fxml"));
            Parent root = loader.load();
            ProfileController controller = loader.getController();
            controller.setUserInfo(currentUser);
            
            Stage stage = (Stage) profileButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to open profile: " + e.getMessage());
        }
    }
    
    private void showLoginRequiredAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Login Required");
        alert.setHeaderText("Authentication Required");
        alert.setContentText("You must be logged in to access this feature.");
        alert.showAndWait();
        
        // Redirect to login page
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/login.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) (root != null ? root.getScene().getWindow() : view.getScene().getWindow());
            stage.setScene(new Scene(loginRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load login page: " + e.getMessage());
        }
    }

    public void showEventListView() throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/EventListView.fxml"));
            loader.setController(this);
            Scene scene = new Scene(loader.load(), 1200, 800);
            java.net.URL cssUrl = getClass().getResource("/styles/main.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("Warning: /styles/main.css not found");
            }
            stage.setScene(scene);
            stage.setTitle("Event Management - Events");
            stage.show();
            initializeEventList();
            initializeSortChoiceBox();
            populateEventList();
            
            // Check for current user
            if (sessionManager.isLoggedIn()) {
                currentUser = sessionManager.getCurrentUser();
            }
            updateUIForUser();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load event list view: " + e.getMessage());
        }
    }

    @FXML
    private void showCalendarView() throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/eventCalendar.fxml"));
            EventCalendarController calendarController = new EventCalendarController();
            calendarController.setStage(stage);
            loader.setController(calendarController);
            Scene scene = new Scene(loader.load(), 1200, 800);
            java.net.URL cssUrl = getClass().getResource("/styles/main.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("Warning: /styles/main.css not found");
            }
            stage.setScene(scene);
            stage.setTitle("Event Management - Calendar");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load calendar view: " + e.getMessage());
        }
    }

    @FXML
    private void returnToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/mainMenu.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            Stage stage = (Stage) root.getScene().getWindow();
            
            // Get the controller to set user info
            MainMenuController controller = loader.getController();
            if (currentUser != null) {
                controller.setUserInfo(currentUser);
            }
            
            stage.setScene(scene);
        } catch (IOException e) {
            showError("Failed to return to main menu: " + e.getMessage());
        }
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
            noEventsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6C757D;");
            
            VBox emptyState = new VBox(20, noEventsLabel);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(50));
            emptyState.setPrefWidth(eventsBox.getPrefWidth());
            emptyState.setPrefHeight(300);
            
            eventsBox.getChildren().add(emptyState);
        } else {
            // Create a card for each event
            for (Event event : sortedEvents) {
                eventsBox.getChildren().add(createEventCard(event));
            }
        }
    }
    
    /**
     * Creates a modern card for displaying an event
     */
    private VBox createEventCard(Event event) {
        // Container
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 0; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        card.setPrefWidth(Double.MAX_VALUE);
        card.setCursor(javafx.scene.Cursor.HAND);
        
        // Event header - create a colored rectangle instead of loading an image
        String typeColor = "#4B9CD3"; // Default blue
        if (event.getType() != null) {
            switch (event.getType()) {
                case "Entertainment":
                    typeColor = "#9C27B0"; // Purple
                    break;
                case "Cultural":
                    typeColor = "#E91E63"; // Pink
                    break;
                case "Educational":
                    typeColor = "#009688"; // Teal
                    break;
                case "Sports":
                    typeColor = "#FF9800"; // Orange
                    break;
            }
        }
        
        // Top section with type badge and date
        HBox topSection = new HBox(10);
        topSection.setPadding(new Insets(15, 20, 0, 20));
        
        Label typeLabel = new Label(event.getType() != null ? event.getType() : "Event");
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-background-color: " + typeColor + 
                          "; -fx-background-radius: 3px; -fx-padding: 2 10 2 10;");
        
        Label dateLabel = new Label(event.getDate() != null ? event.getDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "No date");
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6C757D;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        topSection.getChildren().addAll(typeLabel, spacer, dateLabel);
        
        // Event info
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(5, 20, 20, 20));
        
        Label nameLabel = new Label(event.getTitre());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #212529;");
        nameLabel.setWrapText(true);
        
        HBox locationBox = new HBox(5);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        
        Label locationIcon = new Label("📍");
        locationIcon.setStyle("-fx-font-size: 14px;");
        
        Label locationLabel = new Label(event.getLieu() != null ? event.getLieu() : "No location");
        locationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6C757D;");
        
        locationBox.getChildren().addAll(locationIcon, locationLabel);
        
        HBox timeBox = new HBox(5);
        timeBox.setAlignment(Pos.CENTER_LEFT);
        
        Label timeIcon = new Label("🕒");
        timeIcon.setStyle("-fx-font-size: 14px;");
        
        Label timeLabel = new Label(event.getHeure() != null ? event.getHeure().toString() : "No time specified");
        timeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6C757D;");
        
        timeBox.getChildren().addAll(timeIcon, timeLabel);
        
        Label descriptionLabel = new Label(event.getDescription() != null ? truncateText(event.getDescription(), 150) : "No description");
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #212529;");
        descriptionLabel.setWrapText(true);
        
        Separator separator = new Separator();
        separator.setStyle("-fx-padding: 5 0;");
        
        // Button row
        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);
        
        Button viewButton = new Button("View Details");
        viewButton.setStyle("-fx-background-color: #4B9CD3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 5;");
        viewButton.setOnAction(e -> {
            try {
                showEventView(event.getEventId());
            } catch (IOException ex) {
                showError("Failed to load event view");
            }
        });
        
                Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #5CB85C; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 5;");
        editButton.setOnAction(e -> {
            try {
                showEditEventForm(event.getEventId());
            } catch (IOException ex) {
                showError("Failed to load edit form");
            }
        });
        
                Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 5;");
                deleteButton.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + event.getTitre() + "?", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(type -> {
                        if (type == ButtonType.YES) {
                    eventService.deleteEvent(event.getEventId());
                    initializeEventList();
                    populateEventList();
                        }
                    });
                });
        
        buttonRow.getChildren().addAll(deleteButton, editButton, viewButton);
        
        infoBox.getChildren().addAll(nameLabel, locationBox, timeBox, descriptionLabel, separator, buttonRow);
        
        // Add all components to card
        card.getChildren().addAll(topSection, infoBox);
        
        return card;
    }

    @FXML
    private void handleCreateEvent() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        java.net.URL fxmlUrl = getClass().getResource("/Desktop/CreateEventForm.fxml");
        if (fxmlUrl == null) {
            throw new IOException("Cannot find /Desktop/CreateEventForm.fxml");
        }
        System.out.println("Loading FXML: " + fxmlUrl);
        loader.setLocation(fxmlUrl);
        loader.setController(this);
        Scene scene = new Scene(loader.load(), 400, 600);
        java.net.URL cssUrl = getClass().getResource("/styles/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Warning: /styles/styles.css not found");
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
        java.net.URL fxmlUrl = getClass().getResource("/Desktop/EditEventForm.fxml");
        if (fxmlUrl == null) {
            throw new IOException("Cannot find /Desktop/EditEventForm.fxml");
        }
        System.out.println("Loading FXML: " + fxmlUrl);
        loader.setLocation(fxmlUrl);
        loader.setController(this);
        Scene scene = new Scene(loader.load(), 400, 600);
        java.net.URL cssUrl = getClass().getResource("/styles/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Warning: /styles/styles.css not found");
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
        java.net.URL fxmlUrl = getClass().getResource("/Desktop/EventView.fxml");
        if (fxmlUrl == null) {
            throw new IOException("Cannot find /Desktop/EventView.fxml");
        }
        loader.setLocation(fxmlUrl);
        loader.setController(this);
        Scene scene = new Scene(loader.load(), 1200, 800);
        java.net.URL cssUrl = getClass().getResource("/styles/main.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Warning: /styles/main.css not found");
        }
        
        // Set event title
        titleLabelView.setText(event.getTitre());
        
        // Format type, date and time for header card
        String typeValue = event.getType() != null ? event.getType() : "General Event";
        typeText.setText("Type: " + typeValue);
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        String dateValue = event.getDate() != null ? event.getDate().format(dateFormatter) : "No date specified";
        dateText.setText("Date: " + dateValue);
        
        String timeValue = event.getHeure() != null ? event.getHeure().toString() : "No time specified";
        timeText.setText("Time: " + timeValue);
        
        // Format description
        String descValue = event.getDescription() != null ? event.getDescription() : "No description available";
        descText.setText(descValue);
        
        // Format location
        String locationValue = event.getLieu() != null ? event.getLieu() : "No location specified";
        locationText.setText(locationValue);
        
        // Format organizer and max participants
        String organizerValue = event.getOrganisateurNom() != null ? event.getOrganisateurNom() : "Unknown";
        organizerText.setText("Organizer: " + organizerValue);
        
        String maxParticipantsValue = event.getNombreMaxParticipants() != null ? event.getNombreMaxParticipants().toString() : "Unlimited";
        maxParticipantsText.setText("Maximum Participants: " + maxParticipantsValue);
        
        // Format status, ticket price and registration end
        String statusValue = event.getStatus() != null ? event.getStatus() : "Open";
        statusText.setText("Status: " + statusValue);
        
        String ticketPriceValue = event.getTicketPrix() != null ? event.getTicketPrix() : "Free";
        ticketPriceText.setText("Ticket Price: " + ticketPriceValue);
        
        String registrationEndValue = event.getPeriodeInscriptionFin() != null ? event.getPeriodeInscriptionFin() : "Not specified";
        registrationEndText.setText("Registration Ends: " + registrationEndValue);
        
        stage.setScene(scene);
        stage.setTitle("Event Details: " + event.getTitre());
        
        // Update user session info
        updateUIForUser();
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
            return null; // Return null for Void
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
        java.net.URL fxmlUrl = getClass().getResource("/Desktop/StatisticsView.fxml");
        if (fxmlUrl == null) {
            System.err.println("Cannot find /Desktop/StatisticsView.fxml");
            throw new IOException("Cannot find /Desktop/StatisticsView.fxml");
        }
        System.out.println("Loading FXML: " + fxmlUrl);
        loader.setLocation(fxmlUrl);
        loader.setController(this);
        try {
            Scene scene = new Scene(loader.load(), 600, 600);
            java.net.URL cssUrl = getClass().getResource("/styles/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("Warning: /styles/styles.css not found");
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

    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}