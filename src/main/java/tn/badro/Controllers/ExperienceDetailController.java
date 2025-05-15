package tn.badro.Controllers;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import tn.badro.entities.Experience;
import tn.badro.entities.ExperienceComment;
import tn.badro.entities.ExperienceLike;
import tn.badro.entities.User;
import tn.badro.services.CommentService;
import tn.badro.services.ExperienceService;
import tn.badro.services.LikeService;
import tn.badro.services.SessionManager;
import tn.badro.services.TranslationService;
import tn.badro.services.TranslationService.Language;
import tn.badro.services.UserService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class ExperienceDetailController implements Initializable {
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label authorLabel;
    
    @FXML
    private Label dateLabel;
    
    @FXML
    private Label locationLabel;
    
    @FXML
    private Label destinationLabel;
    
    @FXML
    private ImageView experienceImage;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private Button btnLike;
    
    @FXML
    private Label likeCountLabel;
    
    @FXML
    private Label commentCountLabel;
    
    @FXML
    private VBox commentsContainer;
    
    @FXML
    private TextField commentField;
    
    @FXML
    private Button btnComment;
    
    @FXML
    private Button btnEdit;
    
    @FXML
    private Button btnDelete;
    
    @FXML
    private Button btnBack;
    
    @FXML
    private ComboBox<Language> translateComboBox;
    
    @FXML
    private Button btnTranslate;
    
    @FXML
    private Button btnResetTranslation;
    
    @FXML 
    private HBox translationBar;
    
    private Experience experience;
    
    private final ExperienceService experienceService = new ExperienceService();
    private final CommentService commentService = new CommentService();
    private final LikeService likeService = new LikeService();
    private final UserService userService = new UserService();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final TranslationService translationService = new TranslationService();
    
    private Runnable onDeleteCallback;
    private boolean userHasLiked = false;
    private boolean isTranslated = false;
    private Language currentLanguage = Language.ENGLISH;
    
    // Original text storage for reset
    private String originalTitle;
    private String originalDescription;
    private String originalDestination;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Disable comment button if not logged in
        if (!sessionManager.isLoggedIn()) {
            commentField.setDisable(true);
            btnComment.setDisable(true);
            commentField.setPromptText("Login to comment");
        }
        
        // Setup translation components
        setupTranslationUI();
        
        // Enhanced button styling
        enhanceButtons();
        
        // Apply custom styling to the description area
        descriptionArea.setStyle("-fx-control-inner-background: white; -fx-border-color: transparent; " +
                               "-fx-text-fill: #212529; -fx-font-size: 14px;");
    }
    
    private void setupTranslationUI() {
        // Add language options to combobox
        translateComboBox.setItems(FXCollections.observableArrayList(
            Language.ENGLISH, 
            Language.FRENCH, 
            Language.ARABIC, 
            Language.GERMAN,
            Language.SPANISH,
            Language.ITALIAN,
            Language.PORTUGUESE,
            Language.RUSSIAN,
            Language.JAPANESE,
            Language.CHINESE
        ));
        
        translateComboBox.getSelectionModel().select(Language.ENGLISH);
        
        // Initially hide the reset button until translation happens
        btnResetTranslation.setVisible(false);
        
        // Style the translation bar
        translationBar.setAlignment(Pos.CENTER_LEFT);
        translationBar.setSpacing(10);
        translationBar.setPadding(new Insets(5, 0, 5, 0));
        
        // Add icon to translation button
        btnTranslate.setGraphic(createIcon("translate_icon.png", 16));
        
        // Add styles
        btnTranslate.setStyle("-fx-background-color: #307D91; -fx-text-fill: white; -fx-font-weight: bold; " +
                          "-fx-background-radius: 5; -fx-padding: 6 12;");
        btnResetTranslation.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                                    "-fx-background-radius: 5; -fx-padding: 6 12;");
        
        // Add hover effects
        addButtonHoverEffect(btnTranslate);
        addButtonHoverEffect(btnResetTranslation);
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
        return null;
    }
    
    private void enhanceButtons() {
        // Like button
        btnLike.setStyle("-fx-background-color: #307D91; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);");
        
        // Comment button
        btnComment.setStyle("-fx-background-color: #307D91; -fx-text-fill: white; -fx-font-weight: bold; " +
                           "-fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);");
        
        // Add hover effects
        addButtonHoverEffect(btnLike);
        addButtonHoverEffect(btnComment);
        addButtonHoverEffect(btnEdit);
        addButtonHoverEffect(btnDelete);
        addButtonHoverEffect(btnBack);
    }
    
    private void addButtonHoverEffect(Button button) {
        if (button == null) return;
        
        String originalStyle = button.getStyle();
        String baseStyle = originalStyle.isEmpty() ? "-fx-background-color: #307D91; -fx-text-fill: white;" : originalStyle;
        String hoverStyle = baseStyle + "-fx-cursor: hand; -fx-opacity: 0.9;";
        
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
    }
    
    public void setExperience(Experience experience) {
        this.experience = experience;
        displayExperienceDetails();
    }
    
    public void setOnDeleteCallback(Runnable callback) {
        this.onDeleteCallback = callback;
    }
    
    private void displayExperienceDetails() {
        if (experience == null) {
            return;
        }
        
        // Store original text for reset
        originalTitle = experience.getTitle();
        originalDescription = experience.getDescription();
        originalDestination = experience.getDestination();
        
        // Set basic details
        titleLabel.setText(originalTitle);
        descriptionArea.setText(originalDescription);
        
        // Format date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        dateLabel.setText(experience.getDatePosted().format(formatter));
        
        // Set location or hide if not provided
        if (experience.getLocation() != null && !experience.getLocation().trim().isEmpty()) {
            locationLabel.setText(experience.getLocation());
        } else {
            locationLabel.setVisible(false);
        }
        
        // Set destination or hide if not provided
        if (originalDestination != null && !originalDestination.trim().isEmpty()) {
            destinationLabel.setText("Destination: " + originalDestination);
            destinationLabel.setVisible(true);
        } else {
            destinationLabel.setVisible(false);
        }
        
        // Set author name
        Optional<User> author = userService.getUserById(experience.getUserId());
        authorLabel.setText("By " + author.map(u -> u.getNom() + " " + u.getPrenom()).orElse("Unknown User"));
        
        // Set image if available
        if (experience.getImagePath() != null && !experience.getImagePath().isEmpty()) {
            File file = new File(experience.getImagePath());
            if (file.exists()) {
                experienceImage.setImage(new Image(file.toURI().toString()));
            } else {
                experienceImage.setVisible(false);
            }
        } else {
            experienceImage.setVisible(false);
        }
        
        // Show edit/delete buttons only if the current user is the author
        if (sessionManager.isLoggedIn()) {
            int currentUserId = sessionManager.getCurrentUserId();
            btnEdit.setVisible(currentUserId == experience.getUserId());
            btnDelete.setVisible(currentUserId == experience.getUserId());
            
            // Check if user has already liked this experience
            userHasLiked = likeService.hasUserLiked(experience.getId(), currentUserId);
            updateLikeButton();
        } else {
            btnEdit.setVisible(false);
            btnDelete.setVisible(false);
        }
        
        // Update like and comment counts
        updateLikeCount();
        updateCommentCount();
        
        // Load comments
        loadComments();
    }
    
    private void updateLikeButton() {
        if (userHasLiked) {
            btnLike.setText("Liked");
            btnLike.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; " +
                            "-fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);");
        } else {
            btnLike.setText("Like");
            btnLike.setStyle("-fx-background-color: #307D91; -fx-text-fill: white; -fx-font-weight: bold; " +
                            "-fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);");
        }
        
        // Re-apply hover effect after style change
        addButtonHoverEffect(btnLike);
    }
    
    private void updateLikeCount() {
        int likeCount = likeService.getLikeCount(experience.getId());
        likeCountLabel.setText(likeCount + (likeCount == 1 ? " like" : " likes"));
    }
    
    private void updateCommentCount() {
        int commentCount = commentService.getByExperienceId(experience.getId()).size();
        commentCountLabel.setText(commentCount + (commentCount == 1 ? " comment" : " comments"));
    }
    
    private void loadComments() {
        commentsContainer.getChildren().clear();
        
        List<ExperienceComment> comments = commentService.getByExperienceId(experience.getId());
        
        if (comments.isEmpty()) {
            Label emptyLabel = new Label("No comments yet. Be the first to comment!");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d; -fx-padding: 20 0 0 0;");
            commentsContainer.getChildren().add(emptyLabel);
            return;
        }
        
        for (ExperienceComment comment : comments) {
            createCommentUI(comment);
        }
    }
    
    private void createCommentUI(ExperienceComment comment) {
        // Create comment container
        VBox commentBox = new VBox(5);
        commentBox.setPadding(new Insets(10));
        commentBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // User info and date
        Optional<User> user = userService.getUserById(comment.getUserId());
        String userName = user.map(u -> u.getNom() + " " + u.getPrenom()).orElse("Unknown User");
        
        Label userLabel = new Label(userName);
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #307D91;");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        Label dateLabel = new Label(comment.getDatePosted().format(formatter));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        
        HBox headerBox = new HBox(10, userLabel, dateLabel);
        
        // Comment text
        Label commentTextLabel = new Label(comment.getContent());
        commentTextLabel.setWrapText(true);
        commentTextLabel.setStyle("-fx-text-fill: #212529;");
        
        // Add delete button if user is author or if user is admin
        if (sessionManager.isLoggedIn() && 
            (sessionManager.getCurrentUserId() == comment.getUserId() || sessionManager.isAdmin())) {
            Button deleteButton = new Button("Delete");
            // If admin is deleting someone else's comment, make it clear
            if (sessionManager.isAdmin() && sessionManager.getCurrentUserId() != comment.getUserId()) {
                deleteButton.setText("Delete (Admin)");
                deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc3545; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else {
                deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc3545; -fx-font-size: 11px;");
            }
            deleteButton.setOnAction(e -> deleteComment(comment));
            headerBox.getChildren().add(deleteButton);
        }
        
        // Add all elements to comment box
        commentBox.getChildren().addAll(headerBox, commentTextLabel);
        
        // Add to comments container
        commentsContainer.getChildren().add(commentBox);
    }
    
    private void deleteComment(ExperienceComment comment) {
        // Get the comment author's name for admin deletions
        String authorName = "this";
        if (sessionManager.isAdmin() && sessionManager.getCurrentUserId() != comment.getUserId()) {
            Optional<User> author = userService.getUserById(comment.getUserId());
            authorName = author.map(u -> u.getNom() + " " + u.getPrenom() + "'s").orElse("this");
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Comment");
        confirm.setHeaderText("Delete Comment");
        
        if (sessionManager.isAdmin() && sessionManager.getCurrentUserId() != comment.getUserId()) {
            confirm.setContentText("Are you sure you want to delete " + authorName + " comment? This action cannot be undone.");
        } else {
            confirm.setContentText("Are you sure you want to delete this comment?");
        }
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = commentService.delete(comment.getId());
            if (success) {
                loadComments();
                updateCommentCount();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not delete comment", "Please try again later");
            }
        }
    }
    
    @FXML
    private void onLikeClicked() {
        if (!sessionManager.isLoggedIn()) {
            showLoginPrompt();
            return;
        }
        
        int userId = sessionManager.getCurrentUserId();
        
        if (userHasLiked) {
            // Unlike
            boolean success = likeService.removeLike(userId, experience.getId());
            if (success) {
                userHasLiked = false;
                updateLikeButton();
                updateLikeCount();
            }
        } else {
            // Like
            boolean success = likeService.addLike(userId, experience.getId());
            if (success) {
                userHasLiked = true;
                updateLikeButton();
                updateLikeCount();
            }
        }
    }
    
    @FXML
    private void onCommentClicked() {
        if (!sessionManager.isLoggedIn()) {
            showLoginPrompt();
            return;
        }
        
        String commentText = commentField.getText().trim();
        if (commentText.isEmpty()) {
            return;
        }
        
        ExperienceComment comment = new ExperienceComment();
        comment.setExperienceId(experience.getId());
        comment.setUserId(sessionManager.getCurrentUserId());
        comment.setContent(commentText);
        
        boolean success = commentService.add(comment);
        if (success) {
            commentField.clear();
            loadComments();
            updateCommentCount();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not add comment", "Please try again later");
        }
    }
    
    @FXML
    private void onEditClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/badro/experienceForm.fxml"));
            Parent root = loader.load();
            
            ExperienceFormController controller = loader.getController();
            controller.setExperience(experience);
            controller.setOnExperienceAddedCallback(() -> {
                // Reload the experience
                Experience updatedExperience = experienceService.getById(experience.getId());
                if (updatedExperience != null) {
                    setExperience(updatedExperience);
                }
            });
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Experience");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open edit form", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onDeleteClicked() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Experience");
        confirmAlert.setContentText("Are you sure you want to delete this experience? This action cannot be undone.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = experienceService.delete(experience.getId());
            if (success) {
                if (onDeleteCallback != null) {
                    onDeleteCallback.run();
                }
                
                // Close this window
                ((Stage) btnDelete.getScene().getWindow()).close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not delete experience", "Please try again later");
            }
        }
    }
    
    @FXML
    private void onBackClicked() {
        ((Stage) btnBack.getScene().getWindow()).close();
    }
    
    @FXML
    private void onTranslateClicked() {
        Language targetLanguage = translateComboBox.getSelectionModel().getSelectedItem();
        if (targetLanguage == null) {
            return;
        }
        
        // Don't translate again if already translated to this language
        if (isTranslated && targetLanguage == currentLanguage) {
            return;
        }
        
        // Show the reset button when translation occurs
        btnResetTranslation.setVisible(true);
        
        // Create progress indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(-1);
        progressIndicator.setPrefSize(20, 20);
        translationBar.getChildren().add(2, progressIndicator);
        
        // Start translating
        CompletableFuture<Void> titleFuture = translationService.translateAsync(originalTitle, Language.AUTO_DETECT, targetLanguage)
            .thenAccept(translated -> Platform.runLater(() -> titleLabel.setText(translated)));
            
        CompletableFuture<Void> descriptionFuture = translationService.translateAsync(originalDescription, Language.AUTO_DETECT, targetLanguage)
            .thenAccept(translated -> Platform.runLater(() -> descriptionArea.setText(translated)));
            
        CompletableFuture<Void> destinationFuture = CompletableFuture.completedFuture(null);
        if (originalDestination != null && !originalDestination.isEmpty() && destinationLabel.isVisible()) {
            destinationFuture = translationService.translateAsync(originalDestination, Language.AUTO_DETECT, targetLanguage)
                .thenAccept(translated -> Platform.runLater(() -> destinationLabel.setText("Destination: " + translated)));
        }
        
        // When all translations complete
        CompletableFuture.allOf(titleFuture, descriptionFuture, destinationFuture)
            .thenRun(() -> Platform.runLater(() -> {
                // Remove progress indicator
                translationBar.getChildren().remove(progressIndicator);
                
                // Update state
                isTranslated = true;
                currentLanguage = targetLanguage;
                
                // Show success message
                showTranslationSuccessMessage();
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    translationBar.getChildren().remove(progressIndicator);
                    showAlert(Alert.AlertType.ERROR, "Translation Error", 
                              "Could not translate content", 
                              "Please try again later: " + ex.getMessage());
                });
                return null;
            });
    }
    
    @FXML
    private void onResetTranslationClicked() {
        // Reset title, description, and destination
        titleLabel.setText(originalTitle);
        descriptionArea.setText(originalDescription);
        if (originalDestination != null && !originalDestination.isEmpty()) {
            destinationLabel.setText("Destination: " + originalDestination);
        }
        
        // Reset state
        isTranslated = false;
        btnResetTranslation.setVisible(false);
        translateComboBox.getSelectionModel().select(Language.ENGLISH);
    }
    
    private void showTranslationSuccessMessage() {
        // Use a simple alert instead of complex animation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Translation Complete");
        alert.setHeaderText(null);
        alert.setContentText("The content has been successfully translated to " + currentLanguage.getDisplayName());
        alert.show();
        
        // Auto-close after 2 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> alert.close());
        delay.play();
    }
    
    private void showLoginPrompt() {
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
                Stage stage = (Stage) btnLike.getScene().getWindow();
                stage.setScene(new Scene(loginRoot));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
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
} 