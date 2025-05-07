package tn.badro.Controllers;

import tn.badro.components.TableComponent;
import tn.badro.entities.User;
import tn.badro.entities.University;
import tn.badro.services.UniversityService;
import tn.badro.services.ProgrammeService;
import tn.badro.entities.Programme;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.awt.Desktop;

import javafx.fxml.FXML;

public class UniversityController {
    private final UniversityService universityService = new UniversityService();
    private final ProgrammeService programmeService = new ProgrammeService();
    private Stage stage;
    private TableComponent<University> tableComponent;

    @FXML
    private VBox universityListView;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button generatePdfButton;
    
    @FXML
    private TableView<University> tableView;
    
    @FXML
    private Label userDisplayName;

    private User currentUser;

    @FXML
    public void initialize() {
        // Create table component
        tableComponent = new TableComponent<>(FXCollections.observableArrayList(universityService.getAllUniversities()));
        tableComponent.getTableView().setStyle("-fx-border-color: #ddd; -fx-border-width: 1px;");
        tableComponent.getTableView().setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Add columns
        TableColumn<University, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<University, String> imageCol = new TableColumn<>("IMAGE");
        imageCol.setCellValueFactory(new PropertyValueFactory<>("image"));
        imageCol.setPrefWidth(100);
        imageCol.setStyle("-fx-alignment: CENTER;");
        imageCol.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null) {
                    setGraphic(null);
                } else {
                    try {
                        imageView.setImage(new Image(imagePath));
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        TableColumn<University, String> nameCol = new TableColumn<>("NAME");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        nameCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<University, String> locationCol = new TableColumn<>("LOCATION");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locationCol.setPrefWidth(150);
        locationCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<University, String> descriptionCol = new TableColumn<>("DESCRIPTION");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionCol.setPrefWidth(300);
        descriptionCol.setStyle("-fx-alignment: CENTER-LEFT;");

        // Actions column
        TableColumn<University, Void> actionsCol = new TableColumn<>("ACTIONS");
        actionsCol.setPrefWidth(300);
        actionsCol.setStyle("-fx-alignment: CENTER;");
        actionsCol.setCellFactory(col -> new TableCell<University, Void>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox actions = new HBox(5, viewBtn, editBtn, deleteBtn);
            
            {
                viewBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-min-width: 60; -fx-padding: 5 10;");
                editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-min-width: 60; -fx-padding: 5 10;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-min-width: 60; -fx-padding: 5 10;");
                
                viewBtn.setOnAction(e -> {
                    University university = getTableView().getItems().get(getIndex());
                    showUniversityView(university.getId());
                });
                
                editBtn.setOnAction(e -> {
                    University university = getTableView().getItems().get(getIndex());
                    showEditForm(university.getId());
                });
                
                deleteBtn.setOnAction(e -> {
                    University university = getTableView().getItems().get(getIndex());
                    deleteUniversity(university.getId());
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
            idCol, imageCol, nameCol, locationCol, descriptionCol, actionsCol
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

        Label titleLabel = new Label("Add New University");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Name field with validation
        VBox nameBox = new VBox(5);
        Label nameLabel = new Label("University Name *");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter university name");
        Label nameError = new Label();
        nameError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        nameBox.getChildren().addAll(nameLabel, nameField, nameError);

        // Location field with validation
        VBox locationBox = new VBox(5);
        Label locationLabel = new Label("Location *");
        TextField locationField = new TextField();
        locationField.setPromptText("Enter location");
        Label locationError = new Label();
        locationError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        locationBox.getChildren().addAll(locationLabel, locationField, locationError);

        // Description field with validation
        VBox descBox = new VBox(5);
        Label descLabel = new Label("Description *");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Enter university description");
        descriptionArea.setPrefRowCount(3);
        Label descError = new Label();
        descError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        descBox.getChildren().addAll(descLabel, descriptionArea, descError);

        // Image URL field with validation
        VBox imageBox = new VBox(5);
        Label imageLabel = new Label("Image URL");
        TextField imageField = new TextField();
        imageField.setPromptText("Enter image URL (optional)");
        Label imageError = new Label();
        imageError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        imageBox.getChildren().addAll(imageLabel, imageField, imageError);

        // Add validation listeners
        nameField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                nameError.setText("University name is required");
                nameField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 3) {
                nameError.setText("Name must be at least 3 characters");
                nameField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() > 100) {
                nameError.setText("Name must be less than 100 characters");
                nameField.setStyle("-fx-border-color: red;");
            } else {
                nameError.setText("");
                nameField.setStyle("");
            }
        });

        locationField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                locationError.setText("Location is required");
                locationField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 2) {
                locationError.setText("Location must be at least 2 characters");
                locationField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() > 100) {
                locationError.setText("Location must be less than 100 characters");
                locationField.setStyle("-fx-border-color: red;");
            } else {
                locationError.setText("");
                locationField.setStyle("");
            }
        });

        descriptionArea.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                descError.setText("Description is required");
                descriptionArea.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 10) {
                descError.setText("Description must be at least 10 characters");
                descriptionArea.setStyle("-fx-border-color: red;");
            } else if (newValue.length() > 1000) {
                descError.setText("Description must be less than 1000 characters");
                descriptionArea.setStyle("-fx-border-color: red;");
            } else {
                descError.setText("");
                descriptionArea.setStyle("");
            }
        });

        imageField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                try {
                    new URL(newValue.trim());
                    imageError.setText("");
                    imageField.setStyle("");
                } catch (MalformedURLException e) {
                    imageError.setText("Please enter a valid URL");
                    imageField.setStyle("-fx-border-color: red;");
                }
            } else {
                imageError.setText("");
                imageField.setStyle("");
            }
        });

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        saveButton.setOnAction(e -> {
            // Clear previous error states
            nameError.setText("");
            locationError.setText("");
            descError.setText("");
            imageError.setText("");
            errorLabel.setText("");
            
            // Validate all fields
            boolean hasErrors = false;
            
            // Name validation
            if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                nameError.setText("University name is required");
                nameField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            } else if (nameField.getText().length() < 3) {
                nameError.setText("Name must be at least 3 characters");
                nameField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            }
            
            // Location validation
            if (locationField.getText() == null || locationField.getText().trim().isEmpty()) {
                locationError.setText("Location is required");
                locationField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            } else if (locationField.getText().length() < 2) {
                locationError.setText("Location must be at least 2 characters");
                locationField.setStyle("-fx-border-color: red;");
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
            
            // Image URL validation (optional)
            String imageUrl = imageField.getText().trim();
            if (!imageUrl.isEmpty()) {
                try {
                    new URL(imageUrl);
                } catch (MalformedURLException ex) {
                    imageError.setText("Please enter a valid URL");
                    imageField.setStyle("-fx-border-color: red;");
                    hasErrors = true;
                }
            }
            
            if (!hasErrors) {
                try {
                    University university = new University();
                    university.setName(nameField.getText().trim());
                    university.setLocation(locationField.getText().trim());
                    university.setDescription(descriptionArea.getText().trim());
                    university.setImage(imageUrl.isEmpty() ? null : imageUrl);

                    universityService.createUniversity(university);
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
            locationBox,
            descBox,
            imageBox,
            errorLabel,
            buttons
        );

        Scene scene = new Scene(form);
        dialog.setScene(scene);
        dialog.show();
    }

    private void showEditForm(Integer id) {
        Optional<University> universityOpt = universityService.getUniversityById(id);
        if (universityOpt.isEmpty()) {
            showError("University not found");
            return;
        }

        University university = universityOpt.get();
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.initOwner(stage);

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);
        form.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1px;");

        Label titleLabel = new Label("Edit University");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Name field with validation
        VBox nameBox = new VBox(5);
        Label nameLabel = new Label("University Name *");
        TextField nameField = new TextField(university.getName());
        Label nameError = new Label();
        nameError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        nameBox.getChildren().addAll(nameLabel, nameField, nameError);

        // Location field with validation
        VBox locationBox = new VBox(5);
        Label locationLabel = new Label("Location *");
        TextField locationField = new TextField(university.getLocation());
        Label locationError = new Label();
        locationError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        locationBox.getChildren().addAll(locationLabel, locationField, locationError);

        // Description field with validation
        VBox descBox = new VBox(5);
        Label descLabel = new Label("Description *");
        TextArea descriptionArea = new TextArea(university.getDescription());
        descriptionArea.setPrefRowCount(3);
        Label descError = new Label();
        descError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        descBox.getChildren().addAll(descLabel, descriptionArea, descError);

        // Image URL field with validation
        VBox imageBox = new VBox(5);
        Label imageLabel = new Label("Image URL");
        TextField imageField = new TextField(university.getImage());
        Label imageError = new Label();
        imageError.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        imageBox.getChildren().addAll(imageLabel, imageField, imageError);

        // Add validation listeners
        nameField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                nameError.setText("University name is required");
                nameField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 3) {
                nameError.setText("Name must be at least 3 characters");
                nameField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() > 100) {
                nameError.setText("Name must be less than 100 characters");
                nameField.setStyle("-fx-border-color: red;");
            } else {
                nameError.setText("");
                nameField.setStyle("");
            }
        });

        locationField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                locationError.setText("Location is required");
                locationField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 2) {
                locationError.setText("Location must be at least 2 characters");
                locationField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() > 100) {
                locationError.setText("Location must be less than 100 characters");
                locationField.setStyle("-fx-border-color: red;");
            } else {
                locationError.setText("");
                locationField.setStyle("");
            }
        });

        descriptionArea.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                descError.setText("Description is required");
                descriptionArea.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 10) {
                descError.setText("Description must be at least 10 characters");
                descriptionArea.setStyle("-fx-border-color: red;");
            } else if (newValue.length() > 1000) {
                descError.setText("Description must be less than 1000 characters");
                descriptionArea.setStyle("-fx-border-color: red;");
            } else {
                descError.setText("");
                descriptionArea.setStyle("");
            }
        });

        imageField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                try {
                    new URL(newValue.trim());
                    imageError.setText("");
                    imageField.setStyle("");
                } catch (MalformedURLException e) {
                    imageError.setText("Please enter a valid URL");
                    imageField.setStyle("-fx-border-color: red;");
                }
            } else {
                imageError.setText("");
                imageField.setStyle("");
            }
        });

        Button saveButton = new Button("Update");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        saveButton.setOnAction(e -> {
            // Clear previous error states
            nameError.setText("");
            locationError.setText("");
            descError.setText("");
            imageError.setText("");
            errorLabel.setText("");
            
            // Validate all fields
            boolean hasErrors = false;
            
            // Name validation
            if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                nameError.setText("University name is required");
                nameField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            } else if (nameField.getText().length() < 3) {
                nameError.setText("Name must be at least 3 characters");
                nameField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            }
            
            // Location validation
            if (locationField.getText() == null || locationField.getText().trim().isEmpty()) {
                locationError.setText("Location is required");
                locationField.setStyle("-fx-border-color: red;");
                hasErrors = true;
            } else if (locationField.getText().length() < 2) {
                locationError.setText("Location must be at least 2 characters");
                locationField.setStyle("-fx-border-color: red;");
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
            
            // Image URL validation (optional)
            String imageUrl = imageField.getText().trim();
            if (!imageUrl.isEmpty()) {
                try {
                    new URL(imageUrl);
                } catch (MalformedURLException ex) {
                    imageError.setText("Please enter a valid URL");
                    imageField.setStyle("-fx-border-color: red;");
                    hasErrors = true;
                }
            }
            
            if (!hasErrors) {
                try {
                    university.setName(nameField.getText().trim());
                    university.setLocation(locationField.getText().trim());
                    university.setDescription(descriptionArea.getText().trim());
                    university.setImage(imageUrl.isEmpty() ? null : imageUrl);

                    universityService.updateUniversity(id, university);
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
            locationBox,
            descBox,
            imageBox,
            errorLabel,
            buttons
        );

        Scene scene = new Scene(form);
        dialog.setScene(scene);
        dialog.show();
    }

    private void showUniversityView(Integer id) {
        Optional<University> universityOpt = universityService.getUniversityById(id);
        if (universityOpt.isEmpty()) {
            showError("University not found");
            return;
        }

        University university = universityOpt.get();
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.initOwner(stage);

        VBox view = new VBox(10);
        view.setPadding(new Insets(20));
        view.setAlignment(Pos.CENTER);
        view.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1px;");

        Label titleLabel = new Label(university.getName());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        if (university.getImage() != null && !university.getImage().isEmpty()) {
            try {
                ImageView imageView = new ImageView(new Image(university.getImage()));
                imageView.setFitHeight(200);
                imageView.setFitWidth(200);
                imageView.setPreserveRatio(true);
                view.getChildren().add(imageView);
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }

        Label locationLabel = new Label("Location: " + university.getLocation());
        Label descriptionLabel = new Label("Description: " + 
            (university.getDescription() != null ? university.getDescription() : "No description"));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(400);

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white;");
        closeButton.setOnAction(e -> dialog.close());

        view.getChildren().addAll(titleLabel, locationLabel, descriptionLabel, closeButton);
        Scene scene = new Scene(view);
        dialog.setScene(scene);
        dialog.show();
    }

    @FXML
    public void generatePdfReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save HTML Report");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("HTML Files", "*.html")
        );
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                StringBuilder htmlContent = new StringBuilder();
                htmlContent.append("<!DOCTYPE html>\n");
                htmlContent.append("<html>\n");
                htmlContent.append("<head>\n");
                htmlContent.append("  <title>Universities Report</title>\n");
                htmlContent.append("  <style>\n");
                htmlContent.append("    body { font-family: Arial, sans-serif; margin: 20px; }\n");
                htmlContent.append("    h1 { color: #1a73e8; }\n");
                htmlContent.append("    .university { margin-bottom: 15px; padding: 10px; border: 1px solid #ddd; }\n");
                htmlContent.append("    .university h2 { margin-top: 0; }\n");
                htmlContent.append("  </style>\n");
                htmlContent.append("</head>\n");
                htmlContent.append("<body>\n");
                htmlContent.append("  <h1>Universities Report</h1>\n");
                    
                    for (University university : universityService.getAllUniversities()) {
                    htmlContent.append("  <div class='university'>\n");
                    htmlContent.append("    <h2>").append(university.getName()).append("</h2>\n");
                    htmlContent.append("    <p><strong>Location:</strong> ").append(university.getLocation()).append("</p>\n");
                    if (university.getDescription() != null && !university.getDescription().isEmpty()) {
                        htmlContent.append("    <p><strong>Description:</strong> ").append(university.getDescription()).append("</p>\n");
                    }
                    htmlContent.append("  </div>\n");
                    }
                
                htmlContent.append("</body>\n");
                htmlContent.append("</html>");
                
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(htmlContent.toString());
                }
                
                showInfo("HTML Report generated successfully!");
                
                // Attempt to open the HTML file in the default browser
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(file.toURI());
                }
            } catch (IOException e) {
                showError("Error generating report: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public VBox getUniversityListView() {
        return universityListView;
    }

    @FXML
    public void refreshList() {
        List<University> universities = universityService.getAllUniversities();
        tableView.setItems(FXCollections.observableArrayList(universities));
        tableView.refresh();
    }

    private void deleteUniversity(Integer id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Are you sure you want to delete this university?",
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                universityService.deleteUniversity(id);
                refreshList();
            }
        });
    }

    public void setUserInfo(User user) {
        this.currentUser = user;
        if (user != null && userDisplayName != null) {
            userDisplayName.setText(user.getNom() + " " + user.getPrenom());
        }
    }
} 