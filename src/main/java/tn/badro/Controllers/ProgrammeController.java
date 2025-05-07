package tn.badro.Controllers;

import tn.badro.components.TableComponent;
import tn.badro.entities.Programme;
import tn.badro.services.ProgrammeService;
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

public class ProgrammeController {
    private final ProgrammeService programmeService = new ProgrammeService();
    private Stage stage;
    private TableComponent<Programme> tableComponent;

    @FXML
    private VBox programmeListView;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Button addButton;
    
    @FXML
    private TableView<Programme> tableView;
    
    @FXML
    public void initialize() {
        // Create table component
        tableComponent = new TableComponent<>(FXCollections.observableArrayList(programmeService.getAllProgrammes()));
        tableComponent.getTableView().setStyle("-fx-border-color: #ddd; -fx-border-width: 1px;");
        tableComponent.getTableView().setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Add columns
        TableColumn<Programme, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Programme, String> nameCol = new TableColumn<>("NAME");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);
        nameCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<Programme, String> typeCol = new TableColumn<>("TYPE");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(120);
        typeCol.setStyle("-fx-alignment: CENTER;");
        typeCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(type);
                    if (type.equalsIgnoreCase("Scholarship")) {
                        setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-padding: 5px; -fx-background-radius: 3px;");
                    } else {
                        setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #f57c00; -fx-padding: 5px; -fx-background-radius: 3px;");
                    }
                }
            }
        });

        TableColumn<Programme, String> descriptionCol = new TableColumn<>("DESCRIPTION");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionCol.setPrefWidth(300);
        descriptionCol.setStyle("-fx-alignment: CENTER-LEFT;");

        // Actions column
        TableColumn<Programme, Void> actionsCol = new TableColumn<>("ACTIONS");
        actionsCol.setPrefWidth(300);
        actionsCol.setStyle("-fx-alignment: CENTER;");
        actionsCol.setCellFactory(col -> new TableCell<Programme, Void>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox actions = new HBox(5, viewBtn, editBtn, deleteBtn);
            
            {
                viewBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-min-width: 80;");
                editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-min-width: 80;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-min-width: 80;");
                
                viewBtn.setOnAction(e -> {
                    Programme programme = getTableView().getItems().get(getIndex());
                    showProgrammeView(programme.getId());
                });
                
                editBtn.setOnAction(e -> {
                    Programme programme = getTableView().getItems().get(getIndex());
                    showEditForm(programme.getId());
                });
                
                deleteBtn.setOnAction(e -> {
                    Programme programme = getTableView().getItems().get(getIndex());
                    deleteProgramme(programme.getId());
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
            idCol, nameCol, typeCol, descriptionCol, actionsCol
        );
        
        // Replace the TableView in the FXML with our TableComponent's TableView
        tableView.getColumns().addAll(tableComponent.getTableView().getColumns());
        tableView.setItems(tableComponent.getTableView().getItems());
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

        Label titleLabel = new Label("Add New Programme");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Name field with validation
        VBox nameBox = new VBox(5);
        Label nameLabel = new Label("Programme Name *");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter programme name");
        Label nameError = new Label();
        nameError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        nameBox.getChildren().addAll(nameLabel, nameField, nameError);

        // Type selection with validation
        VBox typeBox = new VBox(5);
        Label typeLabel = new Label("Programme Type *");
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Scholarship", "Paid");
        typeComboBox.setPromptText("Select Type");
        Label typeError = new Label();
        typeError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        typeBox.getChildren().addAll(typeLabel, typeComboBox, typeError);

        // Description field with validation
        VBox descBox = new VBox(5);
        Label descLabel = new Label("Description *");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Enter programme description");
        descriptionArea.setPrefRowCount(3);
        Label descError = new Label();
        descError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        descBox.getChildren().addAll(descLabel, descriptionArea, descError);

        // Add validation listeners
        nameField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                nameError.setText("Programme name is required");
                nameField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 3) {
                nameError.setText("Name must be at least 3 characters");
                nameField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() > 50) {
                nameError.setText("Name must be less than 50 characters");
                nameField.setStyle("-fx-border-color: red;");
            } else {
                nameError.setText("");
                nameField.setStyle("");
            }
        });

        typeComboBox.valueProperty().addListener((obs, old, newValue) -> {
            if (newValue == null) {
                typeError.setText("Programme type is required");
                typeComboBox.setStyle("-fx-border-color: red;");
            } else {
                typeError.setText("");
                typeComboBox.setStyle("");
            }
        });

        descriptionArea.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                descError.setText("Description is required");
                descriptionArea.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 10) {
                descError.setText("Description must be at least 10 characters");
                descriptionArea.setStyle("-fx-border-color: red;");
            } else if (newValue.length() > 500) {
                descError.setText("Description must be less than 500 characters");
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
            nameError.setText("");
            typeError.setText("");
            descError.setText("");
            errorLabel.setText("");
            
            // Validate all fields
            boolean hasErrors = false;
            
            if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                nameError.setText("Programme name is required");
                nameField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            } else if (nameField.getText().length() < 3) {
                nameError.setText("Name must be at least 3 characters");
                nameField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            }
            
            if (typeComboBox.getValue() == null) {
                typeError.setText("Programme type is required");
                typeComboBox.setStyle("-fx-border-color: red;");
                hasErrors = true;
            }
            
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
                    Programme programme = new Programme();
                    programme.setName(nameField.getText().trim());
                    programme.setType(typeComboBox.getValue());
                    programme.setDescription(descriptionArea.getText().trim());

                    programmeService.createProgramme(programme);
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
            nameBox,
            typeBox,
            descBox,
            errorLabel,
            buttons
        );

        Scene scene = new Scene(form);
        dialog.setScene(scene);
        dialog.show();
    }

    @FXML
    public void showEditForm(Integer id) {
        Optional<Programme> programmeOpt = programmeService.getProgrammeById(id);
        if (programmeOpt.isEmpty()) {
            showError("Programme not found");
            return;
        }

        Programme programme = programmeOpt.get();
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.initOwner(stage);

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);
        form.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1px;");

        Label titleLabel = new Label("Edit Programme");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Name field with validation
        VBox nameBox = new VBox(5);
        Label nameLabel = new Label("Programme Name *");
        TextField nameField = new TextField(programme.getName());
        Label nameError = new Label();
        nameError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        nameBox.getChildren().addAll(nameLabel, nameField, nameError);

        // Type selection with validation
        VBox typeBox = new VBox(5);
        Label typeLabel = new Label("Programme Type *");
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Scholarship", "Paid");
        typeComboBox.setValue(programme.getType());
        Label typeError = new Label();
        typeError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        typeBox.getChildren().addAll(typeLabel, typeComboBox, typeError);

        // Description field with validation
        VBox descBox = new VBox(5);
        Label descLabel = new Label("Description *");
        TextArea descriptionArea = new TextArea(programme.getDescription());
        descriptionArea.setPrefRowCount(3);
        Label descError = new Label();
        descError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        descBox.getChildren().addAll(descLabel, descriptionArea, descError);

        // Add validation listeners
        nameField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                nameError.setText("Programme name is required");
                nameField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 3) {
                nameError.setText("Name must be at least 3 characters");
                nameField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() > 50) {
                nameError.setText("Name must be less than 50 characters");
                nameField.setStyle("-fx-border-color: red;");
            } else {
                nameError.setText("");
                nameField.setStyle("");
            }
        });

        typeComboBox.valueProperty().addListener((obs, old, newValue) -> {
            if (newValue == null) {
                typeError.setText("Programme type is required");
                typeComboBox.setStyle("-fx-border-color: red;");
            } else {
                typeError.setText("");
                typeComboBox.setStyle("");
            }
        });

        descriptionArea.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                descError.setText("Description is required");
                descriptionArea.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 10) {
                descError.setText("Description must be at least 10 characters");
                descriptionArea.setStyle("-fx-border-color: red;");
            } else if (newValue.length() > 500) {
                descError.setText("Description must be less than 500 characters");
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
            nameError.setText("");
            typeError.setText("");
            descError.setText("");
            errorLabel.setText("");
            
            // Validate all fields
            boolean hasErrors = false;
            
            if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                nameError.setText("Programme name is required");
                nameField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            } else if (nameField.getText().length() < 3) {
                nameError.setText("Name must be at least 3 characters");
                nameField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            }
            
            if (typeComboBox.getValue() == null) {
                typeError.setText("Programme type is required");
                typeComboBox.setStyle("-fx-border-color: red;");
                hasErrors = true;
            }
            
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
                    programme.setName(nameField.getText().trim());
                    programme.setType(typeComboBox.getValue());
                    programme.setDescription(descriptionArea.getText().trim());

                    programmeService.updateProgramme(programme);
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
            nameBox,
            typeBox,
            descBox,
            errorLabel,
            buttons
        );

        Scene scene = new Scene(form);
        dialog.setScene(scene);
        dialog.show();
    }

    @FXML
    public void showProgrammeView(Integer id) {
        Optional<Programme> programmeOpt = programmeService.getProgrammeById(id);
        if (programmeOpt.isEmpty()) {
            showError("Programme not found");
            return;
        }

        Programme programme = programmeOpt.get();
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.initOwner(stage);

        VBox view = new VBox(20);
        view.setPadding(new Insets(20));
        view.setAlignment(Pos.TOP_CENTER);
        view.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1px;");

        // Title
        Label titleLabel = new Label(programme.getName());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Type with badge
        Label typeLabel = new Label(programme.getType());
        typeLabel.setPadding(new Insets(5, 10, 5, 10));
        if (programme.getType().equalsIgnoreCase("Scholarship")) {
            typeLabel.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-background-radius: 3px;");
        } else {
            typeLabel.setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #f57c00; -fx-background-radius: 3px;");
        }

        // Description
        VBox descriptionBox = new VBox(5);
        Label descriptionTitle = new Label("Description");
        descriptionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label descriptionText = new Label(programme.getDescription() != null ? programme.getDescription() : "No description available");
        descriptionText.setWrapText(true);
        descriptionText.setMaxWidth(600);
        descriptionBox.getChildren().addAll(descriptionTitle, descriptionText);

        // Close button
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white;");
        closeButton.setOnAction(e -> dialog.close());

        view.getChildren().addAll(titleLabel, typeLabel, descriptionBox, closeButton);
        Scene scene = new Scene(view);
        dialog.setScene(scene);
        dialog.show();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    @FXML
    public void refreshList() {
        List<Programme> programmes = programmeService.getAllProgrammes();
        tableView.setItems(FXCollections.observableArrayList(programmes));
        tableView.refresh();
    }

    @FXML
    public void deleteProgramme(Integer id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Are you sure you want to delete this programme?",
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                programmeService.deleteProgramme(id);
                refreshList();
            }
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
} 