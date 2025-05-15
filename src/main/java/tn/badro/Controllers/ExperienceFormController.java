package tn.badro.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.badro.entities.Experience;
import tn.badro.entities.User;
import tn.badro.services.ExperienceService;
import tn.badro.services.SessionManager;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.UUID;

public class ExperienceFormController implements Initializable {
    @FXML
    private Label formTitle;
    
    @FXML
    private TextField titleField;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private TextField locationField;
    
    @FXML
    private TextField destinationField;
    
    @FXML
    private TextField imagePathField;
    
    @FXML
    private Button btnBrowse;
    
    @FXML
    private Button btnSave;
    
    @FXML
    private Button btnCancel;
    
    private final ExperienceService experienceService = new ExperienceService();
    private final SessionManager sessionManager = SessionManager.getInstance();
    
    private Experience editingExperience;
    private File selectedImageFile;
    private Runnable onExperienceAddedCallback;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            showLoginRequiredAlert();
            return;
        }
        
        // Validation
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateForm();
        });
        
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            validateForm();
        });
        
        // Initial validation
        validateForm();
    }
    
    public void setExperience(Experience experience) {
        this.editingExperience = experience;
        formTitle.setText("Edit Experience");
        btnSave.setText("Update Experience");
        
        // Fill form with existing data
        titleField.setText(experience.getTitle());
        descriptionArea.setText(experience.getDescription());
        locationField.setText(experience.getLocation());
        destinationField.setText(experience.getDestination());
        
        if (experience.getImagePath() != null && !experience.getImagePath().isEmpty()) {
            imagePathField.setText(experience.getImagePath());
        }
    }
    
    public void setOnExperienceAddedCallback(Runnable callback) {
        this.onExperienceAddedCallback = callback;
    }
    
    @FXML
    private void onBrowseClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File file = fileChooser.showOpenDialog(btnBrowse.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            imagePathField.setText(file.getAbsolutePath());
        }
    }
    
    @FXML
    private void onSaveClicked() {
        if (!validateForm()) {
            return;
        }
        
        try {
            String title = titleField.getText().trim();
            String description = descriptionArea.getText().trim();
            String location = locationField.getText().trim();
            String destination = destinationField.getText().trim();
            
            String imagePath = null;
            if (selectedImageFile != null) {
                // Copy the image to a permanent location in the app's directory
                String uniqueFileName = UUID.randomUUID().toString() + getFileExtension(selectedImageFile.getName());
                Path destinationPath = Paths.get("src/main/resources/tn/badro/images/experiences/" + uniqueFileName);
                
                // Create directory if it doesn't exist
                Files.createDirectories(destinationPath.getParent());
                
                // Copy the file
                Files.copy(selectedImageFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                
                imagePath = destinationPath.toString();
            } else if (editingExperience != null && editingExperience.getImagePath() != null) {
                imagePath = editingExperience.getImagePath();
            }
            
            if (editingExperience == null) {
                // Create new experience
                Experience experience = new Experience();
                experience.setTitle(title);
                experience.setDescription(description);
                experience.setLocation(location);
                experience.setDestination(destination);
                experience.setDatePosted(LocalDateTime.now());
                experience.setUserId(sessionManager.getCurrentUserId());
                experience.setImagePath(imagePath);
                
                boolean success = experienceService.add(experience);
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Experience shared successfully", 
                              "Your experience has been shared with others.");
                    
                    if (onExperienceAddedCallback != null) {
                        onExperienceAddedCallback.run();
                    }
                    
                    closeWindow();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not save experience", 
                              "Please try again later.");
                }
            } else {
                // Update existing experience
                editingExperience.setTitle(title);
                editingExperience.setDescription(description);
                editingExperience.setLocation(location);
                editingExperience.setDestination(destination);
                editingExperience.setImagePath(imagePath);
                
                boolean success = experienceService.update(editingExperience);
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Experience updated successfully", 
                              "Your experience has been updated.");
                    
                    if (onExperienceAddedCallback != null) {
                        onExperienceAddedCallback.run();
                    }
                    
                    closeWindow();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not update experience", 
                              "Please try again later.");
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onCancelClicked() {
        closeWindow();
    }
    
    private boolean validateForm() {
        boolean isValid = !titleField.getText().trim().isEmpty() && 
                         !descriptionArea.getText().trim().isEmpty();
        
        btnSave.setDisable(!isValid);
        
        return isValid;
    }
    
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }
    
    private void closeWindow() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showLoginRequiredAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Login Required");
        alert.setHeaderText("Authentication Required");
        alert.setContentText("You must be logged in to share experiences.");
        
        ButtonType loginButton = new ButtonType("Login");
        ButtonType cancelButton = ButtonType.CANCEL;
        
        alert.getButtonTypes().setAll(loginButton, cancelButton);
        
        alert.showAndWait().ifPresent(result -> {
            if (result == loginButton) {
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/Desktop/login.fxml"));
                    javafx.scene.Parent loginRoot = loader.load();
                    javafx.stage.Stage stage = (javafx.stage.Stage) (formTitle != null ? formTitle.getScene().getWindow() : new javafx.stage.Stage());
                    stage.setScene(new javafx.scene.Scene(loginRoot));
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Close this window
                closeWindow();
            }
        });
    }
} 