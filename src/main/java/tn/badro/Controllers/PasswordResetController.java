package tn.badro.Controllers;

import tn.badro.entities.User;
import tn.badro.services.PasswordResetService;
import tn.badro.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class PasswordResetController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField tokenField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    private final PasswordResetService passwordResetService = new PasswordResetService();
    private final UserService userService = new UserService();

    @FXML
    private void handleRequestReset() {
        String email = emailField.getText();
        if (email == null || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter your email.");
            return;
        }
        User user = userService.findByEmail(email);
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user found with this email.");
            return;
        }
        passwordResetService.createResetToken(user);
        showAlert(Alert.AlertType.INFORMATION, "Success", "Password reset email sent if the email exists.");

        // Navigate to reset password screen
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/resetPassword.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load reset password screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleResetPassword() {
        String code = tokenField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (code == null || code.isEmpty() || newPassword == null || newPassword.isEmpty() || confirmPassword == null || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
            return;
        }
        boolean success = passwordResetService.resetPassword(code, newPassword);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Password has been reset successfully.");
            // Redirect to login screen after successful reset
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) tokenField.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load login screen: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid or expired verification code.");
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/login.fxml"));
            Parent root = loader.load();
            Stage stage;
            
            // Check which field is available based on which screen we're on
            if (emailField != null && emailField.getScene() != null) {
                stage = (Stage) emailField.getScene().getWindow();
            } else if (tokenField != null && tokenField.getScene() != null) {
                stage = (Stage) tokenField.getScene().getWindow();
            } else {
                // If neither is available, throw an exception with helpful message
                throw new RuntimeException("Can't determine current scene - no UI controls available");
            }
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load login screen: " + e.getMessage());
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleSendEmail() {
        String email = emailField.getText();
        if (email == null || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Email Required", "Please enter your email address.");
            return;
        }
        
        // Generate and send reset token
        PasswordResetService resetService = new PasswordResetService();
        boolean success = resetService.generateResetToken(email);
        
        if (success) {
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, 
                      "Reset Token Sent", 
                      "A password reset token has been sent to your email address: " + email + "\n\n" +
                      "Please check your inbox (and spam folder) for the reset instructions.");
            
            // Navigate to the token entry screen
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/resetPassword.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) emailField.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Navigation Failed", "Could not navigate to reset password screen: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Reset Failed", "No account found with this email address or an error occurred sending the email.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
