package tn.badro.Controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import tn.badro.services.PreferencesService;
import tn.badro.services.MatchingService;
import tn.badro.services.EmailService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.badro.entities.Preferences;
import javafx.event.ActionEvent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.media.AudioClip;
import tn.badro.entities.Notification;
import tn.badro.entities.NotificationManager;
import tn.badro.services.UserService;
import tn.badro.entities.User;
import java.time.format.DateTimeFormatter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import tn.badro.services.SessionManager;
import tn.badro.services.NotificationService;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;

public class PreferenceController {
    private PreferencesService preferencesService = new PreferencesService();
    private final MatchingService matchingService = new MatchingService();
    private final EmailService emailService = new EmailService();
    private final UserService userService = new UserService();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final NotificationService notificationService = new NotificationService();
    private User currentUser; // Store the current user
    private Preferences selectedPreference; // Track the selected preference

    // Updated UI components
    @FXML private TilePane preferencesContainer;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private RadioButton englishRadio, frenchRadio, arabicRadio;
    @FXML private RadioButton onlineRadio, inPersonRadio;
    @FXML private TextField levelTextField;
    @FXML private TextField UniversityType;
    @FXML private TextField culturalAct;
    @FXML private TextField Domain;
    @FXML private TextField Climat;
    @FXML private Button addPreferenceButton;
    @FXML private TabPane tabPane;
    @FXML private Tab tabAdd;
    @FXML private Button Update;
    @FXML private AnchorPane mainAnchorPane;
    @FXML private Button notificationIcon;
    @FXML private ListView<String> notificationListView;
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
        
        System.out.println("Preference page accessed by user: " + currentUser.getEmail());

        // Initialize country dropdown
        countryComboBox.getItems().addAll("France", "Germany", "Canada", "Japan", "United States", "United Kingdom", "Spain", "Italy");
        
        // Load user preferences and display as cards
        loadPreferences();

        updateNotificationBadge();
        NotificationManager.getInstance().getNotifications().addListener((javafx.collections.ListChangeListener<Notification>) c -> updateNotificationBadge());
    }

    /**
     * Loads preferences from database and displays them as cards
     */
    private void loadPreferences() {
        try {
            List<Preferences> userPreferences = preferencesService.getPreferencesByUserId(currentUser.getId());
            preferencesContainer.getChildren().clear();
            
            if (userPreferences.isEmpty()) {
                // Show empty state message
                Label emptyLabel = new Label("You haven't added any preferences yet");
                emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6C757D;");
                
                Button addFirstBtn = new Button("Add Your First Preference");
                addFirstBtn.setStyle("-fx-background-color: #4B9CD3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
                addFirstBtn.setOnAction(e -> switchToAddTab());
                
                VBox emptyState = new VBox(20, emptyLabel, addFirstBtn);
                emptyState.setAlignment(javafx.geometry.Pos.CENTER);
                emptyState.setPadding(new Insets(50));
                emptyState.setPrefWidth(preferencesContainer.getPrefWidth());
                emptyState.setPrefHeight(300);
                
                preferencesContainer.getChildren().add(emptyState);
            } else {
                // Create a card for each preference
                for (Preferences pref : userPreferences) {
                    preferencesContainer.getChildren().add(createPreferenceCard(pref));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Loading Preferences", null, "Could not load your preferences from the database.");
        }
    }
    
    /**
     * Creates a nicely styled card for displaying a preference
     */
    private VBox createPreferenceCard(Preferences pref) {
        // Container
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        card.setPrefWidth(320);
        card.setPadding(new Insets(20));
        card.setUserData(pref); // Store preference with the card
        
        // Card header with flag/country
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label countryLabel = new Label(pref.getCountry());
        countryLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4B9CD3;");
        
        Label climatLabel = new Label(pref.getClimat_pref());
        climatLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6C757D;");
        
        VBox locationInfo = new VBox(5, countryLabel, climatLabel);
        header.getChildren().addAll(locationInfo);
        
        // Card content - main preference details
        VBox content = new VBox(10);
        
        // Domain
        HBox domainRow = createInfoRow("Domain", pref.getDomain());
        
        // Language
        HBox languageRow = createInfoRow("Language", pref.getPreferred_language() + " (" + pref.getLanguage_level() + ")");
        
        // Teaching Mode
        HBox teachingRow = createInfoRow("Teaching", pref.getTeaching_mode());
        
        // University Type
        HBox universityRow = createInfoRow("University", pref.getUniversity_type());
        
        // Cultural Activities 
        HBox activitiesRow = createInfoRow("Activities", pref.getCultural_activities());
        
        content.getChildren().addAll(domainRow, languageRow, teachingRow, universityRow, activitiesRow);
        
        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 3;");
        editBtn.setCursor(javafx.scene.Cursor.HAND);
        editBtn.setOnAction(e -> {
            selectedPreference = pref;
            fillFormFields(pref);
            switchToAddTab();
        });
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 3;");
        deleteBtn.setCursor(javafx.scene.Cursor.HAND);
        deleteBtn.setOnAction(e -> deletePreference(pref, card));
        
        actions.getChildren().addAll(editBtn, deleteBtn);
        
        // Add all components to card
        card.getChildren().addAll(header, new Separator(), content, actions);
        
        return card;
    }
    
    /**
     * Creates a row with label and value for the preference card
     */
    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label labelNode = new Label(label + ":");
        labelNode.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        labelNode.setPrefWidth(80);
        
        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-text-fill: #212529;");
        valueNode.setWrapText(true);
        HBox.setHgrow(valueNode, Priority.ALWAYS);
        
        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }
    
    /**
     * Deletes a preference and removes its card
     */
    private void deletePreference(Preferences pref, Node card) {
        // Verify that the preference belongs to the current user
        if (pref.getId_user() != currentUser.getId()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", null, "You can only delete your own preferences.");
            return;
        }
        
        // Ask for confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete this preference?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete from database
            preferencesService.supprimer(pref);
            
            // Remove card from UI with animation
            FadeTransition fade = new FadeTransition(Duration.millis(300), card);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(e -> {
                preferencesContainer.getChildren().remove(card);
                if (preferencesContainer.getChildren().isEmpty()) {
                    loadPreferences(); // Reload to show empty state
                }
            });
            fade.play();
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Preference Deleted", null, "Your preference has been deleted successfully.");
        }
    }
    
    /**
     * Switch to the Add tab
     */
    @FXML
    private void switchToAddTab() {
        tabPane.getSelectionModel().select(tabAdd);
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
        
        // Redirect to login page or main menu
        try {
            // This is an example, you'll need to adapt it to your navigation structure
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/login.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) (mainAnchorPane != null ? mainAnchorPane.getScene().getWindow() : new Stage());
            stage.setScene(new Scene(loginRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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
            
            Stage stage = (Stage) tabPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", null, "Failed to return to main menu: " + e.getMessage());
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

    private void loadNotifications() {
        List<String> notifications = matchingService.loadRecentNotifications();
        notificationListView.getItems().setAll(notifications);
    }

    private void fillFormFields(Preferences p) {
        Climat.setText(p.getClimat_pref());
        countryComboBox.setValue(p.getCountry());
        Domain.setText(p.getDomain());
        setLanguageRadio(p.getPreferred_language());
        setTeachingModeRadio(p.getTeaching_mode());
        UniversityType.setText(p.getUniversity_type());
        culturalAct.setText(p.getCultural_activities());
        levelTextField.setText(p.getLanguage_level());
    }

    private void setLanguageRadio(String language) {
        // Réinitialiser toutes les radios
        englishRadio.setSelected(false);
        frenchRadio.setSelected(false);
        arabicRadio.setSelected(false);

        // Sélectionner celle qui correspond
        switch (language) {
            case "English" -> englishRadio.setSelected(true);
            case "French" -> frenchRadio.setSelected(true);
            case "Arabic" -> arabicRadio.setSelected(true);
        }
    }

    private void setTeachingModeRadio(String mode) {
        // Réinitialiser les deux radios
        onlineRadio.setSelected(false);
        inPersonRadio.setSelected(false);

        // Sélectionner celle qui correspond
        switch (mode) {
            case "Online" -> onlineRadio.setSelected(true);
            case "In-person classes" -> inPersonRadio.setSelected(true);
        }
    }

    @FXML
    public void handleAddPreference(ActionEvent event) {
        // First check if user is logged in
        if (!sessionManager.isLoggedIn() || currentUser == null) {
            showLoginRequiredAlert();
            return;
        }
        
        // Get values from fields...
        String country = countryComboBox.getValue();
        if (country == null || country.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", null,"Please select a country.");
            return;
        }

        String language;
        if (englishRadio.isSelected()) {
            language = "English";
        } else if (frenchRadio.isSelected()) {
            language = "French";
        } else if (arabicRadio.isSelected()) {
            language = "Arabic";
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", null,"Please select a language.");
            return;
        }

        String teachingMode;
        if (onlineRadio.isSelected()) {
            teachingMode = "Online";
        } else if (inPersonRadio.isSelected()) {
            teachingMode = "In-Person";
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", null,"Please select a teaching mode.");
            return;
        }

        String level = levelTextField.getText();
        if (level == null || level.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", null,"Please enter a language level.");
            return;
        }

        String universityType = UniversityType.getText();
        if (universityType == null || universityType.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", null,"Please enter a university type.");
            return;
        }

        String culturalActivities = culturalAct.getText();
        if (culturalActivities == null || culturalActivities.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", null,"Please enter cultural activities.");
            return;
        }

        String domain = Domain.getText();
        if (domain == null || domain.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", null,"Please enter a study domain.");
            return;
        }

        String climat = Climat.getText();
        if (climat == null || climat.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", null,"Please enter a preferred climate.");
            return;
        }

        Preferences newPref = new Preferences(
                climat,
                country,
                domain,
                language,
                teachingMode,
                universityType,
                culturalActivities,
                level,
                currentUser.getId() // Set the user ID
        );

        try {
            preferencesService.ajouter(newPref);
            
            // Clear form
            clearForm();
            
            // Switch to preferences tab and refresh
            tabPane.getSelectionModel().select(0); // Select first tab
            loadPreferences(); // Reload cards
            
            showAlert(Alert.AlertType.INFORMATION, "Success", null,"Preference added successfully!");

            // Check for matching preferences from OTHER users
            List<Preferences> matchingPreferences = preferencesService.findMatchingPreferences(newPref);
            
            if (!matchingPreferences.isEmpty()) {
                // Modified: Handle multiple matches more efficiently
                handleMatchingPreferences(matchingPreferences, newPref);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", null,"An error occurred: " + e.getMessage());
        }
    }

    /**
     * Handle matching preferences more efficiently by batching notifications and preventing UI lag
     */
    private void handleMatchingPreferences(List<Preferences> matchingPreferences, Preferences userPreference) {
        // Play notification sound only once
        playNotificationSound();
        
        // If there are too many matches, create a summary notification instead of individual ones
        if (matchingPreferences.size() > 3) {
            // Send batch notification to current user
            String message = "You have " + matchingPreferences.size() + " new preference matches! Check them out in your profile.";
            notificationService.sendSystemNotification(currentUser, "Multiple Preference Matches", message);
            
            // Process individual notifications in background without UI updates
            new Thread(() -> {
                for (Preferences matchingPref : matchingPreferences) {
                    try {
                        // Get the user who owns the matching preference
                        Optional<User> matchingUserOpt = userService.getUserById(matchingPref.getId_user());
                        
                        if (matchingUserOpt.isPresent()) {
                            User matchingUser = matchingUserOpt.get();
                            // Only notify the other user about the match
                            notificationService.sendPreferenceMatchNotification(matchingUser, matchingPref, userPreference);
                            
                            // Add small delay to prevent database congestion
                            Thread.sleep(100);
                        }
                    } catch (Exception e) {
                        // Log error but don't interrupt
                        System.err.println("Error processing notification: " + e.getMessage());
                    }
                }
            }).start();
        } else {
            // For a small number of matches, process normally but more efficiently
            for (Preferences matchingPref : matchingPreferences) {
                // Get the user who owns the matching preference
                Optional<User> matchingUserOpt = userService.getUserById(matchingPref.getId_user());
                
                if (matchingUserOpt.isPresent()) {
                    User matchingUser = matchingUserOpt.get();
                    
                    // Notify both users about the match
                    notificationService.sendPreferenceMatchNotification(currentUser, userPreference, matchingPref);
                    notificationService.sendPreferenceMatchNotification(matchingUser, matchingPref, userPreference);
                }
            }
        }
        
        // Update the UI badge count after notifications are processed
        updateNotificationBadge();
    }

    @FXML
    private void modifierPreference() {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn() || currentUser == null) {
            showLoginRequiredAlert();
            return;
        }
        
        if (selectedPreference == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", null, "Please select a preference to update.");
            return;
        }
        
        // Verify that the preference belongs to the current user
        if (selectedPreference.getId_user() != currentUser.getId()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", null, "You can only modify your own preferences.");
            return;
        }

        String climat = Climat.getText();
        String country = countryComboBox.getValue();
        String domain = Domain.getText();
        String language = getSelectedLanguage();
        String teachingMode = getSelectedTeachingMode();
        String universityType = UniversityType.getText();
        String culturalActivities = culturalAct.getText();
        String level = levelTextField.getText();

        // ✅ Étape 1 : Vérifie les champs vides
        if (climat == null || climat.isBlank() ||
                country == null || country.isBlank() ||
                domain == null || domain.isBlank() ||
                language == null || language.isBlank() ||
                teachingMode == null || teachingMode.isBlank() ||
                universityType == null || universityType.isBlank() ||
                culturalActivities == null || culturalActivities.isBlank() ||
                level == null || level.isBlank()) {

            showAlert(Alert.AlertType.WARNING, "Missing Fields", null, "Please fill in all fields before updating the preference.");
            return;
        }

        // ✅ Étape 2 : Vérifie que certains champs ne soient pas numériques uniquement
        if (climat.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Climate", "Input Error", "The 'climate' field should not contain only digits.");
            return;
        }
        if (domain.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Domain", "Input Error", "The 'domain' field should not contain only digits.");
            return;
        }
        if (universityType.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Invalid University Type", "Input Error", "The 'university type' field should not contain only digits.");
            return;
        }
        if (culturalActivities.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Cultural Activities", "Input Error", "The 'cultural activities' field should not contain only digits.");
            return;
        }

        // ✅ Étape 3 : Vérifie le niveau
        if (!level.matches("^(A[1-2]|B[1-2]|C[1-2])$")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Level", null, "The level must be: A1, A2, B1, B2, C1 or C2.");
            return;
        }

        // ✅ Étape 4 : Mise à jour des données
        try {
            selectedPreference.setClimat_pref(climat);
            selectedPreference.setCountry(country);
            selectedPreference.setDomain(domain);
            selectedPreference.setPreferred_language(language);
            selectedPreference.setTeaching_mode(teachingMode);
            selectedPreference.setUniversity_type(universityType);
            selectedPreference.setCultural_activities(culturalActivities);
            selectedPreference.setLanguage_level(level);
            selectedPreference.setId_user(currentUser.getId()); // Ensure user ID is preserved

            preferencesService.modifier(selectedPreference);
            
            // Clear selection and form
            selectedPreference = null;
            clearForm();
            
            // Switch to preferences tab and refresh
            tabPane.getSelectionModel().select(0); // Select first tab
            loadPreferences(); // Reload cards

            showAlert(Alert.AlertType.INFORMATION, "Update Successful", null, "Preference updated successfully!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", null, "An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        Climat.clear();
        countryComboBox.setValue(null);
        Domain.clear();
        UniversityType.clear();
        culturalAct.clear();
        levelTextField.clear();
        englishRadio.setSelected(true);
        onlineRadio.setSelected(true);
        selectedPreference = null;
    }

    @FXML
    public String getSelectedLanguage() {
        if (englishRadio.isSelected()) return "English";
        if (frenchRadio.isSelected()) return "French";
        if (arabicRadio.isSelected()) return "Arabic";
        return "";
    }

    @FXML
    public String getSelectedTeachingMode() {
        if (onlineRadio.isSelected()) return "Online";
        if (inPersonRadio.isSelected()) return "In-person classes";
        return "";
    }

    private boolean isLevelValid() {
        String level = levelTextField.getText();
        if (level != null && level.matches("^(A[1-2]|B[1-2]|C[1-2])$")) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Niveau invalide");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez entrer un niveau valide parmi : A1, A2, B1, B2, C1, C2.");
            alert.showAndWait();
            return false;
        }
    }

    public void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void showToast(String message) {
        if (mainAnchorPane == null || mainAnchorPane.getScene() == null) {
            System.err.println("Erreur : mainAnchorPane ou sa scène est null.");
            return;
        }

        Stage ownerStage = (Stage) mainAnchorPane.getScene().getWindow();

        Stage toastStage = new Stage();
        toastStage.initOwner(ownerStage);
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        Label label = new Label(message);
        label.setWrapText(true);
        label.setStyle(
                "-fx-background-color: linear-gradient(to right, #2193b0, #6dd5ed);" + // joli dégradé bleu
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 20px 30px;" +
                        "-fx-background-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 2);"
        );

        StackPane root = new StackPane(label);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: transparent;");
        root.setMaxWidth(400);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        toastStage.setScene(scene);

        // Position : bas droite de l'écran
        toastStage.setX(Screen.getPrimary().getVisualBounds().getMaxX() - 450);
        toastStage.setY(Screen.getPrimary().getVisualBounds().getMaxY() - 150);

        playNotificationSound();

        toastStage.show();

        // Animation d'apparition
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Fermeture automatique après 4s avec fondu de sortie
        PauseTransition delay = new PauseTransition(Duration.seconds(7));
        delay.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> toastStage.close());
            fadeOut.play();
        });
        delay.play();
    }

    public void playNotificationSound() {
        try {
            String soundPath = getClass().getResource("/sounds/ding.wav").toString(); // fais bien attention au chemin
            AudioClip clip = new AudioClip(soundPath);
            clip.setVolume(0.6); // Réglage du volume
            clip.play();         // Lecture du son
        } catch (Exception e) {
            System.err.println("Erreur de lecture du son : " + e.getMessage());
        }
    }

    private void updateNotificationBadge() {
        long unread = NotificationManager.getInstance().getUnreadCount();
        notificationBadge.setText(unread > 0 ? String.valueOf(unread) : "");
        notificationBadge.setVisible(unread > 0);
    }
}



