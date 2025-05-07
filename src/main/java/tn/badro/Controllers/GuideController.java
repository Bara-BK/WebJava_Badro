package tn.badro.Controllers;

import tn.badro.components.TableComponent;
import tn.badro.entities.Guide;
import tn.badro.services.GuideService;
import tn.badro.services.UniversityService;
import tn.badro.entities.University;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.fxml.FXML;

import java.util.List;
import java.util.Optional;

public class GuideController {
    private final GuideService guideService = new GuideService();
    private final UniversityService universityService = new UniversityService();
    private Stage stage;
    private TableComponent<Guide> tableComponent;

    @FXML
    private VBox guideListView;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Button addButton;
    
    @FXML
    private TableView<Guide> tableView;
    
    @FXML
    public void initialize() {
        // Create table component
        tableComponent = new TableComponent<>(FXCollections.observableArrayList(guideService.getAllGuides()));
        tableComponent.getTableView().setStyle("-fx-border-color: #ddd; -fx-border-width: 1px;");
        tableComponent.getTableView().setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Add columns
        TableColumn<Guide, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Guide, Integer> universityIdCol = new TableColumn<>("UNIVERSITY ID");
        universityIdCol.setCellValueFactory(new PropertyValueFactory<>("universityId"));
        universityIdCol.setPrefWidth(120);
        universityIdCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Guide, String> titleCol = new TableColumn<>("TITLE");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);
        titleCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<Guide, String> countryCol = new TableColumn<>("COUNTRY");
        countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        countryCol.setPrefWidth(150);
        countryCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Guide, String> descriptionCol = new TableColumn<>("DESCRIPTION");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionCol.setPrefWidth(300);
        descriptionCol.setStyle("-fx-alignment: CENTER-LEFT;");

        // Actions column
        TableColumn<Guide, Void> actionsCol = new TableColumn<>("ACTIONS");
        actionsCol.setPrefWidth(300);
        actionsCol.setStyle("-fx-alignment: CENTER;");
        actionsCol.setCellFactory(col -> new TableCell<Guide, Void>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox actions = new HBox(5, viewBtn, editBtn, deleteBtn);
            
            {
                viewBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-min-width: 60; -fx-padding: 5 10;");
                editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-min-width: 60; -fx-padding: 5 10;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-min-width: 60; -fx-padding: 5 10;");
                
                viewBtn.setOnAction(e -> {
                    Guide guide = getTableView().getItems().get(getIndex());
                    showGuideView(guide.getId());
                });
                
                editBtn.setOnAction(e -> {
                    Guide guide = getTableView().getItems().get(getIndex());
                    showEditForm(guide.getId());
                });
                
                deleteBtn.setOnAction(e -> {
                    Guide guide = getTableView().getItems().get(getIndex());
                    deleteGuide(guide.getId());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actions);
                }
            }
        });

        tableComponent.getTableView().getColumns().addAll(
            idCol, universityIdCol, titleCol, countryCol, descriptionCol, actionsCol
        );
        
        // Replace the TableView in the FXML with our TableComponent's TableView
        tableView.getColumns().addAll(tableComponent.getTableView().getColumns());
        tableView.setItems(tableComponent.getTableView().getItems());
    }

    private void validateUniversityId(String value, Label errorLabel, TextField field) {
        if (value == null || value.trim().isEmpty()) {
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
            errorLabel.setText("University ID is required");
            field.setStyle("-fx-border-color: red;");
            return;
        }

        try {
            int universityId = Integer.parseInt(value.trim());
            if (universityId <= 0) {
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                errorLabel.setText("University ID must be positive");
                field.setStyle("-fx-border-color: red;");
                return;
            }

            // Check if university exists
            Optional<University> university = universityService.getUniversityById(universityId);
            if (university.isEmpty()) {
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                errorLabel.setText("University ID does not exist");
                field.setStyle("-fx-border-color: red;");
            } else {
                errorLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
                errorLabel.setText("University: " + university.get().getName());
                field.setStyle("");
            }
        } catch (NumberFormatException e) {
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
            errorLabel.setText("University ID must be a number");
            field.setStyle("-fx-border-color: red;");
        }
    }

    @FXML
    public void showCreateForm() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.initOwner(stage);

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);
        form.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1px;");

        Label titleLabel = new Label("Add New Guide");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // University ID field with validation
        VBox universityBox = new VBox(5);
        Label universityLabel = new Label("University ID *");
        TextField universityIdField = new TextField();
        universityIdField.setPromptText("Enter university ID");
        Label universityError = new Label();
        universityError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        universityBox.getChildren().addAll(universityLabel, universityIdField, universityError);

        // Title field with validation
        VBox titleBox = new VBox(5);
        Label titleFieldLabel = new Label("Title *");
        TextField titleField = new TextField();
        titleField.setPromptText("Enter guide title");
        Label titleError = new Label();
        titleError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        titleBox.getChildren().addAll(titleFieldLabel, titleField, titleError);

        // Country field with validation
        VBox countryBox = new VBox(5);
        Label countryLabel = new Label("Country *");
        TextField countryField = new TextField();
        countryField.setPromptText("Enter country");
        Label countryError = new Label();
        countryError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        countryBox.getChildren().addAll(countryLabel, countryField, countryError);

        // Description field with validation
        VBox descBox = new VBox(5);
        Label descLabel = new Label("Description *");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Enter guide description");
        descriptionArea.setPrefRowCount(3);
        Label descError = new Label();
        descError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        descBox.getChildren().addAll(descLabel, descriptionArea, descError);

        // Add validation listeners
        universityIdField.textProperty().addListener((obs, old, newValue) -> {
            validateUniversityId(newValue, universityError, universityIdField);
        });

        titleField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                titleError.setText("Title is required");
                titleField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 3) {
                titleError.setText("Title must be at least 3 characters");
                titleField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() > 255) {
                titleError.setText("Title must be less than 255 characters");
                titleField.setStyle("-fx-border-color: red;");
            } else {
                titleError.setText("");
                titleField.setStyle("");
            }
        });

        countryField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                countryError.setText("Country is required");
                countryField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 2) {
                countryError.setText("Country must be at least 2 characters");
                countryField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() > 255) {
                countryError.setText("Country must be less than 255 characters");
                countryField.setStyle("-fx-border-color: red;");
            } else {
                countryError.setText("");
                countryField.setStyle("");
            }
        });

        descriptionArea.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                descError.setText("Description is required");
                descriptionArea.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 10) {
                descError.setText("Description must be at least 10 characters");
                descriptionArea.setStyle("-fx-border-color: red;");
            } else {
                descError.setText("");
                descriptionArea.setStyle("");
            }
        });

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        saveButton.setOnAction(e -> {
            // Clear previous error states
            universityError.setText("");
            titleError.setText("");
            countryError.setText("");
            descError.setText("");
            errorLabel.setText("");
            
            // Validate all fields
            boolean hasErrors = false;
            
            // University ID validation with existence check
            int universityId = 0;
            try {
                universityId = Integer.parseInt(universityIdField.getText().trim());
                if (universityId <= 0) {
                    universityError.setText("University ID must be positive");
                    universityIdField.setStyle("-fx-border-color: red;");
                    hasErrors = true;
                } else {
                    Optional<University> university = universityService.getUniversityById(universityId);
                    if (university.isEmpty()) {
                        universityError.setText("University ID does not exist");
                        universityIdField.setStyle("-fx-border-color: red;");
                        hasErrors = true;
                    }
                }
            } catch (NumberFormatException ex) {
                universityError.setText("University ID must be a number");
                universityIdField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            }
            
            // Title validation
            if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
                titleError.setText("Title is required");
                titleField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            } else if (titleField.getText().length() < 3) {
                titleError.setText("Title must be at least 3 characters");
                titleField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            }
            
            // Country validation
            if (countryField.getText() == null || countryField.getText().trim().isEmpty()) {
                countryError.setText("Country is required");
                countryField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            } else if (countryField.getText().length() < 2) {
                countryError.setText("Country must be at least 2 characters");
                countryField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            }
            
            // Description validation
            if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
                descError.setText("Description is required");
                descriptionArea.setStyle("-fx-border-color: red;");
                hasErrors = true;
            } else if (descriptionArea.getText().length() < 10) {
                descError.setText("Description must be at least 10 characters");
                descriptionArea.setStyle("-fx-border-color: red;");
                hasErrors = true;
            }
            
            if (!hasErrors) {
                try {
                    Guide guide = new Guide();
                    guide.setUniversityId(universityId);
                    guide.setTitle(titleField.getText().trim());
                    guide.setCountry(countryField.getText().trim());
                    guide.setDescription(descriptionArea.getText().trim());

                    guideService.createGuide(guide);
                    dialog.close();
                    refreshList();
                } catch (Exception ex) {
                    errorLabel.setText("Error: " + ex.getMessage());
                }
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, saveButton, cancelButton);
        buttons.setAlignment(Pos.CENTER);

        form.getChildren().addAll(
            titleLabel,
            universityBox,
            titleBox,
            countryBox,
            descBox,
            errorLabel,
            buttons
        );

        Scene scene = new Scene(form);
        dialog.setScene(scene);
        dialog.show();
    }

    private void showEditForm(Integer id) {
        Optional<Guide> guideOpt = guideService.getGuideById(id);
        if (guideOpt.isEmpty()) {
            showError("Guide not found");
            return;
        }

        Guide guide = guideOpt.get();
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.initOwner(stage);

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);
        form.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1px;");

        Label titleLabel = new Label("Edit Guide");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // University ID field with validation
        VBox universityBox = new VBox(5);
        Label universityLabel = new Label("University ID *");
        TextField universityIdField = new TextField(guide.getUniversityId().toString());
        Label universityError = new Label();
        universityError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        universityBox.getChildren().addAll(universityLabel, universityIdField, universityError);

        // Title field with validation
        VBox titleBox = new VBox(5);
        Label titleFieldLabel = new Label("Title *");
        TextField titleField = new TextField(guide.getTitle());
        Label titleError = new Label();
        titleError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        titleBox.getChildren().addAll(titleFieldLabel, titleField, titleError);

        // Country field with validation
        VBox countryBox = new VBox(5);
        Label countryLabel = new Label("Country *");
        TextField countryField = new TextField(guide.getCountry());
        Label countryError = new Label();
        countryError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        countryBox.getChildren().addAll(countryLabel, countryField, countryError);

        // Description field with validation
        VBox descBox = new VBox(5);
        Label descLabel = new Label("Description *");
        TextArea descriptionArea = new TextArea(guide.getDescription());
        descriptionArea.setPrefRowCount(3);
        Label descError = new Label();
        descError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        descBox.getChildren().addAll(descLabel, descriptionArea, descError);

        // Add validation listeners
        universityIdField.textProperty().addListener((obs, old, newValue) -> {
            validateUniversityId(newValue, universityError, universityIdField);
        });

        titleField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                titleError.setText("Title is required");
                titleField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 3) {
                titleError.setText("Title must be at least 3 characters");
                titleField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() > 255) {
                titleError.setText("Title must be less than 255 characters");
                titleField.setStyle("-fx-border-color: red;");
            } else {
                titleError.setText("");
                titleField.setStyle("");
            }
        });

        countryField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                countryError.setText("Country is required");
                countryField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 2) {
                countryError.setText("Country must be at least 2 characters");
                countryField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() > 255) {
                countryError.setText("Country must be less than 255 characters");
                countryField.setStyle("-fx-border-color: red;");
            } else {
                countryError.setText("");
                countryField.setStyle("");
            }
        });

        descriptionArea.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                descError.setText("Description is required");
                descriptionArea.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 10) {
                descError.setText("Description must be at least 10 characters");
                descriptionArea.setStyle("-fx-border-color: red;");
            } else {
                descError.setText("");
                descriptionArea.setStyle("");
            }
        });

        Button saveButton = new Button("Update");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        saveButton.setOnAction(e -> {
            // Clear previous error states
            universityError.setText("");
            titleError.setText("");
            countryError.setText("");
            descError.setText("");
            errorLabel.setText("");
            
            // Validate all fields
            boolean hasErrors = false;
            
            // University ID validation with existence check
            int universityId = 0;
            try {
                universityId = Integer.parseInt(universityIdField.getText().trim());
                if (universityId <= 0) {
                    universityError.setText("University ID must be positive");
                    universityIdField.setStyle("-fx-border-color: red;");
                    hasErrors = true;
                } else {
                    Optional<University> university = universityService.getUniversityById(universityId);
                    if (university.isEmpty()) {
                        universityError.setText("University ID does not exist");
                        universityIdField.setStyle("-fx-border-color: red;");
                        hasErrors = true;
                    }
                }
            } catch (NumberFormatException ex) {
                universityError.setText("University ID must be a number");
                universityIdField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            }
            
            // Title validation
            if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
                titleError.setText("Title is required");
                titleField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            } else if (titleField.getText().length() < 3) {
                titleError.setText("Title must be at least 3 characters");
                titleField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            }
            
            // Country validation
            if (countryField.getText() == null || countryField.getText().trim().isEmpty()) {
                countryError.setText("Country is required");
                countryField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            } else if (countryField.getText().length() < 2) {
                countryError.setText("Country must be at least 2 characters");
                countryField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            }
            
            // Description validation
            if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
                descError.setText("Description is required");
                descriptionArea.setStyle("-fx-border-color: red;");
                hasErrors = true;
            } else if (descriptionArea.getText().length() < 10) {
                descError.setText("Description must be at least 10 characters");
                descriptionArea.setStyle("-fx-border-color: red;");
                hasErrors = true;
            }
            
            if (!hasErrors) {
                try {
                    guide.setUniversityId(universityId);
                    guide.setTitle(titleField.getText().trim());
                    guide.setCountry(countryField.getText().trim());
                    guide.setDescription(descriptionArea.getText().trim());

                    guideService.updateGuide(id, guide);
                    dialog.close();
                    refreshList();
                } catch (Exception ex) {
                    errorLabel.setText("Error: " + ex.getMessage());
                }
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, saveButton, cancelButton);
        buttons.setAlignment(Pos.CENTER);

        form.getChildren().addAll(
            titleLabel,
            universityBox,
            titleBox,
            countryBox,
            descBox,
            errorLabel,
            buttons
        );

        Scene scene = new Scene(form);
        dialog.setScene(scene);
        dialog.show();
    }

    private void showGuideView(Integer id) {
        Optional<Guide> guideOpt = guideService.getGuideById(id);
        if (guideOpt.isEmpty()) {
            showError("Guide not found");
            return;
        }

        Guide guide = guideOpt.get();
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.initOwner(stage);

        // Create the main content VBox
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: white;");

        // Title
        Label titleLabel = new Label(guide.getTitle());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);

        // Get university name
        String universityName = universityService.getUniversityById(guide.getUniversityId())
            .map(University::getName)
            .orElse("Unknown University");

        Label universityLabel = new Label("University: " + universityName);
        universityLabel.setWrapText(true);
        
        Label countryLabel = new Label("Country: " + guide.getCountry());
        countryLabel.setWrapText(true);

        // Description with header
        VBox descriptionBox = new VBox(5);
        Label descriptionHeader = new Label("Description:");
        descriptionHeader.setStyle("-fx-font-weight: bold;");
        Label descriptionText = new Label(guide.getDescription() != null ? guide.getDescription() : "No description");
        descriptionText.setWrapText(true);
        descriptionText.setMaxWidth(400);
        descriptionBox.getChildren().addAll(descriptionHeader, descriptionText);

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white;");
        closeButton.setOnAction(e -> dialog.close());

        // Add all elements to the content VBox
        content.getChildren().addAll(
            titleLabel,
            universityLabel,
            countryLabel,
            descriptionBox,
            closeButton
        );

        // Create ScrollPane and set content
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1px;");
        scrollPane.setPrefViewportHeight(400);  // Set a reasonable default height
        scrollPane.setPrefViewportWidth(450);   // Set a reasonable default width

        Scene scene = new Scene(scrollPane);
        dialog.setScene(scene);
        dialog.show();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    @FXML
    public void refreshList() {
        List<Guide> guides = guideService.getAllGuides();
        tableView.setItems(FXCollections.observableArrayList(guides));
        tableView.refresh();
    }

    private void deleteGuide(Integer id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Are you sure you want to delete this guide?",
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                guideService.deleteGuide(id);
                refreshList();
            }
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
} 