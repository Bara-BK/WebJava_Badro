package tn.badro.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import tn.badro.entities.Experience;
import tn.badro.entities.User;
import tn.badro.services.ExperienceService;
import tn.badro.services.UserService;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ExperienceAdminController implements Initializable {

    @FXML private TableView<Experience> experienceTable;
    @FXML private TableColumn<Experience, Integer> idColumn;
    @FXML private TableColumn<Experience, String> titleColumn;
    @FXML private TableColumn<Experience, String> userColumn;
    @FXML private TableColumn<Experience, String> dateColumn;
    @FXML private TableColumn<Experience, Integer> likesColumn;
    @FXML private TableColumn<Experience, Integer> commentsColumn;
    @FXML private TableColumn<Experience, Void> actionColumn;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Button btnAdd;
    @FXML private Button btnSearch;
    @FXML private Button btnRefresh;

    private final ExperienceService experienceService = new ExperienceService();
    private final UserService userService = new UserService();
    private ObservableList<Experience> experiencesList = FXCollections.observableArrayList();
    private FilteredList<Experience> filteredExperiences;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        // Custom cell factory for user column to show user's name instead of ID
        userColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    Experience experience = getTableView().getItems().get(getIndex());
                    Optional<User> user = userService.getUserById(experience.getUserId());
                    setText(user.map(u -> u.getNom() + " " + u.getPrenom()).orElse("Unknown User"));
                }
            }
        });
        
        // Date column formatter
        dateColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    Experience experience = getTableView().getItems().get(getIndex());
                    setText(experience.getDatePosted().format(formatter));
                }
            }
        });
        
        // Like count column
        likesColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    Experience experience = getTableView().getItems().get(getIndex());
                    setText(String.valueOf(experienceService.getLikeCount(experience.getId())));
                }
            }
        });
        
        // Comment count column
        commentsColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    Experience experience = getTableView().getItems().get(getIndex());
                    setText(String.valueOf(experienceService.getCommentCount(experience.getId())));
                }
            }
        });
        
        // Action buttons column
        actionColumn.setCellFactory(createActionButtonsCellFactory());
        
        // Setup search functionality
        setupSearch();
        
        // Load data
        loadExperiences();
    }
    
    private void setupSearch() {
        filteredExperiences = new FilteredList<>(experiencesList, p -> true);
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredExperiences.setPredicate(experience -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                if (experience.getTitle().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else return experience.getDescription().toLowerCase().contains(lowerCaseFilter);
            });
            
            updateStatusLabel();
        });
        
        experienceTable.setItems(filteredExperiences);
    }
    
    @FXML
    private void onAddClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/badro/experienceForm.fxml"));
            Parent root = loader.load();
            
            ExperienceFormController controller = loader.getController();
            controller.setOnExperienceAddedCallback(this::loadExperiences);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Experience");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open experience form: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onRefreshClicked() {
        loadExperiences();
    }
    
    @FXML
    private void onSearchClicked() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            filteredExperiences.setPredicate(p -> true);
        } else {
            filteredExperiences.setPredicate(experience -> {
                String lowerCaseFilter = searchTerm.toLowerCase();
                if (experience.getTitle().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else return experience.getDescription().toLowerCase().contains(lowerCaseFilter);
            });
        }
        
        updateStatusLabel();
    }
    
    private void loadExperiences() {
        try {
            List<Experience> experiences = experienceService.getAll();
            experiencesList.clear();
            experiencesList.addAll(experiences);
            
            // Reset search filter
            if (filteredExperiences != null) {
                filteredExperiences.setPredicate(p -> true);
                searchField.clear();
            }
            
            updateStatusLabel();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load experiences: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateStatusLabel() {
        statusLabel.setText("Total Experiences: " + filteredExperiences.size());
    }
    
    private Callback<TableColumn<Experience, Void>, TableCell<Experience, Void>> createActionButtonsCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Experience, Void> call(final TableColumn<Experience, Void> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button("View");
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    
                    {
                        viewBtn.setStyle("-fx-background-color: #307D91; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 3 8;");
                        editBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 3 8;");
                        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 3 8;");
                        
                        viewBtn.setOnAction(event -> {
                            Experience experience = getTableView().getItems().get(getIndex());
                            viewExperience(experience);
                        });
                        
                        editBtn.setOnAction(event -> {
                            Experience experience = getTableView().getItems().get(getIndex());
                            editExperience(experience);
                        });
                        
                        deleteBtn.setOnAction(event -> {
                            Experience experience = getTableView().getItems().get(getIndex());
                            deleteExperience(experience);
                        });
                    }
                    
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(5, viewBtn, editBtn, deleteBtn);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        };
    }
    
    private void viewExperience(Experience experience) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/badro/experienceDetail.fxml"));
            Parent root = loader.load();
            
            ExperienceDetailController controller = loader.getController();
            controller.setExperience(experience);
            controller.setOnDeleteCallback(this::loadExperiences);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(experience.getTitle());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to view experience: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void editExperience(Experience experience) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/badro/experienceForm.fxml"));
            Parent root = loader.load();
            
            ExperienceFormController controller = loader.getController();
            controller.setExperience(experience);
            controller.setOnExperienceAddedCallback(this::loadExperiences);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Experience");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to edit experience: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deleteExperience(Experience experience) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Experience");
        confirmAlert.setContentText("Are you sure you want to delete this experience? This action cannot be undone.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = experienceService.delete(experience.getId());
            if (success) {
                loadExperiences();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete experience. Please try again later.");
            }
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 