package tn.badro.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.badro.entities.Preferences;
import tn.badro.entities.User;
import tn.badro.services.PreferencesService;
import tn.badro.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;

import java.sql.SQLException;
import java.util.Optional;
import javafx.scene.control.ToggleGroup;

public class PreferenceAdminController {
    
    @FXML private TextField climatField;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private TextField domainField;
    @FXML private RadioButton englishRadio;
    @FXML private RadioButton frenchRadio;
    @FXML private RadioButton arabicRadio;
    @FXML private RadioButton onlineRadio;
    @FXML private RadioButton inPersonRadio;
    @FXML private TextField universityTypeField;
    @FXML private TextField culturalActivitiesField;
    @FXML private TextField levelField;
    @FXML private TextField userIdField;
    
    @FXML private TableView<Preferences> preferencesTable;
    @FXML private TableColumn<Preferences, Integer> idColumn;
    @FXML private TableColumn<Preferences, String> climatColumn;
    @FXML private TableColumn<Preferences, String> countryColumn;
    @FXML private TableColumn<Preferences, String> domainColumn;
    @FXML private TableColumn<Preferences, String> languageColumn;
    @FXML private TableColumn<Preferences, String> teachingModeColumn;
    @FXML private TableColumn<Preferences, String> universityTypeColumn;
    @FXML private TableColumn<Preferences, String> culturalActivitiesColumn;
    @FXML private TableColumn<Preferences, String> levelColumn;
    @FXML private TableColumn<Preferences, Integer> userColumn;
    
    private PreferencesService preferencesService;
    private UserService userService;
    private ToggleGroup languageGroup;
    private ToggleGroup teachingModeGroup;
    private int currentPreferenceId = -1;
    
    @FXML
    public void initialize() {
        preferencesService = new PreferencesService();
        userService = new UserService();
        
        // Initialize toggle groups
        languageGroup = new ToggleGroup();
        englishRadio.setToggleGroup(languageGroup);
        frenchRadio.setToggleGroup(languageGroup);
        arabicRadio.setToggleGroup(languageGroup);
        
        teachingModeGroup = new ToggleGroup();
        onlineRadio.setToggleGroup(teachingModeGroup);
        inPersonRadio.setToggleGroup(teachingModeGroup);
        
        // Initialize country dropdown
        countryComboBox.getItems().addAll("France", "Germany", "United States", "Japan", "Canada", "Australia", 
                                         "United Kingdom", "Spain", "Italy", "China", "South Korea", "Brazil");
        
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        climatColumn.setCellValueFactory(new PropertyValueFactory<>("climat_pref"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
        domainColumn.setCellValueFactory(new PropertyValueFactory<>("domain"));
        languageColumn.setCellValueFactory(new PropertyValueFactory<>("preferred_language"));
        teachingModeColumn.setCellValueFactory(new PropertyValueFactory<>("teaching_mode"));
        universityTypeColumn.setCellValueFactory(new PropertyValueFactory<>("university_type"));
        culturalActivitiesColumn.setCellValueFactory(new PropertyValueFactory<>("cultural_activities"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("language_level"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("id_user"));
        
        loadPreferences();
    }
    
    private void loadPreferences() {
        try {
            ObservableList<Preferences> preferences = FXCollections.observableArrayList(preferencesService.recuperer());
            preferencesTable.setItems(preferences);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load preferences: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSelectPreference(MouseEvent event) {
        if (event.getClickCount() == 1) {
            Preferences selectedPreference = preferencesTable.getSelectionModel().getSelectedItem();
            if (selectedPreference != null) {
                currentPreferenceId = selectedPreference.getId();
                fillFormFields(selectedPreference);
            }
        }
    }
    
    private void fillFormFields(Preferences preference) {
        climatField.setText(preference.getClimat_pref());
        countryComboBox.setValue(preference.getCountry());
        domainField.setText(preference.getDomain());
        universityTypeField.setText(preference.getUniversity_type());
        culturalActivitiesField.setText(preference.getCultural_activities());
        levelField.setText(preference.getLanguage_level());
        userIdField.setText(String.valueOf(preference.getId_user()));
        
        // Set language radio button
        if (preference.getPreferred_language().equalsIgnoreCase("English")) {
            englishRadio.setSelected(true);
        } else if (preference.getPreferred_language().equalsIgnoreCase("French")) {
            frenchRadio.setSelected(true);
        } else if (preference.getPreferred_language().equalsIgnoreCase("Arabic")) {
            arabicRadio.setSelected(true);
        }
        
        // Set teaching mode radio button
        if (preference.getTeaching_mode().equalsIgnoreCase("Online")) {
            onlineRadio.setSelected(true);
        } else if (preference.getTeaching_mode().equalsIgnoreCase("In-person classes")) {
            inPersonRadio.setSelected(true);
        }
    }
    
    @FXML
    private void handleAddPreference(ActionEvent event) {
        if (!validateFields()) {
            return;
        }
        
        try {
            Preferences newPreference = new Preferences();
            newPreference.setClimat_pref(climatField.getText());
            newPreference.setCountry(countryComboBox.getValue());
            newPreference.setDomain(domainField.getText());
            newPreference.setPreferred_language(getSelectedLanguage());
            newPreference.setTeaching_mode(getSelectedTeachingMode());
            newPreference.setUniversity_type(universityTypeField.getText());
            newPreference.setCultural_activities(culturalActivitiesField.getText());
            newPreference.setLanguage_level(levelField.getText());
            newPreference.setId_user(Integer.parseInt(userIdField.getText()));
            
            // Verify user exists before adding preference
            Optional<User> user = userService.getUserById(Integer.parseInt(userIdField.getText()));
            if (!user.isPresent()) {
                showAlert(Alert.AlertType.ERROR, "Error", "User ID doesn't exist. Please enter a valid user ID.");
                return;
            }
            
            preferencesService.ajouter(newPreference);
            clearFields();
            loadPreferences();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Preference added successfully.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add preference: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUpdatePreference(ActionEvent event) {
        if (currentPreferenceId == -1) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a preference to update.");
            return;
        }
        
        if (!validateFields()) {
            return;
        }
        
        try {
            Preferences preference = new Preferences();
            preference.setId(currentPreferenceId);
            preference.setClimat_pref(climatField.getText());
            preference.setCountry(countryComboBox.getValue());
            preference.setDomain(domainField.getText());
            preference.setPreferred_language(getSelectedLanguage());
            preference.setTeaching_mode(getSelectedTeachingMode());
            preference.setUniversity_type(universityTypeField.getText());
            preference.setCultural_activities(culturalActivitiesField.getText());
            preference.setLanguage_level(levelField.getText());
            preference.setId_user(Integer.parseInt(userIdField.getText()));
            
            // Verify user exists before updating preference
            Optional<User> user = userService.getUserById(Integer.parseInt(userIdField.getText()));
            if (!user.isPresent()) {
                showAlert(Alert.AlertType.ERROR, "Error", "User ID doesn't exist. Please enter a valid user ID.");
                return;
            }
            
            preferencesService.modifier(preference);
            clearFields();
            loadPreferences();
            currentPreferenceId = -1;
            showAlert(Alert.AlertType.INFORMATION, "Success", "Preference updated successfully.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update preference: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeletePreference(ActionEvent event) {
        Preferences selectedPreference = preferencesTable.getSelectionModel().getSelectedItem();
        if (selectedPreference == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a preference to delete.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete this preference?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                preferencesService.supprimer(selectedPreference);
                loadPreferences();
                clearFields();
                currentPreferenceId = -1;
                showAlert(Alert.AlertType.INFORMATION, "Success", "Preference deleted successfully.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete preference: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void clearFields() {
        climatField.clear();
        countryComboBox.setValue(null);
        domainField.clear();
        universityTypeField.clear();
        culturalActivitiesField.clear();
        levelField.clear();
        userIdField.clear();
        englishRadio.setSelected(true);
        onlineRadio.setSelected(true);
        currentPreferenceId = -1;
    }
    
    private String getSelectedLanguage() {
        if (englishRadio.isSelected()) return "English";
        if (frenchRadio.isSelected()) return "French";
        if (arabicRadio.isSelected()) return "Arabic";
        return "English"; // Default
    }
    
    private String getSelectedTeachingMode() {
        if (onlineRadio.isSelected()) return "Online";
        if (inPersonRadio.isSelected()) return "In-person classes";
        return "Online"; // Default
    }
    
    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();
        
        if (climatField.getText().trim().isEmpty()) {
            errors.append("- Climat is required\n");
        }
        
        if (countryComboBox.getValue() == null || countryComboBox.getValue().trim().isEmpty()) {
            errors.append("- Country is required\n");
        }
        
        if (domainField.getText().trim().isEmpty()) {
            errors.append("- Domain is required\n");
        }
        
        if (universityTypeField.getText().trim().isEmpty()) {
            errors.append("- University Type is required\n");
        }
        
        if (culturalActivitiesField.getText().trim().isEmpty()) {
            errors.append("- Cultural Activities is required\n");
        }
        
        if (levelField.getText().trim().isEmpty()) {
            errors.append("- Language Level is required\n");
        }
        
        if (userIdField.getText().trim().isEmpty()) {
            errors.append("- User ID is required\n");
        } else {
            try {
                Integer.parseInt(userIdField.getText().trim());
            } catch (NumberFormatException e) {
                errors.append("- User ID must be a number\n");
            }
        }
        
        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please correct the following errors:\n" + errors.toString());
            return false;
        }
        
        return true;
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 