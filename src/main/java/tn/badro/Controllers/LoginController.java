package tn.badro.Controllers;

import tn.badro.services.UserService;
import tn.badro.entities.User;
import tn.badro.services.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.regex.Pattern;
import javafx.scene.Parent;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Label userDisplayName;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        try {
            if (isFieldEmpty(emailField, "Email") || isFieldEmpty(passwordField, "Password")) {
                return;
            }

            String email = emailField.getText();
            String password = passwordField.getText();

            User user = userService.authenticateUser(email, password);
            if (user != null) {
                // Create session and set current user
                String sessionId = SessionManager.getInstance().createSession(user);
                SessionManager.getInstance().setCurrentUser(user);
                
                Stage stage = (Stage) emailField.getScene().getWindow();
                
                if (user.getRoles().equals("admin")) {
                    // Load dashboard for admin
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/dashboard.fxml"));
                    Parent root = loader.load();
                    
                    DashboardController controller = loader.getController();
                    controller.setStage(stage);
                    controller.setUserInfo(user);
                    
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                } else {
                    // Load main menu for regular user
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/mainMenu.fxml"));
                    Parent root = loader.load();
                    MainMenuController mainMenuController = loader.getController();
                    mainMenuController.setUserInfo(user);
                    
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                }
            } else {
                statusLabel.setText("Invalid email or password");
            }
        } catch (Exception e) {
            statusLabel.setText("Error during login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showResetPasswordRequest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/resetPasswordRequest.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace(); // Print full stack trace
            statusLabel.setText("Failed to load reset password screen: " + e.getMessage());
            
            // Show a more detailed error alert
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to load reset password screen");
            alert.setContentText("Error: " + e.getMessage() + "\n\nPlease check the application logs for more details.");
            alert.showAndWait();
        }
    }

    @FXML
    private void showRegister(ActionEvent event) throws Exception {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/registration.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
    }

    @FXML
    private void returnToMainMenu(ActionEvent event) throws Exception {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/mainMenu.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
    }

    private boolean isFieldEmpty(TextField field, String fieldName) {
        if (field.getText().trim().isEmpty()) {
            showError("Empty Field", fieldName + " is required.");
            return true;
        }
        return false;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }

    private void showError(String title, String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #2ecc71;");
    }

    private void clearStatus() {
        statusLabel.setText("");
    }

    public void setUserInfo(User user) {
        if (user != null && userDisplayName != null) {
            userDisplayName.setText(user.getNom() + " " + user.getPrenom());
        }
    }
}