package tn.badro.Controllers;

import tn.badro.entities.User;
import tn.badro.entities.Notification;
import tn.badro.entities.NotificationManager;
import tn.badro.services.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.Popup;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.Parent;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.net.URL;

public class DashboardController {
    private Stage stage;
    private UniversityController universityController;
    private ProgrammeController programmeController;
    private GuideController guideController;
    private UserController userController;
    private PreferenceController preferenceController;
    private User currentUser;
    
    @FXML
    private BorderPane mainLayout;
    
    @FXML
    private VBox sidebar;
    
    @FXML
    private HBox header;
    
    @FXML
    private HBox userSection;
    
    @FXML
    private Label userDisplayName;
    
    @FXML
    private VBox welcomeContent;
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label instructionLabel;
    
    @FXML
    private Button universitiesBtn;
    
    @FXML
    private Button programmesBtn;
    
    @FXML
    private Button guidesBtn;
    
    @FXML
    private Button usersBtn;

    @FXML
    private Button preferencesBtn;

    @FXML
    private Button applicationsBtn;

    @FXML
    private Button returnToMainBtn;
    
    @FXML
    private Button experiencesBtn;
    
    @FXML
    private ImageView NotificationIcon;
    
    @FXML
    private Label notificationBadge;
    
    private Popup notificationPopup;

    // Default no-arg constructor for FXML
    public DashboardController() {
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        initializeControllers();
    }

    private void initializeControllers() {
        this.universityController = new UniversityController();
        this.programmeController = new ProgrammeController();
        this.guideController = new GuideController();
        this.userController = new UserController();
        this.preferenceController = new PreferenceController();
        
        if (stage != null) {
            universityController.setStage(stage);
            programmeController.setStage(stage);
            guideController.setStage(stage);
            userController.setStage(stage);
        }
    }

    @FXML
    public void initialize() {
        try {
            // Set up button actions after FXML is loaded
            if (universitiesBtn != null) {
                universitiesBtn.setOnAction(e -> showUniversitiesContent());
            }
            if (programmesBtn != null) {
                programmesBtn.setOnAction(e -> showProgrammesContent());
            }
            if (guidesBtn != null) {
                guidesBtn.setOnAction(e -> showGuidesContent());
            }
            if (usersBtn != null) {
                usersBtn.setOnAction(e -> showUsersContent());
            }
            if (preferencesBtn != null) {
                preferencesBtn.setOnAction(e -> showPreferencesContent());
            }
            if (applicationsBtn != null) {
                applicationsBtn.setOnAction(e -> showApplicationsContent());
            }
            if (experiencesBtn != null) {
                experiencesBtn.setOnAction(e -> showExperiencesContent());
            }
            if (returnToMainBtn != null) {
                returnToMainBtn.setOnAction(e -> returnToMainMenu());
            }
            
            // Update notification badge
            updateNotificationBadge();
            
            // Add listener for notification changes
            NotificationManager.getInstance().setOnNotificationAddedCallback(this::updateNotificationBadge);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to initialize dashboard: " + e.getMessage());
        }
    }

    public void setupStage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/dashboard.fxml"));
            loader.setController(this);
            mainLayout = loader.load();
            
            Scene scene = new Scene(mainLayout, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("BADRO - Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to setup dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void showUniversitiesContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/universities.fxml"));
            VBox universitiesView = loader.load();
            mainLayout.setCenter(universitiesView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load universities content: " + e.getMessage());
        }
    }

    @FXML
    private void showProgrammesContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/programmes.fxml"));
            VBox programmesView = loader.load();
            mainLayout.setCenter(programmesView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load programmes content: " + e.getMessage());
        }
    }

    @FXML
    private void showGuidesContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/guides.fxml"));
            VBox guidesView = loader.load();
            mainLayout.setCenter(guidesView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load guides content: " + e.getMessage());
        }
    }

    @FXML
    private void showUsersContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/User.fxml"));
            Node userContent = loader.load();
            mainLayout.setCenter(userContent);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users content: " + e.getMessage());
        }
    }

    @FXML
    private void showPreferencesContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/preferencesAdmin.fxml"));
            Node preferencesContent = loader.load();
            mainLayout.setCenter(preferencesContent);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load preferences content: " + e.getMessage());
        }
    }

    @FXML
    private void showApplicationsContent() {
        try {
            // First check if resource exists
            URL resource = getClass().getResource("/Desktop/programApplications.fxml");
            if (resource == null) {
                // If resource doesn't exist, show a placeholder message
                VBox placeholder = new VBox();
                placeholder.setAlignment(Pos.CENTER);
                placeholder.setSpacing(20);
                
                Label titleLabel = new Label("Program Applications");
                titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4B9CD3;");
                
                Label infoLabel = new Label("This module is currently under development.");
                infoLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d;");
                
                placeholder.getChildren().addAll(titleLabel, infoLabel);
                mainLayout.setCenter(placeholder);
                return;
            }
            
            // If resource exists, load it normally
            FXMLLoader loader = new FXMLLoader(resource);
            VBox applicationsView = loader.load();
            mainLayout.setCenter(applicationsView);
        } catch (IOException e) {
            e.printStackTrace();
            
            // Create a placeholder on error
            VBox errorPlaceholder = new VBox();
            errorPlaceholder.setAlignment(Pos.CENTER);
            errorPlaceholder.setSpacing(20);
            
            Label errorTitle = new Label("Could Not Load Applications");
            errorTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
            
            Label errorMsg = new Label("An error occurred while loading the applications module.\nPlease try again later.");
            errorMsg.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d; -fx-text-alignment: center;");
            errorMsg.setWrapText(true);
            
            errorPlaceholder.getChildren().addAll(errorTitle, errorMsg);
            mainLayout.setCenter(errorPlaceholder);
        }
    }

    @FXML
    private void showExperiencesContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/experienceAdmin.fxml"));
            Parent experiencesView = loader.load();
            mainLayout.setCenter(experiencesView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load experiences content: " + e.getMessage());
        }
    }

    @FXML
    public void returnToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/mainMenu.fxml"));
            Parent root = loader.load();
            MainMenuController controller = loader.getController();
            controller.setUserInfo(currentUser);
            
            Stage stage = (Stage) mainLayout.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to return to main menu: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Invalidate the session
            if (currentUser != null) {
                SessionManager.getInstance().invalidateSession(currentUser.getEmail());
            }
            
            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) mainLayout.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout: " + e.getMessage());
        }
    }

    @FXML
    private void showProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/profile.fxml"));
            Parent root = loader.load();
            ProfileController controller = loader.getController();
            controller.setUserInfo(currentUser);
            
            Stage stage = (Stage) mainLayout.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load profile: " + e.getMessage());
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setUserInfo(User user) {
        this.currentUser = user;
        if (userDisplayName != null) {
            userDisplayName.setText(user.getNom() + " " + user.getPrenom());
        }
    }
} 