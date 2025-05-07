package tn.badro.Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import tn.badro.entities.NotificationManager;
import tn.badro.entities.ProgramApplication;
import tn.badro.entities.Programme;
import tn.badro.entities.University;
import tn.badro.entities.User;
import tn.badro.services.ProgramApplicationService;
import tn.badro.services.ProgrammeService;
import tn.badro.services.UniversityService;
import tn.badro.services.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class UserApplicationsController {
    
    @FXML private BorderPane mainBorderPane;
    @FXML private TableView<ProgramApplication> applicationsTable;
    @FXML private TableColumn<ProgramApplication, String> programColumn;
    @FXML private TableColumn<ProgramApplication, String> universityColumn;
    @FXML private TableColumn<ProgramApplication, String> dateAppliedColumn;
    @FXML private TableColumn<ProgramApplication, String> startDateColumn;
    @FXML private TableColumn<ProgramApplication, String> endDateColumn;
    @FXML private TableColumn<ProgramApplication, String> statusColumn;
    @FXML private TableColumn<ProgramApplication, Void> actionsColumn;
    @FXML private Label totalApplicationsLabel;
    @FXML private Label emptyStateLabel;
    
    private User currentUser;
    private final ProgramApplicationService applicationService = new ProgramApplicationService();
    private final ProgrammeService programmeService = new ProgrammeService();
    private final UniversityService universityService = new UniversityService();
    private final SessionManager sessionManager = SessionManager.getInstance();
    
    private ObservableList<ProgramApplication> userApplications = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            showLoginRequiredAlert();
            return;
        }
        
        // Get current user
        currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            showLoginRequiredAlert();
            return;
        }
        
        setupTable();
        loadUserApplications();
    }
    
    private void setupTable() {
        // Configure columns
        programColumn.setCellValueFactory(cellData -> {
            int programId = cellData.getValue().getProgrammeId();
            try {
                Optional<Programme> programmeOpt = programmeService.getProgrammeById(programId);
                if (programmeOpt.isPresent()) {
                    return new SimpleStringProperty(programmeOpt.get().getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty("Unknown");
        });
        
        universityColumn.setCellValueFactory(cellData -> {
            int programId = cellData.getValue().getProgrammeId();
            try {
                Optional<Programme> programmeOpt = programmeService.getProgrammeById(programId);
                if (programmeOpt.isPresent()) {
                    Programme programme = programmeOpt.get();
                    int universityId = programme.getUniversityId();
                    Optional<University> universityOpt = universityService.getUniversityById(universityId);
                    if (universityOpt.isPresent()) {
                        return new SimpleStringProperty(universityOpt.get().getName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty("Unknown");
        });
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        dateAppliedColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getApplicationDate().format(formatter)));
        
        startDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStartDate().format(formatter)));
        
        endDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEndDate().format(formatter)));
        
        // Setup status column with colored status labels
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    
                    // Apply styling based on status
                    switch (status) {
                        case "Pending":
                            setStyle("-fx-background-color: #ffc107; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: CENTER;");
                            break;
                        case "Approved":
                            setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: CENTER;");
                            break;
                        case "Rejected":
                            setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: CENTER;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
        
        // Setup actions column
        actionsColumn.setCellFactory(createActionCellFactory());
        
        // Bind to data
        applicationsTable.setItems(userApplications);
    }
    
    private Callback<TableColumn<ProgramApplication, Void>, TableCell<ProgramApplication, Void>> createActionCellFactory() {
        return param -> new TableCell<>() {
            private final Button viewDetailsBtn = new Button("View Details");
            
            {
                viewDetailsBtn.setStyle("-fx-background-color: #4B9CD3; -fx-text-fill: white;");
                viewDetailsBtn.setOnAction(event -> {
                    ProgramApplication application = getTableView().getItems().get(getIndex());
                    showApplicationDetails(application);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewDetailsBtn);
                }
            }
        };
    }
    
    private void loadUserApplications() {
        try {
            // Get applications for current user
            List<ProgramApplication> applications = applicationService.getApplicationsByUserId(currentUser.getId());
            userApplications.setAll(applications);
            
            // Update counter
            totalApplicationsLabel.setText("Total: " + applications.size() + " application" + 
                                           (applications.size() != 1 ? "s" : ""));
            
            // Show empty state or table
            if (applications.isEmpty()) {
                emptyStateLabel.setVisible(true);
                applicationsTable.setVisible(false);
            } else {
                emptyStateLabel.setVisible(false);
                applicationsTable.setVisible(true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load your applications: " + e.getMessage());
        }
    }
    
    private void showApplicationDetails(ProgramApplication application) {
        try {
            // Get program and university details
            Optional<Programme> programOpt = programmeService.getProgrammeById(application.getProgrammeId());
            Optional<University> universityOpt = Optional.empty();
            
            if (programOpt.isPresent()) {
                Programme program = programOpt.get();
                universityOpt = universityService.getUniversityById(program.getUniversityId());
            }
            
            // Create content for dialog
            StringBuilder content = new StringBuilder();
            
            content.append("Program: ");
            if (programOpt.isPresent()) {
                content.append(programOpt.get().getName());
            } else {
                content.append("Unknown Program");
            }
            content.append("\n\n");
            
            content.append("University: ");
            if (universityOpt.isPresent()) {
                University university = universityOpt.get();
                content.append(university.getName()).append(" (").append(university.getLocation()).append(")");
            } else {
                content.append("Unknown University");
            }
            content.append("\n\n");
            
            // Format dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            content.append("Applied On: ").append(application.getApplicationDate().format(formatter)).append("\n");
            content.append("Exchange Period: From ").append(application.getStartDate().format(formatter))
                  .append(" to ").append(application.getEndDate().format(formatter)).append("\n\n");
            
            // Motivation letter
            content.append("YOUR MOTIVATION LETTER:\n").append(application.getMotivationLetter());
            
            // Create and configure dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Application Details");
            
            // Create status header with enhanced styling
            String statusText;
            String statusStyle;
            
            switch (application.getStatus()) {
                case "Approved":
                    statusText = "APPROVED - Congratulations! Your application has been accepted.";
                    statusStyle = "-fx-font-weight: bold; -fx-text-fill: #28a745;";
                    break;
                case "Rejected":
                    statusText = "REJECTED - Unfortunately, your application was not accepted at this time.";
                    statusStyle = "-fx-font-weight: bold; -fx-text-fill: #dc3545;";
                    break;
                default: // Pending
                    statusText = "PENDING - Your application is still under review.";
                    statusStyle = "-fx-font-weight: bold; -fx-text-fill: #ffc107;";
                    break;
            }
            
            Label statusLabel = new Label(statusText);
            statusLabel.setStyle(statusStyle);
            dialog.setHeaderText("Exchange Program Application");
            dialog.setGraphic(statusLabel);
            
            DialogPane dialogPane = dialog.getDialogPane();
            
            TextArea textArea = new TextArea(content.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefHeight(400);
            textArea.setPrefWidth(600);
            
            dialogPane.setContent(textArea);
            dialogPane.getButtonTypes().add(ButtonType.CLOSE);
            
            dialog.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to show application details: " + e.getMessage());
        }
    }
    
    @FXML
    private void returnToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/mainMenu.fxml"));
            Parent root = loader.load();
            MainMenuController controller = loader.getController();
            controller.setUserInfo(currentUser);
            
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to return to main menu: " + e.getMessage());
        }
    }
    
    /**
     * Shows login required alert
     */
    private void showLoginRequiredAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Login Required");
        alert.setHeaderText("Authentication Required");
        alert.setContentText("You must be logged in to view your applications.");
        
        ButtonType loginButton = new ButtonType("Login");
        ButtonType cancelButton = ButtonType.CANCEL;
        
        alert.getButtonTypes().setAll(loginButton, cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == loginButton) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/login.fxml"));
                Parent loginRoot = loader.load();
                Stage stage = (Stage) mainBorderPane.getScene().getWindow();
                stage.setScene(new Scene(loginRoot));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public void setUserInfo(User user) {
        this.currentUser = user;
    }
} 