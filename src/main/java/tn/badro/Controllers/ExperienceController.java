package tn.badro.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import tn.badro.entities.Experience;
import tn.badro.entities.User;
import tn.badro.entities.Notification;
import tn.badro.entities.NotificationManager;
import tn.badro.services.ExperienceService;
import tn.badro.services.LikeService;
import tn.badro.services.SessionManager;
import tn.badro.services.UserService;
import javafx.scene.shape.Circle;
import javafx.scene.SnapshotParameters;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.Cursor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ExperienceController implements Initializable {
    @FXML private BorderPane mainBorderPane;
    @FXML private VBox experiencesContainer;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> destinationFilterComboBox;
    @FXML private Button btnAddExperience;
    @FXML private Button btnMyExperiences;
    @FXML private Button btnSearch;
    @FXML private Button btnClearFilters;
    @FXML private HBox userInfoSection;
    @FXML private Label userDisplayName;
    @FXML private Button profileButton;
    @FXML private Button returnToMainButton;
    @FXML private Button logoutButton;
    
    // Notification components
    @FXML
    private StackPane notificationIconContainer;
    
    @FXML
    private ImageView NotificationIcon;
    
    @FXML
    private Label notificationBadge;
    
    private final ExperienceService experienceService = new ExperienceService();
    private final UserService userService = new UserService();
    private final LikeService likeService = new LikeService();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final NotificationManager notificationManager = NotificationManager.getInstance();
    
    private boolean showingMyExperiences = false;
    private User currentUser;
    private Popup notificationPopup;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize filter options
        filterComboBox.getItems().addAll("Most Recent", "Most Liked");
        filterComboBox.getSelectionModel().selectFirst();
        
        // Initialize destination filter
        destinationFilterComboBox.getItems().add("All Destinations");
        destinationFilterComboBox.getItems().addAll(experienceService.getAllDestinations());
        destinationFilterComboBox.getSelectionModel().selectFirst();
        
        // Enhanced button styling
        enhanceButtonUI();
        
        // Check if user is logged in
        if (sessionManager.isLoggedIn()) {
            User currentUser = sessionManager.getCurrentUser();
            if (currentUser != null) {
                userDisplayName.setText(currentUser.getNom() + " " + currentUser.getPrenom());
                userDisplayName.setVisible(true);
                profileButton.setVisible(true);
                logoutButton.setVisible(true);
                notificationIconContainer.setVisible(true);
                
                // Initialize notification badge
                updateNotificationBadge();
                
                // Set notification callback
                notificationManager.setOnNotificationAddedCallback(this::updateNotificationBadge);
            }
        } else {
            userDisplayName.setText("");
            userDisplayName.setVisible(false);
            profileButton.setVisible(false);
            logoutButton.setVisible(false);
            btnMyExperiences.setVisible(false);
            notificationIconContainer.setVisible(false);
        }
        
        // Add change listeners
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadExperiences());
        destinationFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadExperiences());
        
        // Load experiences
        loadExperiences();
    }
    
    private void enhanceButtonUI() {
        // Style the buttons with better visuals
        String buttonStyle = "-fx-background-color: #307D91; -fx-text-fill: white; -fx-font-weight: bold; " +
                           "-fx-background-radius: 5; -fx-padding: 8 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);";
        
        btnAddExperience.setStyle(buttonStyle);
        btnMyExperiences.setStyle(buttonStyle);
        btnSearch.setStyle(buttonStyle);
        btnClearFilters.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; " +
                              "-fx-background-radius: 5; -fx-padding: 8 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);");
        
        // Add hover effects
        addHoverEffect(btnAddExperience);
        addHoverEffect(btnMyExperiences);
        addHoverEffect(btnSearch);
        addHoverEffect(btnClearFilters);
        
        // Style search field and filters
        searchField.setPromptText("Search experiences...");
        searchField.setStyle("-fx-padding: 8; -fx-background-radius: 5;");
        
        destinationFilterComboBox.setPromptText("Filter by destination");
        destinationFilterComboBox.setStyle("-fx-padding: 7; -fx-background-radius: 5;");
        
        filterComboBox.setStyle("-fx-padding: 7; -fx-background-radius: 5;");
    }
    
    private void addHoverEffect(Button button) {
        String originalStyle = button.getStyle();
        String hoverStyle = originalStyle + "-fx-background-color: #255A6B; -fx-cursor: hand;";
        
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(originalStyle));
    }
    
    @FXML
    private void returnToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/mainMenu.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the current user before showing the view
            MainMenuController controller = loader.getController();
            
            // Make sure we're using the current user from the session manager
            User currentUser = sessionManager.getCurrentUser();
            if (currentUser != null) {
                controller.setUserInfo(currentUser);
            }
            
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error", "Failed to return to main menu: " + e.getMessage());
        }
    }
    
    @FXML
    private void showProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/profile.fxml"));
            Parent root = loader.load();
            ProfileController controller = loader.getController();
            controller.setUserInfo(currentUser);
            
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error", "Failed to open profile: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        try {
            if (currentUser != null) {
                sessionManager.invalidateSession(currentUser.getEmail());
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Error", "Failed to logout: " + e.getMessage());
        }
    }
    
    @FXML
    private void onAddExperienceClicked() {
        if (!sessionManager.isLoggedIn()) {
            showLoginRequiredAlert();
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/badro/experienceForm.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Share Your Experience");
            stage.setScene(new Scene(root));
            
            // Get controller and set current user
            ExperienceFormController controller = loader.getController();
            controller.setOnExperienceAddedCallback(this::loadExperiences);
            
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Form Error", "Could not open form: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onMyExperiencesClicked() {
        if (!sessionManager.isLoggedIn()) {
            showLoginRequiredAlert();
            return;
        }
        
        showingMyExperiences = !showingMyExperiences;
        btnMyExperiences.setText(showingMyExperiences ? "All Experiences" : "My Experiences");
        loadExperiences();
    }
    
    @FXML
    private void onSearchClicked() {
        loadExperiences();
    }
    
    @FXML
    private void onClearFiltersClicked() {
        searchField.clear();
        filterComboBox.getSelectionModel().selectFirst();
        destinationFilterComboBox.getSelectionModel().selectFirst();
        loadExperiences();
    }
    
    private void loadExperiences() {
        experiencesContainer.getChildren().clear();
        
        if (!sessionManager.isLoggedIn() && showingMyExperiences) {
            showingMyExperiences = false;
            btnMyExperiences.setText("My Experiences");
        }
        
        // Get filter values
        String searchTerm = searchField.getText().trim();
        String destinationFilter = destinationFilterComboBox.getValue();
        if ("All Destinations".equals(destinationFilter)) {
            destinationFilter = "";
        }
        
        var experiences = getFilteredExperiences(searchTerm, destinationFilter);
        
        // Apply sort filter
        String sortFilter = filterComboBox.getValue();
        if ("Most Recent".equals(sortFilter)) {
            experiences.sort((e1, e2) -> e2.getDatePosted().compareTo(e1.getDatePosted()));
        } else if ("Most Liked".equals(sortFilter)) {
            experiences.sort((e1, e2) -> {
                int likes1 = likeService.getLikeCount(e1.getId());
                int likes2 = likeService.getLikeCount(e2.getId());
                return Integer.compare(likes2, likes1);
            });
        }
        
        // Check if we have any experiences to display
        if (experiences.isEmpty()) {
            Label noExperiencesLabel = new Label("No experiences found");
            noExperiencesLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #6c757d; -fx-padding: 20;");
            experiencesContainer.getChildren().add(noExperiencesLabel);
            return;
        }
        
        // Create cards for each experience
        for (Experience experience : experiences) {
            VBox card = createExperienceCard(experience);
            experiencesContainer.getChildren().add(card);
        }
    }
    
    private List<Experience> getFilteredExperiences(String keyword, String destination) {
        int currentUserId = sessionManager.getCurrentUserId();
        
        // First filter by user (if showing my experiences)
        var experiences = showingMyExperiences 
            ? experienceService.getByUserId(currentUserId)
            : experienceService.getAll();
        
        // Apply search filters if either is provided
        if (!keyword.isEmpty() || !destination.isEmpty()) {
            experiences = experienceService.advancedSearch(keyword, destination);
            
            // Filter by user if necessary
            if (showingMyExperiences) {
                experiences.removeIf(e -> e.getUserId() != currentUserId);
            }
        }
        
        return experiences;
    }
    
    private VBox createExperienceCard(Experience experience) {
        try {
            VBox card = new VBox(10);
            card.setPadding(new Insets(20));
            card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-background-radius: 12; -fx-border-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 2);");
            
            // Header with title and user info in separate rows for better visibility
            Label titleLabel = new Label(experience.getTitle());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #307D91;");
            
            // Get user info
            Optional<User> userOpt = userService.getUserById(experience.getUserId());
            String userName = userOpt.map(u -> u.getNom() + " " + u.getPrenom()).orElse("Unknown User");
            
            // Improved user info layout
            HBox infoBox = new HBox(10);
            infoBox.setStyle("-fx-padding: 5 0 10 0;");
            
            // Profile icon
            ImageView profileIcon = new ImageView();
            try {
                URL iconUrl = getClass().getResource("/tn/badro/images/profile_icon.png");
                if (iconUrl != null) {
                    profileIcon.setImage(new Image(iconUrl.toString()));
                } else {
                    // Fallback to a colored circle if icon not found
                    Circle profileCircle = new Circle(10, Color.valueOf("#307D91"));
                    StackPane circlePane = new StackPane(profileCircle);
                    SnapshotParameters parameters = new SnapshotParameters();
                    parameters.setFill(Color.TRANSPARENT);
                    profileIcon.setImage(circlePane.snapshot(parameters, null));
                }
                profileIcon.setFitHeight(18);
                profileIcon.setFitWidth(18);
            } catch (Exception e) {
                System.err.println("Error loading profile icon: " + e.getMessage());
            }
            
            Label userNameLabel = new Label("By " + userName);
            userNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
            
            Label dateLabel = new Label(experience.getDatePosted().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            dateLabel.setStyle("-fx-text-fill: #6c757d;");
            
            infoBox.getChildren().addAll(
                new HBox(5, profileIcon, userNameLabel),
                new Label("•"),
                dateLabel
            );
            
            if (experience.getLocation() != null && !experience.getLocation().trim().isEmpty()) {
                ImageView locationIcon = new ImageView();
                try {
                    URL iconUrl = getClass().getResource("/tn/badro/images/location_icon.png");
                    if (iconUrl != null) {
                        locationIcon.setImage(new Image(iconUrl.toString()));
                        locationIcon.setFitHeight(16);
                        locationIcon.setFitWidth(16);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading location icon: " + e.getMessage());
                }
                
                Label locationLabel = new Label(experience.getLocation());
                locationLabel.setStyle("-fx-text-fill: #6c757d;");
                
                infoBox.getChildren().addAll(
                    new Label("•"),
                    new HBox(5, locationIcon, locationLabel)
                );
            }
            
            // Destination badges with enhanced UI
            HBox tagBox = new HBox(8);
            tagBox.setPadding(new Insets(0, 0, 10, 0));
            
            if (experience.getDestination() != null && !experience.getDestination().trim().isEmpty()) {
                Label destinationTag = new Label(experience.getDestination());
                destinationTag.setGraphic(createIcon("destination_icon.png", 14));
                destinationTag.setContentDisplay(ContentDisplay.LEFT);
                destinationTag.setStyle("-fx-background-color: #e6f7fc; -fx-text-fill: #307D91; -fx-padding: 7 14; " +
                                    "-fx-background-radius: 50; -fx-font-size: 13px; -fx-font-weight: bold; " +
                                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);");
                tagBox.getChildren().add(destinationTag);
            }
            
            // Image container with improved styling
            StackPane imageContainer = new StackPane();
            imageContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 3, 0, 0, 1);");
            
            // Image if available
            if (experience.getImagePath() != null && !experience.getImagePath().isEmpty()) {
                File file = new File(experience.getImagePath());
                if (file.exists()) {
                    ImageView imageView = new ImageView(new Image(file.toURI().toString()));
                    imageView.setFitWidth(450);
                    imageView.setPreserveRatio(true);
                    
                    // Add subtle border and rounded corners to image
                    StackPane.setMargin(imageView, new Insets(4));
                    imageContainer.getChildren().add(imageView);
                    card.getChildren().add(imageContainer);
                }
            }
            
            // Description preview with improved styling
            String description = experience.getDescription();
            Label descriptionLabel = new Label(description.length() > 150 
                ? description.substring(0, 150) + "..." 
                : description);
            descriptionLabel.setWrapText(true);
            descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #343a40; -fx-padding: 5 0;");
            
            // Card footer with enhanced UI
            HBox footerBox = new HBox();
            footerBox.setAlignment(Pos.CENTER_LEFT);
            footerBox.setSpacing(15);
            footerBox.setPadding(new Insets(10, 0, 5, 0));
            
            // Likes and comments count with icons
            int likes = likeService.getLikeCount(experience.getId());
            int comments = experienceService.getCommentCount(experience.getId());
            
            HBox statsBox = new HBox(15);
            statsBox.setAlignment(Pos.CENTER_LEFT);
            statsBox.setStyle("-fx-padding: 10 0 10 0;");
            
            // Like count with heart icon
            ImageView likeIcon = createIcon("heart_icon.png", 16);
            Label likesLabel = new Label(likes + (likes == 1 ? " like" : " likes"));
            likesLabel.setGraphic(likeIcon);
            likesLabel.setContentDisplay(ContentDisplay.LEFT);
            likesLabel.setStyle("-fx-text-fill: #307D91; -fx-font-weight: bold;");
            
            // Comment count with comment icon
            ImageView commentIcon = createIcon("comment_icon.png", 16);
            Label commentsLabel = new Label(comments + (comments == 1 ? " comment" : " comments"));
            commentsLabel.setGraphic(commentIcon);
            commentsLabel.setContentDisplay(ContentDisplay.LEFT);
            commentsLabel.setStyle("-fx-text-fill: #307D91; -fx-font-weight: bold;");
            
            statsBox.getChildren().addAll(
                likesLabel,
                new Label("•"),
                commentsLabel
            );
            
            // Button container
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            
            // Read more button with enhanced styling
            Button btnReadMore = new Button("Read More");
            btnReadMore.setStyle("-fx-background-color: #307D91; -fx-text-fill: white; -fx-font-weight: bold; " +
                                "-fx-background-radius: 5; -fx-padding: 8 16; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);");
            
            // Add hover effect to button
            String originalStyle = btnReadMore.getStyle();
            String hoverStyle = originalStyle + "-fx-background-color: #255A6B; -fx-cursor: hand;";
            btnReadMore.setOnMouseEntered(e -> btnReadMore.setStyle(hoverStyle));
            btnReadMore.setOnMouseExited(e -> btnReadMore.setStyle(originalStyle));
            
            btnReadMore.setOnAction(e -> openExperienceDetail(experience));
            
            // Add buttons to button container
            buttonBox.getChildren().add(btnReadMore);
            
            // Combine stats and buttons in footer
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            footerBox.getChildren().addAll(statsBox, spacer, buttonBox);
            
            // Add all elements to card
            card.getChildren().addAll(titleLabel, infoBox);
            
            // Add destination tags if present
            if (!tagBox.getChildren().isEmpty()) {
                card.getChildren().add(tagBox);
            }
            
            // Add remaining elements
            card.getChildren().addAll(descriptionLabel, footerBox);
            
            // Add subtle hover effect to the entire card
            String originalCardStyle = card.getStyle();
            String hoverCardStyle = originalCardStyle + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 12, 0, 0, 4);";
            card.setOnMouseEntered(e -> {
                card.setStyle(hoverCardStyle);
                card.setCursor(Cursor.HAND);
            });
            card.setOnMouseExited(e -> card.setStyle(originalCardStyle));
            
            // Make the entire card clickable
            card.setOnMouseClicked(e -> openExperienceDetail(experience));
            
            return card;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private ImageView createIcon(String iconName, int size) {
        try {
            URL iconUrl = getClass().getResource("/tn/badro/images/" + iconName);
            if (iconUrl != null) {
                Image icon = new Image(iconUrl.toString());
                ImageView imageView = new ImageView(icon);
                imageView.setFitHeight(size);
                imageView.setFitWidth(size);
                return imageView;
            }
        } catch (Exception e) {
            System.err.println("Could not load icon: " + iconName);
        }
        return new ImageView();
    }
    
    private void openExperienceDetail(Experience experience) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/badro/experienceDetail.fxml"));
            Parent root = loader.load();
            
            // Get controller and pass experience
            ExperienceDetailController controller = loader.getController();
            controller.setExperience(experience);
            controller.setOnDeleteCallback(this::loadExperiences);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(experience.getTitle());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Detail Error", "Could not open experience details: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showLoginRequiredAlert() {
        // First, try to get the stage (if it exists)
        Stage currentStage = null;
        try {
            if (mainBorderPane != null && mainBorderPane.getScene() != null) {
                currentStage = (Stage) mainBorderPane.getScene().getWindow();
            }
        } catch (Exception e) {
            // Handle gracefully if we can't get the stage
        }
        
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Login Required");
        alert.setHeaderText("Authentication Required");
        alert.setContentText("You must be logged in to access the experiences page.");
        
        ButtonType loginButton = new ButtonType("Login");
        ButtonType cancelButton = ButtonType.CANCEL;
        
        alert.getButtonTypes().setAll(loginButton, cancelButton);
        
        // Store reference to the result outside the try blocks
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == loginButton) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/login.fxml"));
                Parent loginRoot = loader.load();
                
                // If we have a current stage, use it, otherwise create a new one
                Stage targetStage = (currentStage != null) ? currentStage : new Stage();
                targetStage.setScene(new Scene(loginRoot));
                targetStage.show();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error loading login page: " + e.getMessage());
            }
        } else {
            // If they cancel, go back to main menu
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/mainMenu.fxml"));
                Parent root = loader.load();
                
                // If we have a current stage, use it, otherwise create a new one
                Stage targetStage = (currentStage != null) ? currentStage : new Stage();
                targetStage.setScene(new Scene(root));
                targetStage.show();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error loading main menu: " + e.getMessage());
            }
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Updates the notification badge count
     */
    private void updateNotificationBadge() {
        if (notificationBadge != null) {
            int unreadCount = notificationManager.getUnreadCount();
            notificationBadge.setText(String.valueOf(unreadCount));
            notificationBadge.setVisible(unreadCount > 0);
        }
    }
    
    /**
     * Handles clicks on the notification icon by showing/hiding the notification popup
     */
    @FXML
    private void handleNotificationIconClick() {
        if (notificationPopup != null && notificationPopup.isShowing()) {
            notificationPopup.hide();
            return;
        }
        
        showNotifications();
    }
    
    /**
     * Shows the notification popup with the list of notifications
     */
    private void showNotifications() {
        if (sessionManager.getCurrentUser() == null) return;
        
        try {
            // Create popup content
            notificationPopup = new Popup();
            notificationPopup.setAutoHide(true);

            VBox notificationBox = new VBox();
            notificationBox.setSpacing(0);
            notificationBox.setStyle("-fx-background-color: white; -fx-padding: 0; -fx-border-color: #307D91; " +
                    "-fx-border-width: 2; -fx-background-radius: 12; -fx-border-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, #024F65, 10, 0.5, 0, 2);");
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
            for (Notification n : notificationManager.getNotifications()) {
                VBox notifItem = new VBox();
                notifItem.setStyle("-fx-padding: 12 16 10 16; -fx-background-color: " + 
                        (n.isRead() ? "#f8f9fa;" : "#eaf6fa;") + 
                        "-fx-background-radius: 8; -fx-border-width: 0 0 1 0; -fx-border-color: #f0f0f0;");
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
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            double xPos = NotificationIcon.localToScreen(0, 0).getX();
            double yPos = NotificationIcon.localToScreen(0, 0).getY() + NotificationIcon.getFitHeight();
            notificationPopup.show(stage, xPos, yPos);
            
            // Mark notifications as read when popup is closed
            notificationPopup.setOnHidden(e -> {
                notificationManager.markAllAsRead();
                updateNotificationBadge();
            });
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 