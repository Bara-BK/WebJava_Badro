package tn.badro.Controllers;

import tn.badro.entities.Event;
import tn.badro.entities.User;
import tn.badro.entities.Notification;
import tn.badro.entities.NotificationManager;
import tn.badro.services.EventService;
import tn.badro.services.ParticipationService;
import tn.badro.services.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.geometry.Point2D;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class EventCalendarController implements Initializable {
    @FXML private BorderPane mainBorderPane;
    @FXML private VBox root;
    @FXML private GridPane calendarGrid;
    @FXML private ComboBox<String> viewComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField titleField;
    @FXML private TextField locationField;
    @FXML private ChoiceBox<String> typeField;
    @FXML private Button createEventButton;
    @FXML private Button backButton;
    @FXML private Label userDisplayName;
    @FXML private ImageView NotificationIcon;
    @FXML private Label notificationBadge;
    @FXML private StackPane notificationIconContainer;
    @FXML private Button profileButton;
    
    private User currentUser;
    private EventService eventService;
    private ParticipationService participationService;
    private Stage stage;
    private SessionManager sessionManager = SessionManager.getInstance();
    private Popup notificationPopup;
    
    public EventCalendarController() {
        this.eventService = new EventService();
        this.participationService = new ParticipationService();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupViewOptions();
        setupTypeOptions();
        loadEvents();
        
        // Check for current user
        if (sessionManager.isLoggedIn()) {
            currentUser = sessionManager.getCurrentUser();
            updateUIForUser();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setUserInfo(User user) {
        this.currentUser = user;
        updateUIForUser();
    }
    
    private void updateUIForUser() {
        if (currentUser != null && userDisplayName != null) {
            userDisplayName.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            userDisplayName.setVisible(true);
            if (profileButton != null) profileButton.setVisible(true);
            if (NotificationIcon != null) NotificationIcon.setVisible(true);
            if (notificationIconContainer != null) notificationIconContainer.setVisible(true);
            updateNotificationBadge();
            
            // Show admin-only controls for admin users only
            boolean isAdmin = "admin".equals(currentUser.getRoles());
            if (createEventButton != null) createEventButton.setVisible(isAdmin);
        } else if (userDisplayName != null) {
            userDisplayName.setText("");
            userDisplayName.setVisible(false);
            if (profileButton != null) profileButton.setVisible(false);
            if (NotificationIcon != null) NotificationIcon.setVisible(false);
            if (notificationIconContainer != null) notificationIconContainer.setVisible(false);
            
            // Hide admin-only controls for non-logged in users
            if (createEventButton != null) createEventButton.setVisible(false);
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
            
            Scene scene = new Scene(root);
            getStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to open profile: " + e.getMessage());
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
            getStage().setScene(new Scene(loginRoot));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load login page: " + e.getMessage());
        }
    }
    
    private void setupViewOptions() {
        if (viewComboBox != null) {
            ObservableList<String> viewOptions = FXCollections.observableArrayList(
                    "Day", "Week", "Month"
            );
            viewComboBox.setItems(viewOptions);
            viewComboBox.setValue("Month");
        }
    }
    
    private void setupTypeOptions() {
        if (typeField != null) {
            ObservableList<String> typeOptions = FXCollections.observableArrayList(
                    "", "Entertainment", "Cultural", "Educational", "Sports"
            );
            typeField.setItems(typeOptions);
            typeField.setValue("");
        }
    }
    
    private void loadEvents() {
        try {
            List<Event> events = eventService.getAllEvents();
            displayEvents(events);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load events: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void displayEvents(List<Event> events) {
        if (calendarGrid == null) {
            System.err.println("Calendar grid is null");
            return;
        }
        
        // Clear the calendar grid
        calendarGrid.getChildren().clear();
        
        // Add day headers (Sun-Sat)
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5; -fx-alignment: center;");
            calendarGrid.add(dayLabel, i, 0);
        }
        
        // Current month calendar
        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        int monthStartDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // 0 = Sunday, 6 = Saturday
        int daysInMonth = firstOfMonth.lengthOfMonth();
        
        // Create calendar cells
        int row = 1;
        int col = monthStartDayOfWeek;
        
        // Add event indicators to the calendar
        for (int day = 1; day <= daysInMonth; day++) {
            VBox dayCell = new VBox(5);
            dayCell.setStyle("-fx-border-color: #e0e0e0; -fx-padding: 5; -fx-background-color: white;");
            dayCell.setPrefHeight(80);
            
            // Day number
            Label dayLabel = new Label(String.valueOf(day));
            
            // Highlight today
            if (day == today.getDayOfMonth()) {
                dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4B9CD3;");
                dayCell.setStyle("-fx-border-color: #4B9CD3; -fx-border-width: 2; -fx-padding: 5; -fx-background-color: #f0f8ff;");
            }
            
            dayCell.getChildren().add(dayLabel);
            
            // Add events for this day
            LocalDate currentDate = LocalDate.of(today.getYear(), today.getMonth(), day);
            for (Event event : events) {
                if (event.getDate() != null && event.getDate().equals(currentDate)) {
                    Label eventLabel = new Label(event.getTitre());
                    eventLabel.setWrapText(true);
                    eventLabel.setMaxWidth(Double.MAX_VALUE);
                    
                    // Style based on event type
                    String bgColor = "#4B9CD3"; // Default blue
                    if (event.getType() != null) {
                        switch (event.getType()) {
                            case "Entertainment":
                                bgColor = "#9C27B0"; // Purple
                                break;
                            case "Cultural":
                                bgColor = "#E91E63"; // Pink
                                break;
                            case "Educational":
                                bgColor = "#009688"; // Teal
                                break;
                            case "Sports":
                                bgColor = "#FF9800"; // Orange
                                break;
                        }
                    }
                    
                    eventLabel.setStyle(
                        "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 2 5;" +
                        "-fx-background-radius: 3;" +
                        "-fx-font-size: 10px;"
                    );
                    
                    // Make it clickable
                    eventLabel.setOnMouseClicked(e -> openEventDetails(event.getEventId()));
                    dayCell.getChildren().add(eventLabel);
                }
            }
            
            calendarGrid.add(dayCell, col, row);
            
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }
    
    private void openEventDetails(Integer eventId) {
        try {
            // Open event details in EventController
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/EventView.fxml"));
            EventController controller = new EventController(eventService, participationService);
            controller.setStage(getStage());
            if (currentUser != null) {
                controller.setUserInfo(currentUser);
            }
            loader.setController(controller);
            
            // Load the scene
            Scene scene = new Scene(loader.load(), 1200, 800);
            controller.showEventView(eventId);
            getStage().setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open event details: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateEvent() {
        try {
            EventController controller = new EventController(eventService, participationService);
            controller.setStage(getStage());
            if (currentUser != null) {
                controller.setUserInfo(currentUser);
            }
            
            // Call the create event method directly
            controller.handleCreateEvent();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to open create event form: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleViewEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/EventListView.fxml"));
            EventController controller = new EventController(eventService, participationService);
            controller.setStage(getStage());
            if (currentUser != null) {
                controller.setUserInfo(currentUser);
            }
            loader.setController(controller);
            
            Scene scene = new Scene(loader.load(), 1200, 800);
            getStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to open events list: " + e.getMessage());
        }
    }
    
    @FXML
    private void returnToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/mainMenu.fxml"));
            Parent root = loader.load();
            MainMenuController controller = loader.getController();
            if (currentUser != null) {
                controller.setUserInfo(currentUser);
            }
            
            Scene scene = new Scene(root);
            getStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to return to main menu: " + e.getMessage());
        }
    }
    
    private Stage getStage() {
        if (stage != null) {
            return stage;
        }
        
        if (mainBorderPane != null && mainBorderPane.getScene() != null) {
            return (Stage) mainBorderPane.getScene().getWindow();
        }
        
        if (backButton != null && backButton.getScene() != null) {
            return (Stage) backButton.getScene().getWindow();
        }
        
        if (root != null && root.getScene() != null) {
            return (Stage) root.getScene().getWindow();
        }
        
        throw new IllegalStateException("Cannot access Stage - JavaFX components not initialized");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}