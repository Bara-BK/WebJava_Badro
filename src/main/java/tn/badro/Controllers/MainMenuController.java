package tn.badro.Controllers;

import tn.badro.entities.User;
import tn.badro.services.SessionManager;
import tn.badro.entities.Notification;
import tn.badro.entities.NotificationManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.stage.Popup;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import tn.badro.Controllers.ProfileController;
import tn.badro.Controllers.UniversityController;
import javafx.scene.control.Alert;
import tn.badro.services.EventService;
import tn.badro.services.ParticipationService;
import tn.badro.Controllers.EventController;

public class MainMenuController {
    @FXML private Label userDisplayName;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button logoutButton;
    @FXML private Button profileButton; // Added profile button
    @FXML private HBox userInfoSection;
    @FXML private BorderPane mainMenuBorderPane;
    @FXML private ImageView NotificationIcon;
    @FXML private Label notificationBadge;
    @FXML private StackPane notificationIconContainer;

    private User currentUser;
    private Popup notificationPopup;

    @FXML
    public void initialize() {
        updateUIForUser();
    }

    public void setUserInfo(User user) {
        this.currentUser = user;
        updateUIForUser();
    }

    private void updateUIForUser() {
        if (currentUser != null) {
            userDisplayName.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            loginButton.setVisible(false);
            registerButton.setVisible(false);
            logoutButton.setVisible(true);
            profileButton.setVisible(true); // Show profile button when logged in
            NotificationIcon.setVisible(true); // Show notification icon when logged in
            notificationIconContainer.setVisible(true); // Show notification container when logged in
            updateNotificationBadge(); // Update notification badge count
            // Add listener for notification changes
            NotificationManager.getInstance().getNotifications().addListener(
                (javafx.collections.ListChangeListener<Notification>) c -> updateNotificationBadge());
        } else {
            userDisplayName.setText("");
            loginButton.setVisible(true);
            registerButton.setVisible(true);
            logoutButton.setVisible(false);
            profileButton.setVisible(false); // Hide profile button when not logged in
            NotificationIcon.setVisible(false); // Hide notification icon when not logged in
            notificationIconContainer.setVisible(false); // Hide notification container when not logged in
            notificationBadge.setVisible(false); // Hide notification badge when not logged in
        }
    }

    @FXML
    private void handleLogout() {
        try {
            if (currentUser != null) {
                SessionManager.getInstance().invalidateSession(currentUser.getEmail());
                currentUser = null;
                updateUIForUser();
            }
            
            showLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/registration.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/profile.fxml"));
            Parent root = loader.load();
            ProfileController controller = loader.getController();
            controller.setUserInfo(currentUser);
            
            Stage stage = (Stage) profileButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showUniversities(ActionEvent event) throws Exception {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        UniversityController universityController = new UniversityController();
        universityController.setStage(stage);
        if (currentUser != null) {
            universityController.setUserInfo(currentUser);
        }
        stage.setScene(new Scene(universityController.getUniversityListView(), 800, 600));
    }

    @FXML
    private void showPreferencesMatching() {
        try {
            // Check if user is logged in
            if (!SessionManager.getInstance().isLoggedIn()) {
                showLoginRequiredAlert();
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/preference.fxml"));
            Parent preferencesRoot = loader.load();
            Stage stage = (Stage) mainMenuBorderPane.getScene().getWindow();
            Scene scene = new Scene(preferencesRoot);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showUniversitiesPage() {
        try {
            // Check if user is logged in
            if (!SessionManager.getInstance().isLoggedIn()) {
                showLoginRequiredAlert();
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/universityFrontend.fxml"));
            Parent universitiesRoot = loader.load();
            UniversityFrontendController controller = loader.getController();
            controller.setUserInfo(currentUser);
            
            Stage stage = (Stage) mainMenuBorderPane.getScene().getWindow();
            Scene scene = new Scene(universitiesRoot);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to navigate to universities page: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void showMyApplicationsPage() {
        try {
            // Check if user is logged in
            if (!SessionManager.getInstance().isLoggedIn()) {
                showLoginRequiredAlert();
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/userApplications.fxml"));
            Parent applicationsRoot = loader.load();
            UserApplicationsController controller = loader.getController();
            controller.setUserInfo(currentUser);
            
            Stage stage = (Stage) mainMenuBorderPane.getScene().getWindow();
            Scene scene = new Scene(applicationsRoot);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to navigate to applications page: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void showEventsPage() {
        try {
            // Check if user is logged in
            if (!SessionManager.getInstance().isLoggedIn()) {
                showLoginRequiredAlert();
                return;
            }
            
            EventService eventService = new EventService();
            ParticipationService participationService = new ParticipationService();
            EventController eventController = new EventController(eventService, participationService);
            
            Stage stage = (Stage) mainMenuBorderPane.getScene().getWindow();
            eventController.setStage(stage);
            
            // Set the current user information
            if (currentUser != null) {
                eventController.setUserInfo(currentUser);
            }
            
            // Show the event list view
            eventController.showEventListView();
            
        } catch (IOException e) {
            e.printStackTrace();
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to navigate to events page: " + e.getMessage());
            alert.showAndWait();
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
    
    /**
     * Updates the notification badge count
     */
    private void updateNotificationBadge() {
        long unread = NotificationManager.getInstance().getUnreadCount();
        notificationBadge.setText(unread > 0 ? String.valueOf(unread) : "");
        notificationBadge.setVisible(unread > 0);
    }
    
    /**
     * Show alert that login is required
     */
    private void showLoginRequiredAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Login Required");
        alert.setHeaderText("Authentication Required");
        alert.setContentText("You must be logged in to access the preferences page.");
        alert.showAndWait();
        
        // Redirect to login page
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/login.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) mainMenuBorderPane.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // Other methods for managing events and participants
}
