package tn.badro.Controllers;

import tn.badro.services.UserService;
import tn.badro.entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.regex.Pattern;

public class RegistrationController {
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField numtlfField;
    @FXML private TextField ageField;
    @FXML private ComboBox<String> rolesComboBox;
    @FXML private Label statusLabel;
    @FXML private Button minimizeButton;
    @FXML private Button maximizeButton;
    @FXML private Button closeButton;
    @FXML private Label userDisplayName;

    private Stage stage;
    private boolean isMaximized = false;
    private User currentUser;

    @FXML
    public void initialize() {
        // Set the items for the ComboBox
        ObservableList<String> roles = FXCollections.observableArrayList("user", "admin");
        rolesComboBox.setItems(roles);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void minimizeWindow() {
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    @FXML
    private void maximizeWindow() {
        if (stage != null) {
            if (isMaximized) {
                stage.setMaximized(false);
                maximizeButton.getGraphic().setStyle("-fx-text-fill: white;");
            } else {
                stage.setMaximized(true);
                maximizeButton.getGraphic().setStyle("-fx-text-fill: #2ecc71;");
            }
            isMaximized = !isMaximized;
        }
    }

    @FXML
    private void closeWindow() {
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    private void handleRegister() {
        clearStatus();
        
        // Check for empty fields
        if (isFieldEmpty(nomField, "First Name") || isFieldEmpty(prenomField, "Last Name") ||
                isFieldEmpty(emailField, "Email") || isFieldEmpty(passwordField, "Password") ||
                isFieldEmpty(numtlfField, "Phone Number") || isFieldEmpty(ageField, "Age") ||
                rolesComboBox.getValue() == null) {
            return;
        }

        String email = emailField.getText();
        String password = passwordField.getText();
        String phone = numtlfField.getText();
        String ageText = ageField.getText();
        int age;

        // Email validation
        if (!isValidEmail(email)) {
            showError("Invalid Email", "Please enter a valid email address.");
            return;
        }

        // Password validation
        if (!isValidPassword(password)) {
            showError("Invalid Password", "Password must contain at least one uppercase letter, one number, and be more than 8 characters long.");
            return;
        }

        // Phone validation
        if (!isValidPhone(phone)) {
            showError("Invalid Phone Number", "Phone number must contain exactly 8 digits.");
            return;
        }

        // Age validation
        try {
            age = Integer.parseInt(ageText);
            if (age < 18 || age > 100) {
                showError("Invalid Age", "Age must be between 18 and 100.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Invalid Age", "Age must be a number.");
            return;
        }

        // Create and save the user
        User user = new User();
        user.setNom(nomField.getText());
        user.setPrenom(prenomField.getText());
        user.setEmail(email);
        user.setPassword(password);
        user.setNumtlf(phone);
        user.setAge(age);
        user.setRoles(rolesComboBox.getValue());

        UserService userService = new UserService();
        try {
            userService.addUser(user);
            showSuccess("Registration successful! You can now log in.");
            showLogin(null); // Redirect to login page after successful registration
        } catch (Exception e) {
            showError("Registration Failed", e.getMessage());
        }
    }

    private void clearStatus() {
        statusLabel.setText("");
    }

    private void showError(String title, String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #2ecc71;");
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

    private boolean isValidPassword(String password) {
        return password.length() > 8 &&
                Pattern.compile("[A-Z]").matcher(password).find() &&
                Pattern.compile("[0-9]").matcher(password).find();
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("\\d{8}");
    }

    @FXML
    private void showLogin(ActionEvent event) {
        try {
            Stage stage = (Stage) ((event != null ? (Button) event.getSource() : nomField)).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/login.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
        } catch (Exception e) {
            showError("Error", "Failed to load login page: " + e.getMessage());
        }
    }

    @FXML
    private void returnToMainMenu(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/mainMenu.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
        } catch (Exception e) {
            showError("Error", "Failed to return to main menu: " + e.getMessage());
        }
    }

    public void setUserInfo(User user) {
        this.currentUser = user;
        if (user != null && userDisplayName != null) {
            userDisplayName.setText(user.getNom() + " " + user.getPrenom());
        }
    }
}