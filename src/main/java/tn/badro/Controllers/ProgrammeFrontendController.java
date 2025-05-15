package tn.badro.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Point2D;
import javafx.stage.Stage;
import tn.badro.entities.Programme;
import tn.badro.entities.User;
import tn.badro.entities.Notification;
import tn.badro.entities.NotificationManager;
import tn.badro.services.ProgrammeService;
import tn.badro.services.UniversityService;
import tn.badro.services.SessionManager;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

public class ProgrammeFrontendController {
    
    private final ProgrammeService programmeService = new ProgrammeService();
    private final UniversityService universityService = new UniversityService();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private User currentUser;
    private Programme selectedProgram;
    
    @FXML private BorderPane mainBorderPane;
    @FXML private TabPane tabPane;
    @FXML private TilePane programsContainer;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Tab programDetailTab;
    @FXML private VBox programDetailContainer;
    @FXML private Label programName;
    @FXML private Label universityName;
    @FXML private Label programDuration;
    @FXML private Label programCost;
    @FXML private Label programLanguage;
    @FXML private Label programLevel;
    @FXML private Label programDescription;
    @FXML private ImageView NotificationIcon;
    @FXML private Label notificationBadge;
    
    private Popup notificationPopup;

    @FXML
    public void initialize() {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            showLoginRequiredAlert();
            return;
        }
        
        // Get current user from SessionManager
        currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            showLoginRequiredAlert();
            return;
        }
        
        // Load programs and display as cards
        loadPrograms();
        
        // Set up search handler
        searchField.setOnAction(e -> handleSearch());
        
        // Update notification badge
        updateNotificationBadge();
        NotificationManager.getInstance().setOnNotificationAddedCallback(this::updateNotificationBadge);
    }
    
    /**
     * Loads programs from the database and displays them as cards
     */
    private void loadPrograms() {
        List<Programme> programs = programmeService.getAllProgrammes();
        programsContainer.getChildren().clear();
        
        if (programs.isEmpty()) {
            // Show empty state
            Label emptyLabel = new Label("No programs found");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6C757D;");
            
            VBox emptyState = new VBox(20, emptyLabel);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(50));
            emptyState.setPrefWidth(programsContainer.getPrefWidth());
            emptyState.setPrefHeight(300);
            
            programsContainer.getChildren().add(emptyState);
        } else {
            // Create a card for each program
            for (Programme program : programs) {
                programsContainer.getChildren().add(createProgramCard(program));
            }
        }
    }
    
    /**
     * Creates a card for displaying a program
     */
    private VBox createProgramCard(Programme program) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        card.setPrefWidth(320);
        card.setUserData(program);
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(e -> showProgramDetails(program));
        
        // Program header
        Label nameLabel = new Label(program.getName());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4B9CD3;");
        
        // Description
        Label descriptionLabel = new Label(truncateText(program.getDescription(), 100));
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #212529;");
        descriptionLabel.setWrapText(true);
        
        // View details button
        Button viewDetailsBtn = new Button("View Details");
        viewDetailsBtn.setStyle("-fx-background-color: #4B9CD3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 5; -fx-cursor: hand;");
        viewDetailsBtn.setMaxWidth(Double.MAX_VALUE);
        viewDetailsBtn.setOnAction(e -> showProgramDetails(program));
        
        card.getChildren().addAll(nameLabel, new Separator(), descriptionLabel, viewDetailsBtn);
        
        return card;
    }
    
    /**
     * Shows details of a selected program in the program details tab
     */
    private void showProgramDetails(Programme program) {
        selectedProgram = program;
        programName.setText(program.getName());
        programDescription.setText(program.getDescription());
        // Switch to the details tab
        tabPane.getSelectionModel().select(programDetailTab);
    }
    
    /**
     * Handles search functionality
     */
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadPrograms();
            return;
        }
        List<Programme> programs = programmeService.getAllProgrammes();
        List<Programme> filteredPrograms = programs.stream()
            .filter(p -> p.getName().toLowerCase().contains(searchTerm) || 
                        p.getDescription().toLowerCase().contains(searchTerm))
            .collect(Collectors.toList());
        programsContainer.getChildren().clear();
        if (filteredPrograms.isEmpty()) {
            Label noResultsLabel = new Label("No results found for \"" + searchTerm + "\"");
            noResultsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6C757D;");
            Button resetBtn = new Button("Reset Search");
            resetBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
            resetBtn.setOnAction(e -> {
                searchField.clear();
                loadPrograms();
            });
            VBox noResultsBox = new VBox(15, noResultsLabel, resetBtn);
            noResultsBox.setAlignment(Pos.CENTER);
            noResultsBox.setPadding(new Insets(50));
            programsContainer.getChildren().add(noResultsBox);
        } else {
            for (Programme program : filteredPrograms) {
                programsContainer.getChildren().add(createProgramCard(program));
            }
        }
    }
    
    /**
     * Shows application form for a program
     */
    @FXML
    private void showApplicationForm() {
        if (selectedProgram == null) return;
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Apply for Program");
        alert.setHeaderText("Application Process");
        alert.setContentText("The application process for " + selectedProgram.getName() + " will be available soon!");
        alert.showAndWait();
    }
    
    /**
     * Truncates text to a maximum length and adds ellipsis
     */
    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
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
            
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to return to main menu: " + e.getMessage());
        }
    }
    
    @FXML
    private void showUniversities() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/universityFrontend.fxml"));
            Parent root = loader.load();
            UniversityFrontendController controller = loader.getController();
            if (currentUser != null) {
                controller.setUserInfo(currentUser);
            }
            
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to navigate to universities: " + e.getMessage());
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
     * Shows login required alert
     */
    private void showLoginRequiredAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Login Required");
        alert.setHeaderText("Authentication Required");
        alert.setContentText("You must be logged in to access this page.");
        alert.showAndWait();
        
        // Redirect to login page
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/login.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) (mainBorderPane != null ? mainBorderPane.getScene().getWindow() : new Stage());
            stage.setScene(new Scene(loginRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Generic alert method
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Set user info
     */
    public void setUserInfo(User user) {
        this.currentUser = user;
    }
    
    /**
     * Update notification badge
     */
    private void updateNotificationBadge() {
        long unread = NotificationManager.getInstance().getUnreadCount();
        notificationBadge.setText(unread > 0 ? String.valueOf(unread) : "");
        notificationBadge.setVisible(unread > 0);
    }
} 