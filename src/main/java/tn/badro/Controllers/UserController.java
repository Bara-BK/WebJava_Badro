package tn.badro.Controllers;

import tn.badro.entities.User;
import tn.badro.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class UserController implements Initializable {
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> nomColumn;
    @FXML
    private TableColumn<User, String> prenomColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> numtlfColumn;
    @FXML
    private TableColumn<User, Integer> ageColumn;
    @FXML
    private TableColumn<User, String> rolesColumn;

    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField numtlfField;
    @FXML
    private TextField ageField;
    @FXML
    private TextField rolesField;

    private final UserService userService = new UserService();
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadUsers();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        numtlfColumn.setCellValueFactory(new PropertyValueFactory<>("numtlf"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        rolesColumn.setCellValueFactory(new PropertyValueFactory<>("roles"));
    }

    private void loadUsers() {
        userList.clear();
        userList.addAll(userService.getAllUsers());
        userTable.setItems(userList);
    }

    @FXML
    private void handleAddUser() {
        try {
            if (validateFields()) {
                User user = new User();
                user.setNom(nomField.getText());
                user.setPrenom(prenomField.getText());
                user.setEmail(emailField.getText());
                user.setPassword(passwordField.getText());
                user.setNumtlf(numtlfField.getText());
                user.setAge(Integer.parseInt(ageField.getText()));
                user.setRoles(rolesField.getText());

                userService.createUser(user);
                clearFields();
                loadUsers();
                showAlert(Alert.AlertType.INFORMATION, "Success", "User added successfully!");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid age!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add user: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a user to update!");
            return;
        }

        try {
            if (validateFields()) {
                selectedUser.setNom(nomField.getText());
                selectedUser.setPrenom(prenomField.getText());
                selectedUser.setEmail(emailField.getText());
                selectedUser.setPassword(passwordField.getText());
                selectedUser.setNumtlf(numtlfField.getText());
                selectedUser.setAge(Integer.parseInt(ageField.getText()));
                selectedUser.setRoles(rolesField.getText());

                userService.updateUser(selectedUser.getId(), selectedUser);
                clearFields();
                loadUsers();
                showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully!");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid age!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a user to delete!");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete User");
        confirmDialog.setContentText("Are you sure you want to delete this user?");

        if (confirmDialog.showAndWait().get() == ButtonType.OK) {
            userService.deleteUser(selectedUser.getId());
            clearFields();
            loadUsers();
            showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully!");
        }
    }

    @FXML
    private void handleSelectUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            nomField.setText(selectedUser.getNom());
            prenomField.setText(selectedUser.getPrenom());
            emailField.setText(selectedUser.getEmail());
            passwordField.setText(selectedUser.getPassword());
            numtlfField.setText(selectedUser.getNumtlf());
            ageField.setText(String.valueOf(selectedUser.getAge()));
            rolesField.setText(selectedUser.getRoles());
        }
    }

    @FXML
    private void clearFields() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        passwordField.clear();
        numtlfField.clear();
        ageField.clear();
        rolesField.clear();
        userTable.getSelectionModel().clearSelection();
    }

    private boolean validateFields() {
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() ||
            emailField.getText().isEmpty() || passwordField.getText().isEmpty() ||
            numtlfField.getText().isEmpty() || ageField.getText().isEmpty() ||
            rolesField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required!");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
} 