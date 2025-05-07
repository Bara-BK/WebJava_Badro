package tn.badro.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.time.LocalDate;
import java.util.UUID;

import tn.badro.entities.User;
import tn.badro.entities.University;
import tn.badro.entities.Programme;
import tn.badro.entities.ProgramApplication;
import tn.badro.services.ProgramApplicationService;

public class ProgramApplicationController {
    @FXML private Label universityNameLabel;
    @FXML private Label programNameLabel;
    @FXML private TextArea motivationField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private CheckBox agreementCheckbox;
    @FXML private Button submitButton;
    @FXML private VBox applicationFormContainer;
    
    private User currentUser;
    private University selectedUniversity;
    private Programme selectedProgram;
    
    private ProgramApplicationService applicationService = new ProgramApplicationService();
    
    @FXML
    public void initialize() {
        // Set up date pickers
        startDatePicker.setValue(LocalDate.now().plusMonths(1));
        endDatePicker.setValue(LocalDate.now().plusMonths(6));
        
        // Enable submit button only when checkbox is checked
        submitButton.disableProperty().bind(agreementCheckbox.selectedProperty().not());
    }
    
    /**
     * Initialize controller with data
     */
    public void initData(User user, University university, Programme program) {
        this.currentUser = user;
        this.selectedUniversity = university;
        this.selectedProgram = program;
        
        universityNameLabel.setText(university.getName());
        programNameLabel.setText(program.getName());
    }
    
    @FXML
    private void handleSubmitApplication(ActionEvent event) {
        if (!validateForm()) {
            return;
        }
        
        // Create application object
        ProgramApplication application = new ProgramApplication();
        application.setApplicationId(UUID.randomUUID().toString());
        application.setUserId(currentUser.getId());
        application.setProgrammeId(selectedProgram.getId());
        application.setMotivationLetter(motivationField.getText());
        application.setStartDate(startDatePicker.getValue());
        application.setEndDate(endDatePicker.getValue());
        application.setApplicationDate(LocalDate.now());
        application.setStatus("Pending");
        
        // Submit application
        try {
            applicationService.createProgramApplication(application);
            showSuccessAlert();
            closeStage();
        } catch (Exception e) {
            showErrorAlert(e.getMessage());
        }
    }
    
    private boolean validateForm() {
        StringBuilder errorMessages = new StringBuilder();
        
        if (motivationField.getText().trim().isEmpty()) {
            errorMessages.append("- Please provide a motivation letter.\n");
        } else if (motivationField.getText().length() < 100) {
            errorMessages.append("- Your motivation letter should be at least 100 characters.\n");
        }
        
        if (startDatePicker.getValue() == null) {
            errorMessages.append("- Please select a start date.\n");
        }
        
        if (endDatePicker.getValue() == null) {
            errorMessages.append("- Please select an end date.\n");
        } else if (startDatePicker.getValue() != null && 
                   endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            errorMessages.append("- The end date cannot be before the start date.\n");
        }
        
        if (!agreementCheckbox.isSelected()) {
            errorMessages.append("- You must agree to the terms and conditions.\n");
        }
        
        if (errorMessages.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Form");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errorMessages.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Application Submitted");
        alert.setHeaderText("Success!");
        alert.setContentText("Your application for " + selectedProgram.getName() + 
                             " at " + selectedUniversity.getName() + 
                             " has been submitted successfully.");
        alert.showAndWait();
    }
    
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Application Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText("Failed to submit your application: " + message);
        alert.showAndWait();
    }
    
    private void closeStage() {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleCancel() {
        closeStage();
    }
} 