package tn.badro.Controllers;

import tn.badro.entities.User;
import tn.badro.services.UserService;
import tn.badro.services.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import java.io.IOException;
import tn.badro.Controllers.DashboardController;

public class ProfileController {
    @FXML private ImageView profileImage;
    @FXML private Label profileName;
    @FXML private Label profileRole;
    @FXML private Label userDisplayName;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField ageField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;
    @FXML private HBox userInfoSection;

    private User currentUser;
    private final UserService userService = new UserService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void initialize() {
        // Load user data from session
        String sessionId = getSessionId();
        if (sessionId != null) {
            currentUser = sessionManager.getCurrentUser(sessionId);
            if (currentUser != null) {
                loadUserData();
            }
        }
    }

    private void loadUserData() {
        profileName.setText(currentUser.getNom() + " " + currentUser.getPrenom());
        profileRole.setText(currentUser.getRoles());
        userDisplayName.setText(currentUser.getNom() + " " + currentUser.getPrenom());
        
        firstNameField.setText(currentUser.getPrenom());
        lastNameField.setText(currentUser.getNom());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getNumtlf());
        ageField.setText(String.valueOf(currentUser.getAge()));
    }

    @FXML
    private void handleSaveProfile() {
        try {
            // Validate current password if changing password
            if (!newPasswordField.getText().isEmpty()) {
                if (!validatePasswordChange()) {
                    return;
                }
            }

            // Update user data
            currentUser.setPrenom(firstNameField.getText());
            currentUser.setNom(lastNameField.getText());
            currentUser.setEmail(emailField.getText());
            currentUser.setNumtlf(phoneField.getText());
            currentUser.setAge(Integer.parseInt(ageField.getText()));

            // Update password if changed
            if (!newPasswordField.getText().isEmpty()) {
                currentUser.setPassword(newPasswordField.getText());
            }

            // Save changes to database
            userService.updateUser(currentUser);
            
            // Update session
            String sessionId = getSessionId();
            if (sessionId != null) {
                sessionManager.invalidateSession(sessionId);
                sessionManager.createSession(currentUser);
            }

            statusLabel.setText("Profile updated successfully!");
            statusLabel.setStyle("-fx-text-fill: #28a745;");
        } catch (Exception e) {
            statusLabel.setText("Error updating profile: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #DC3545;");
        }
    }

    private boolean validatePasswordChange() {
        if (!currentPasswordField.getText().equals(currentUser.getPassword())) {
            statusLabel.setText("Current password is incorrect");
            return false;
        }

        if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            statusLabel.setText("New passwords do not match");
            return false;
        }

        if (newPasswordField.getText().length() < 6) {
            statusLabel.setText("Password must be at least 6 characters long");
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        try {
            if (currentUser != null && "admin".equals(currentUser.getRoles())) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/dashboard.fxml"));
                Parent root = loader.load();
                DashboardController controller = loader.getController();
                controller.setUserInfo(currentUser);
                
                Stage stage = (Stage) statusLabel.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/mainMenu.fxml"));
                Parent root = loader.load();
                MainMenuController controller = loader.getController();
                controller.setUserInfo(currentUser);
                
                Stage stage = (Stage) statusLabel.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
            }
        } catch (IOException e) {
            statusLabel.setText("Error returning to previous menu: " + e.getMessage());
        }
    }

    private String getSessionId() {
        // In a real application, you would get this from a secure cookie or session storage
        // For now, we'll use a simple implementation
        return currentUser != null ? currentUser.getEmail() : null;
    }

    public void setUserInfo(User user) {
        this.currentUser = user;
        if (user != null) {
            loadUserData();
        }
    }
} 