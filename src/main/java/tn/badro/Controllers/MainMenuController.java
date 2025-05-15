package tn.badro.Controllers;

import tn.badro.entities.User;
import tn.badro.entities.Notification;
import tn.badro.entities.NotificationManager;
import tn.badro.services.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
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
import tn.badro.Controllers.ExperienceController;

public class MainMenuController {
    @FXML private Label userDisplayName;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button logoutButton;
    @FXML private Button profileButton;
    @FXML private Button dashboardButton;
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
            profileButton.setVisible(true);
            
            if (currentUser.getRoles().equals("admin")) {
                dashboardButton.setVisible(true);
            } else {
                dashboardButton.setVisible(false);
            }
            
            NotificationIcon.setVisible(true);
            notificationIconContainer.setVisible(true);
            updateNotificationBadge();
            NotificationManager.getInstance().setOnNotificationAddedCallback(this::updateNotificationBadge);
        } else {
            userDisplayName.setText("");
            loginButton.setVisible(true);
            registerButton.setVisible(true);
            logoutButton.setVisible(false);
            profileButton.setVisible(false);
            dashboardButton.setVisible(false);
            NotificationIcon.setVisible(false);
            notificationIconContainer.setVisible(false);
            notificationBadge.setVisible(false);
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
    private void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/dashboard.fxml"));
            Parent root = loader.load();
            
            DashboardController controller = loader.getController();
            Stage stage = (Stage) mainMenuBorderPane.getScene().getWindow();
            controller.setStage(stage);
            controller.setUserInfo(currentUser);
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to navigate to dashboard: " + e.getMessage());
            alert.showAndWait();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/universityFrontend.fxml"));
            Parent universitiesRoot = loader.load();
            UniversityFrontendController controller = loader.getController();
            controller.setUserInfo(currentUser);
            
            Stage stage = (Stage) mainMenuBorderPane.getScene().getWindow();
            Scene scene = new Scene(universitiesRoot);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/userApplications.fxml"));
            Parent applicationsRoot = loader.load();
            UserApplicationsController controller = loader.getController();
            controller.setUserInfo(currentUser);
            
            Stage stage = (Stage) mainMenuBorderPane.getScene().getWindow();
            Scene scene = new Scene(applicationsRoot);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
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
            // Create the services needed for EventController
            EventService eventService = new EventService();
            ParticipationService participationService = new ParticipationService();
            
            // Create the controller with services
            EventController controller = new EventController(eventService, participationService);
            
            // Set up the controller with stage and user info
            Stage stage = (Stage) mainMenuBorderPane.getScene().getWindow();
            controller.setStage(stage);
            if (currentUser != null) {
                controller.setUserInfo(currentUser);
            }
            
            // Use controller's method to load and display events
            // This ensures events are properly loaded and displayed
            controller.showEventListView();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to events page: " + e.getMessage());
        }
    }
    
    @FXML
    private void showExperiencesPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/badro/experience.fxml"));
            Parent experiencesRoot = loader.load();
            
            Stage stage = (Stage) mainMenuBorderPane.getScene().getWindow();
            Scene scene = new Scene(experiencesRoot);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to navigate to experiences page: " + e.getMessage());
        }
    }
    
    @FXML
    private void showNotifications() {
        if (currentUser == null) return;
        
        if (notificationPopup != null && notificationPopup.isShowing()) {
            notificationPopup.hide();
            return;
        }
        
        try {
            // Create popup content
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

            notificationPopup.getContent().add(notificationBox);
            
            // Position popup below notification icon
            Stage stage = (Stage) mainMenuBorderPane.getScene().getWindow();
            notificationPopup.show(stage, 
                NotificationIcon.localToScreen(NotificationIcon.getBoundsInLocal()).getMinX(), 
                NotificationIcon.localToScreen(NotificationIcon.getBoundsInLocal()).getMaxY());
            
            // Mark notifications as read when popup is closed
            notificationPopup.setOnHidden(e -> {
                NotificationManager.getInstance().markAllAsRead();
                updateNotificationBadge();
            });
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleNotificationIconClick() {
        showNotifications();
    }
    
    private void updateNotificationBadge() {
        if (currentUser != null) {
            int unreadCount = NotificationManager.getInstance().getUnreadCount();
            notificationBadge.setText(String.valueOf(unreadCount));
            notificationBadge.setVisible(unreadCount > 0);
        } else {
            notificationBadge.setVisible(false);
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
