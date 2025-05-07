package tn.badro.Controllers;

import javafx.animation.FadeTransition;
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
import javafx.util.Duration;
import tn.badro.entities.University;
import tn.badro.entities.Programme;
import tn.badro.entities.User;
import tn.badro.entities.Notification;
import tn.badro.entities.NotificationManager;
import tn.badro.services.UniversityService;
import tn.badro.services.ProgrammeService;
import tn.badro.services.SessionManager;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class UniversityFrontendController {
    
    private final UniversityService universityService = new UniversityService();
    private final ProgrammeService programmeService = new ProgrammeService();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private User currentUser;
    private University selectedUniversity;
    
    @FXML private BorderPane mainBorderPane;
    @FXML private TabPane tabPane;
    @FXML private TilePane universitiesContainer;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Tab universityDetailTab;
    @FXML private VBox universityDetailContainer;
    @FXML private ImageView universityImage;
    @FXML private Label universityName;
    @FXML private Label universityLocation;
    @FXML private Label universityDescription;
    @FXML private VBox programsContainer;
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
        
        // Load universities and display as cards
        loadUniversities();
        
        // Set up search handler
        searchField.setOnAction(e -> handleSearch());
        
        // Update notification badge
        updateNotificationBadge();
        NotificationManager.getInstance().getNotifications().addListener((javafx.collections.ListChangeListener<Notification>) c -> updateNotificationBadge());
    }
    
    /**
     * Loads universities from the database and displays them as cards
     */
    private void loadUniversities() {
        List<University> universities = universityService.getAllUniversities();
        universitiesContainer.getChildren().clear();
        
        if (universities.isEmpty()) {
            // Show empty state
            Label emptyLabel = new Label("No universities found");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6C757D;");
            
            VBox emptyState = new VBox(20, emptyLabel);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(50));
            emptyState.setPrefWidth(universitiesContainer.getPrefWidth());
            emptyState.setPrefHeight(300);
            
            universitiesContainer.getChildren().add(emptyState);
        } else {
            // Create a card for each university
            for (University university : universities) {
                universitiesContainer.getChildren().add(createUniversityCard(university));
            }
        }
    }
    
    /**
     * Creates a card for displaying a university
     */
    private VBox createUniversityCard(University university) {
        // Container
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 0; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        card.setPrefWidth(320);
        card.setUserData(university); // Store university with the card
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(e -> showUniversityDetails(university));
        
        // University image - create a rectangle instead of loading an image
        Rectangle placeholderRect = new Rectangle(320, 180);
        placeholderRect.setFill(Color.web("#4B9CD3"));
        
        Label imageLabel = new Label(university.getName().substring(0, Math.min(university.getName().length(), 1)));
        imageLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().addAll(placeholderRect, imageLabel);
        
        // University info
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(15, 20, 20, 20));
        
        Label nameLabel = new Label(university.getName());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4B9CD3;");
        
        Label locationLabel = new Label(university.getLocation());
        locationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6C757D;");
        
        Label descriptionLabel = new Label(truncateText(university.getDescription(), 100));
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #212529;");
        descriptionLabel.setWrapText(true);
        
        HBox programCountBox = new HBox(5);
        programCountBox.setAlignment(Pos.CENTER_LEFT);
        
        // Create a circle icon instead of loading an image
        Circle programIcon = new Circle(8, Color.web("#4B9CD3"));
        
        // Get count of programs for this university
        int programCount = programmeService.getProgrammesByUniversityId(university.getId()).size();
        Label programCountLabel = new Label(programCount + " Exchange Program" + (programCount != 1 ? "s" : ""));
        programCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6C757D;");
        
        programCountBox.getChildren().addAll(programIcon, programCountLabel);
        
        // Learn more button
        Button learnMoreBtn = new Button("View Details");
        learnMoreBtn.setStyle("-fx-background-color: #4B9CD3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 5; -fx-cursor: hand;");
        learnMoreBtn.setMaxWidth(Double.MAX_VALUE);
        learnMoreBtn.setOnAction(e -> showUniversityDetails(university));
        
        infoBox.getChildren().addAll(nameLabel, locationLabel, descriptionLabel, new Separator(), programCountBox, learnMoreBtn);
        
        // Add all components to card
        card.getChildren().addAll(imageContainer, infoBox);
        
        return card;
    }
    
    /**
     * Shows details of a selected university in the university details tab
     */
    private void showUniversityDetails(University university) {
        selectedUniversity = university;
        // Always show placeholder if image is null or empty
        if (university.getImage() == null || university.getImage().isEmpty()) {
            Rectangle imagePlaceholder = new Rectangle(120, 120);
            imagePlaceholder.setFill(Color.web("#4B9CD3"));
            imagePlaceholder.setArcWidth(15);
            imagePlaceholder.setArcHeight(15);
            Label initialLabel = new Label(university.getName().substring(0, Math.min(university.getName().length(), 1)));
            initialLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: white; -fx-font-weight: bold;");
            Pane parent = (Pane) universityImage.getParent();
            if (parent != null) {
                int index = parent.getChildren().indexOf(universityImage);
                if (index >= 0) {
                    StackPane imageContainer = new StackPane();
                    imageContainer.getChildren().addAll(imagePlaceholder, initialLabel);
                    parent.getChildren().set(index, imageContainer);
                }
            }
        } else {
            universityImage.setImage(new Image(university.getImage()));
        }
        universityName.setText(university.getName());
        universityLocation.setText(university.getLocation());
        universityDescription.setText(university.getDescription());
        loadProgramsForUniversity(university.getId());
        tabPane.getSelectionModel().select(universityDetailTab);
    }
    
    /**
     * Loads and displays programs associated with a university
     */
    private void loadProgramsForUniversity(int universityId) {
        programsContainer.getChildren().clear();
        List<Programme> programs = programmeService.getProgrammesByUniversityId(universityId);
        if (programs.isEmpty()) {
            Label emptyLabel = new Label("No exchange programs available for this university.");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6C757D;");
            programsContainer.getChildren().add(emptyLabel);
        } else {
            for (Programme program : programs) {
                VBox card = createProgramCard(program);
                programsContainer.getChildren().add(card);
            }
        }
    }
    
    /**
     * Creates a card for displaying a program
     */
    private VBox createProgramCard(Programme program) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        
        Label nameLabel = new Label(program.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4B9CD3;");
        
        Label typeLabel = new Label(program.getType());
        typeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2e7d32; -fx-background-color: #e8f5e9; -fx-background-radius: 3px; -fx-padding: 2 8 2 8;");
        
        Label descriptionLabel = new Label(program.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #212529;");
        descriptionLabel.setWrapText(true);
        
        // Add buttons container
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.setPadding(new Insets(10, 0, 0, 0));
        
        Button detailsBtn = new Button("View Details");
        detailsBtn.setStyle("-fx-background-color: #4B9CD3; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 7 15; -fx-background-radius: 4;");
        detailsBtn.setOnAction(e -> showProgramDetails(program));
        
        Button applyBtn = new Button("Apply Now");
        applyBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 7 15; -fx-background-radius: 4;");
        applyBtn.setOnAction(e -> handleProgramApplication(program));
        
        buttonContainer.getChildren().addAll(detailsBtn, applyBtn);
        
        card.getChildren().addAll(nameLabel, typeLabel, descriptionLabel, buttonContainer);
        return card;
    }
    
    /**
     * Shows detailed information about a program
     */
    private void showProgramDetails(Programme program) {
        // Create a dialog for program details
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Program Details");
        dialog.setHeaderText(program.getName());
        
        // Create the content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        
        // Program type
        Label typeLabel = new Label("Type: " + program.getType());
        typeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Program description
        Label descHeader = new Label("Description");
        descHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label descContent = new Label(program.getDescription());
        descContent.setStyle("-fx-font-size: 14px;");
        descContent.setWrapText(true);
        
        // University information
        Label uniHeader = new Label("Offered by");
        uniHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        HBox uniInfo = new HBox(10);
        
        // University logo placeholder
        Rectangle logoPlaceholder = new Rectangle(60, 60);
        logoPlaceholder.setFill(Color.web("#4B9CD3"));
        logoPlaceholder.setArcWidth(10);
        logoPlaceholder.setArcHeight(10);
        
        Label initialLabel = new Label(selectedUniversity.getName().substring(0, 1));
        initialLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        StackPane logoStack = new StackPane(logoPlaceholder, initialLabel);
        
        VBox uniTextInfo = new VBox(5);
        Label uniName = new Label(selectedUniversity.getName());
        uniName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label uniLocation = new Label(selectedUniversity.getLocation());
        uniLocation.setStyle("-fx-font-size: 14px; -fx-text-fill: #6C757D;");
        
        uniTextInfo.getChildren().addAll(uniName, uniLocation);
        uniInfo.getChildren().addAll(logoStack, uniTextInfo);
        
        // Apply button
        Button applyButton = new Button("Apply for this Program");
        applyButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
        applyButton.setMaxWidth(Double.MAX_VALUE);
        applyButton.setOnAction(e -> {
            dialog.close();
            handleProgramApplication(program);
        });
        
        content.getChildren().addAll(
            typeLabel, 
            new Separator(),
            descHeader, 
            descContent,
            new Separator(),
            uniHeader,
            uniInfo,
            new Separator(),
            applyButton
        );
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }
    
    /**
     * Handles program application process
     */
    private void handleProgramApplication(Programme program) {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            showLoginRequiredAlert();
            return;
        }
        
        // Show application form
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/badro/programApplicationForm.fxml"));
            Parent root = loader.load();
            
            ProgramApplicationController controller = loader.getController();
            controller.initData(currentUser, selectedUniversity, program);
            
            Stage stage = new Stage();
            stage.setTitle("Apply for Program");
            stage.setScene(new Scene(root));
            stage.initOwner(mainBorderPane.getScene().getWindow());
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Application Error", "Failed to open application form: " + e.getMessage());
        }
    }
    
    /**
     * Handles search functionality
     */
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        
        if (searchTerm.isEmpty()) {
            loadUniversities();
            return;
        }
        
        List<University> universities = universityService.getAllUniversities();
        List<University> filteredUniversities = universities.stream()
            .filter(u -> u.getName().toLowerCase().contains(searchTerm) || 
                         u.getLocation().toLowerCase().contains(searchTerm) ||
                         u.getDescription().toLowerCase().contains(searchTerm))
            .collect(Collectors.toList());
        
        universitiesContainer.getChildren().clear();
        
        if (filteredUniversities.isEmpty()) {
            Label noResultsLabel = new Label("No results found for \"" + searchTerm + "\"");
            noResultsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6C757D;");
            
            Button resetBtn = new Button("Reset Search");
            resetBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
            resetBtn.setOnAction(e -> {
                searchField.clear();
                loadUniversities();
            });
            
            VBox noResultsBox = new VBox(15, noResultsLabel, resetBtn);
            noResultsBox.setAlignment(Pos.CENTER);
            noResultsBox.setPadding(new Insets(50));
            
            universitiesContainer.getChildren().add(noResultsBox);
        } else {
            for (University university : filteredUniversities) {
                universitiesContainer.getChildren().add(createUniversityCard(university));
            }
        }
    }
    
    /**
     * Gets the count of programs for a university
     */
    private int getProgramCountForUniversity(int universityId) {
        // No longer supported, always return 0
        return 0;
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
    private void showPreferences() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/preference.fxml"));
            Parent root = loader.load();
            PreferenceController controller = loader.getController();
            
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to navigate to preferences: " + e.getMessage());
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
     * Shows a login required alert
     */
    private void showLoginRequiredAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Login Required");
        alert.setHeaderText("Authentication Required");
        alert.setContentText("You must be logged in to perform this action.");
        
        ButtonType loginButton = new ButtonType("Login");
        ButtonType cancelButton = ButtonType.CANCEL;
        
        alert.getButtonTypes().setAll(loginButton, cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == loginButton) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/login.fxml"));
                Parent loginRoot = loader.load();
                Stage stage = (Stage) mainBorderPane.getScene().getWindow();
                stage.setScene(new Scene(loginRoot));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
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