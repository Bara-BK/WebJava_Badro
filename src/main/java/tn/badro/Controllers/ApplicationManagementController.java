package tn.badro.Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import tn.badro.entities.ProgramApplication;
import tn.badro.entities.Programme;
import tn.badro.entities.University;
import tn.badro.entities.User;
import tn.badro.entities.Notification;
import tn.badro.entities.NotificationManager;
import tn.badro.services.ProgramApplicationService;
import tn.badro.services.ProgrammeService;
import tn.badro.services.UniversityService;
import tn.badro.services.UserService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ApplicationManagementController {

    @FXML private TableView<ProgramApplication> applicationsTable;
    @FXML private TableColumn<ProgramApplication, String> idColumn;
    @FXML private TableColumn<ProgramApplication, String> userColumn;
    @FXML private TableColumn<ProgramApplication, String> programColumn;
    @FXML private TableColumn<ProgramApplication, String> universityColumn;
    @FXML private TableColumn<ProgramApplication, String> dateAppliedColumn;
    @FXML private TableColumn<ProgramApplication, String> startDateColumn;
    @FXML private TableColumn<ProgramApplication, String> endDateColumn;
    @FXML private TableColumn<ProgramApplication, String> statusColumn;
    @FXML private TableColumn<ProgramApplication, Void> actionsColumn;
    
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Label totalApplicationsLabel;

    private final ProgramApplicationService applicationService = new ProgramApplicationService();
    private final UserService userService = new UserService();
    private final ProgrammeService programmeService = new ProgrammeService();
    private final UniversityService universityService = new UniversityService();
    private final NotificationManager notificationManager = NotificationManager.getInstance();
    
    private ObservableList<ProgramApplication> applications = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupStatusFilter();
        loadApplications();
        
        // Set up search functionality
        searchButton.setOnAction(e -> handleSearch());
        searchField.setOnAction(e -> handleSearch());
    }
    
    private void setupTable() {
        // Setup columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("applicationId"));
        
        userColumn.setCellValueFactory(cellData -> {
            int userId = cellData.getValue().getUserId();
            try {
                Optional<User> userOpt = userService.getUserById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    return new SimpleStringProperty(user.getNom() + " " + user.getPrenom());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty("Unknown");
        });
        
        programColumn.setCellValueFactory(cellData -> {
            int programId = cellData.getValue().getProgrammeId();
            try {
                Optional<Programme> programmeOpt = programmeService.getProgrammeById(programId);
                if (programmeOpt.isPresent()) {
                    Programme programme = programmeOpt.get();
                    return new SimpleStringProperty(programme.getName());
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
                        University university = universityOpt.get();
                        return new SimpleStringProperty(university.getName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty("Unknown");
        });
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        dateAppliedColumn.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getApplicationDate();
            return new SimpleStringProperty(date != null ? date.format(formatter) : "N/A");
        });
        
        startDateColumn.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getStartDate();
            return new SimpleStringProperty(date != null ? date.format(formatter) : "N/A");
        });
        
        endDateColumn.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getEndDate();
            return new SimpleStringProperty(date != null ? date.format(formatter) : "N/A");
        });
        
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
        
        // Setup action column with buttons
        actionsColumn.setCellFactory(createActionCellFactory());
        
        // Bind table to data
        applicationsTable.setItems(applications);
    }
    
    private Callback<TableColumn<ProgramApplication, Void>, TableCell<ProgramApplication, Void>> createActionCellFactory() {
        return param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            
            private final HBox pane = new HBox(5, viewBtn, approveBtn, rejectBtn);
            
            {
                // Style buttons
                viewBtn.setStyle("-fx-background-color: #4B9CD3; -fx-text-fill: white;");
                approveBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                rejectBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                
                // Set button actions
                viewBtn.setOnAction(event -> {
                    ProgramApplication application = getTableView().getItems().get(getIndex());
                    showApplicationDetails(application);
                });
                
                approveBtn.setOnAction(event -> {
                    ProgramApplication application = getTableView().getItems().get(getIndex());
                    updateApplicationStatus(application, "Approved");
                });
                
                rejectBtn.setOnAction(event -> {
                    ProgramApplication application = getTableView().getItems().get(getIndex());
                    updateApplicationStatus(application, "Rejected");
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    // Get the current application
                    ProgramApplication application = getTableView().getItems().get(getIndex());
                    String status = application.getStatus();
                    
                    // Disable appropriate buttons based on current status
                    approveBtn.setDisable("Approved".equals(status));
                    rejectBtn.setDisable("Rejected".equals(status));
                    
                    setGraphic(pane);
                }
            }
        };
    }
    
    private void setupStatusFilter() {
        // Setup status filter
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
            "All", "Pending", "Approved", "Rejected"
        );
        statusFilterComboBox.setItems(statusOptions);
        statusFilterComboBox.setValue("All");
        
        // Add listener to filter applications when status changes
        statusFilterComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> filterApplicationsByStatus(newValue)
        );
    }
    
    private void loadApplications() {
        try {
            List<ProgramApplication> allApplications = applicationService.getAllApplications();
            applications.setAll(allApplications);
            updateTotalCount();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load applications: " + e.getMessage());
        }
    }
    
    private void filterApplicationsByStatus(String status) {
        if (status == null || "All".equals(status)) {
            loadApplications();
            return;
        }
        
        try {
            List<ProgramApplication> filteredApplications = applicationService.getApplicationsByStatus(status);
            applications.setAll(filteredApplications);
            updateTotalCount();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to filter applications: " + e.getMessage());
        }
    }
    
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            // Reset to current filter
            filterApplicationsByStatus(statusFilterComboBox.getValue());
            return;
        }
        
        // We'll search through all applications and filter manually since the search
        // could be matching user names, program names, etc.
        try {
            List<ProgramApplication> allApplications;
            if (!"All".equals(statusFilterComboBox.getValue())) {
                allApplications = applicationService.getApplicationsByStatus(statusFilterComboBox.getValue());
            } else {
                allApplications = applicationService.getAllApplications();
            }
            
            ObservableList<ProgramApplication> searchResults = FXCollections.observableArrayList();
            
            // Filter applications based on search term
            for (ProgramApplication app : allApplications) {
                try {
                    // Search by user name
                    Optional<User> userOpt = userService.getUserById(app.getUserId());
                    if (userOpt.isPresent() && 
                        (userOpt.get().getNom().toLowerCase().contains(searchTerm.toLowerCase()) || 
                         userOpt.get().getPrenom().toLowerCase().contains(searchTerm.toLowerCase()) ||
                         userOpt.get().getEmail().toLowerCase().contains(searchTerm.toLowerCase()))) {
                        searchResults.add(app);
                        continue;
                    }
                    
                    // Search by program name
                    Optional<Programme> programOpt = programmeService.getProgrammeById(app.getProgrammeId());
                    if (programOpt.isPresent() && 
                        programOpt.get().getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                        searchResults.add(app);
                        continue;
                    }
                    
                    // Search by application ID
                    if (app.getApplicationId().toLowerCase().contains(searchTerm.toLowerCase())) {
                        searchResults.add(app);
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            applications.setAll(searchResults);
            updateTotalCount();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to search applications: " + e.getMessage());
        }
    }
    
    private void updateTotalCount() {
        totalApplicationsLabel.setText("Total: " + applications.size() + " applications");
    }
    
    private void showApplicationDetails(ProgramApplication application) {
        try {
            // Get associated data
            Optional<User> userOpt = userService.getUserById(application.getUserId());
            Optional<Programme> programOpt = programmeService.getProgrammeById(application.getProgrammeId());
            Optional<University> universityOpt = Optional.empty();
            
            if (programOpt.isPresent()) {
                Programme program = programOpt.get();
                universityOpt = universityService.getUniversityById(program.getUniversityId());
            }
            
            // Create content for the dialog
            StringBuilder content = new StringBuilder();
            content.append("Application ID: ").append(application.getApplicationId()).append("\n\n");
            
            content.append("Applicant: ");
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                content.append(user.getNom()).append(" ").append(user.getPrenom());
                content.append(" (").append(user.getEmail()).append(")");
            } else {
                content.append("Unknown User");
            }
            content.append("\n\n");
            
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
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            content.append("Applied On: ").append(application.getApplicationDate().format(formatter)).append("\n");
            content.append("Exchange Period: From ").append(application.getStartDate().format(formatter))
                  .append(" to ").append(application.getEndDate().format(formatter)).append("\n\n");
            
            // Create status label with style
            String statusStyle;
            switch (application.getStatus()) {
                case "Approved":
                    statusStyle = "-fx-font-weight: bold; -fx-text-fill: #28a745;";
                    break;
                case "Rejected":
                    statusStyle = "-fx-font-weight: bold; -fx-text-fill: #dc3545;";
                    break;
                default: // Pending
                    statusStyle = "-fx-font-weight: bold; -fx-text-fill: #ffc107;";
                    break;
            }
            
            content.append("Status: ").append(application.getStatus()).append("\n\n");
            content.append("MOTIVATION LETTER:\n").append(application.getMotivationLetter());
            
            // Create and show the dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Application Details");
            dialog.setHeaderText("Exchange Program Application");
            
            Label statusLabel = new Label("Status: " + application.getStatus());
            statusLabel.setStyle(statusStyle);
            dialog.setGraphic(statusLabel);
            
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setContent(new TextArea(content.toString()) {
                {
                    setEditable(false);
                    setWrapText(true);
                    setPrefHeight(400);
                    setPrefWidth(600);
                }
            });
            
            // Add buttons to dialog
            dialogPane.getButtonTypes().addAll(ButtonType.OK);
            if ("Pending".equals(application.getStatus())) {
                ButtonType approveButtonType = new ButtonType("Approve", ButtonBar.ButtonData.OK_DONE);
                ButtonType rejectButtonType = new ButtonType("Reject", ButtonBar.ButtonData.CANCEL_CLOSE);
                dialogPane.getButtonTypes().addAll(approveButtonType, rejectButtonType);
                
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent()) {
                    if (result.get() == approveButtonType) {
                        updateApplicationStatus(application, "Approved");
                    } else if (result.get() == rejectButtonType) {
                        updateApplicationStatus(application, "Rejected");
                    }
                }
            } else {
                dialog.showAndWait();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to show application details: " + e.getMessage());
        }
    }
    
    private void updateApplicationStatus(ProgramApplication application, String newStatus) {
        try {
            // Get user info for notification
            Optional<User> userOpt = userService.getUserById(application.getUserId());
            String userName = userOpt.map(user -> user.getNom() + " " + user.getPrenom()).orElse("Unknown User");
            
            // Get program info for notification
            Optional<Programme> programOpt = programmeService.getProgrammeById(application.getProgrammeId());
            String programName = programOpt.map(Programme::getName).orElse("Unknown Program");
            
            if (applicationService.updateApplicationStatus(application.getApplicationId(), newStatus)) {
                // Create notification message
                String notificationMessage = createStatusChangeNotification(userName, programName, newStatus);
                
                // Add notification to the system
                notificationManager.addNotification(new Notification(notificationMessage));
                
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                          "Application status updated to " + newStatus + " and notification sent");
                
                // Refresh the table to reflect changes
                application.setStatus(newStatus);
                applicationsTable.refresh();
                
                // Reload applications if we're filtering by status
                if (!"All".equals(statusFilterComboBox.getValue())) {
                    filterApplicationsByStatus(statusFilterComboBox.getValue());
                }
                
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                          "Failed to update application status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                      "Failed to update application status: " + e.getMessage());
        }
    }
    
    /**
     * Creates a notification message for status change
     */
    private String createStatusChangeNotification(String userName, String programName, String newStatus) {
        StringBuilder message = new StringBuilder();
        
        switch (newStatus) {
            case "Approved":
                message.append("Application APPROVED: ");
                message.append(userName).append("'s application for ").append(programName);
                message.append(" has been approved. The student will be notified.");
                break;
                
            case "Rejected":
                message.append("Application REJECTED: ");
                message.append(userName).append("'s application for ").append(programName);
                message.append(" has been rejected. The student will be notified.");
                break;
                
            default:
                message.append("Application status changed to ").append(newStatus).append(" for ");
                message.append(userName).append("'s application to ").append(programName).append(".");
        }
        
        return message.toString();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 