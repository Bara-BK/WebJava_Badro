package tn.badro.Controllers;

import tn.badro.entities.Event;
import tn.badro.entities.Participation;
import tn.badro.entities.User;
import tn.badro.services.EventService;
import tn.badro.services.ParticipationService;
import tn.badro.services.SessionManager;
import tn.badro.util.TicketPDFGenerator;
import tn.badro.util.SmsNotificationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.DatePicker;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.awt.Desktop;
import java.time.LocalDate;

public class ParticipationController {
    private final EventService eventService;
    private final ParticipationService participationService;
    private Stage stage;
    private Integer currentEventId;
    private Integer currentParticipationId;
    private Supplier<Void> backToEventCallback;
    private FilteredList<Participation> filteredParticipations;
    private SortedList<Participation> sortedParticipations;
    private User currentUser;
    private SessionManager sessionManager = SessionManager.getInstance();

    // Common data
    private ObservableList<String> sortOptions = FXCollections.observableArrayList("Sort by Name", "Sort by Date", "Sort by Status");

    // ParticipationListView.fxml
    @FXML private BorderPane root;
    @FXML private ImageView logoView;
    @FXML private Label titleLabel;
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> sortChoiceBox;
    @FXML private Button createButton;
    @FXML private Button backButton;
    @FXML private VBox participationsBox;

    // CreateParticipationForm.fxml / EditParticipationForm.fxml
    @FXML private VBox form;
    @FXML private TextField nameField;
    @FXML private DatePicker dateField;
    @FXML private TextField eventNameField;
    @FXML private TextField phoneField;
    @FXML private TextField ticketCodeField;
    @FXML private TextField paymentMethodField;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // ParticipationView.fxml
    @FXML private VBox view;
    @FXML private Label nameLabelView;
    @FXML private Text dateText;
    @FXML private Text eventNameText;
    @FXML private Text phoneText;
    @FXML private Text ticketCodeText;
    @FXML private Text paymentMethodText;
    @FXML private Button exportButton;

    public ParticipationController(EventService eventService, ParticipationService participationService) {
        this.eventService = eventService;
        this.participationService = participationService;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setBackToEventView(Supplier<Void> callback) {
        this.backToEventCallback = callback;
    }
    
    public void setUserInfo(User user) {
        this.currentUser = user;
    }

    public Parent getParticipationListView(Integer eventId) throws IOException {
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }
        
        currentEventId = eventId;
        Optional<Event> eventOpt = eventService.getEventById(eventId);
        if (eventOpt.isEmpty()) {
            throw new IllegalArgumentException("Event not found: " + eventId);
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Desktop/ParticipationListView.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            
            Event event = eventOpt.get();
            titleLabel.setText("Participants: " + event.getTitre());
            
            initializeParticipationList(eventId);
            initializeSortChoiceBox();
            populateParticipationList();
            
            return root;
        } catch (Exception e) {
            System.err.println("Error loading ParticipationListView.fxml: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to load participation view: " + e.getMessage(), e);
        }
    }

    private void initializeParticipationList(Integer eventId) {
        List<Participation> allParticipations = participationService.getParticipationsByEventId(eventId);
        ObservableList<Participation> observableParticipations = FXCollections.observableArrayList(allParticipations);
        filteredParticipations = new FilteredList<>(observableParticipations, p -> true);
        sortedParticipations = new SortedList<>(filteredParticipations);
        sortedParticipations.setComparator(Comparator.comparing(p -> p.getNomParticipant(), String.CASE_INSENSITIVE_ORDER));
    }

    private void initializeSortChoiceBox() {
        sortChoiceBox.setItems(sortOptions);
        sortChoiceBox.setValue("Sort by Name");
    }

    @FXML
    private void sortParticipations() {
        String selectedSort = sortChoiceBox.getValue();
        switch (selectedSort) {
            case "Sort by Name":
                sortedParticipations.setComparator(Comparator.comparing(p -> p.getNomParticipant(), String.CASE_INSENSITIVE_ORDER));
                break;
            case "Sort by Date":
                sortedParticipations.setComparator(Comparator.comparing(Participation::getDateInscription, Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case "Sort by Status":
                // Assuming paimentMethod field is used for status - adjust as needed
                sortedParticipations.setComparator(Comparator.comparing(p -> p.getPaimentMethod() != null ? p.getPaimentMethod() : "", String.CASE_INSENSITIVE_ORDER));
                break;
        }
        populateParticipationList();
    }

    @FXML
    private void filterParticipations() {
        String searchText = searchField.getText().toLowerCase().trim();
        filteredParticipations.setPredicate(participation -> {
            if (searchText.isEmpty()) {
                return true;
            }
            if (participation.getNomParticipant().toLowerCase().contains(searchText)) {
                return true;
            }
            if (participation.getTicketCode() != null && participation.getTicketCode().toLowerCase().contains(searchText)) {
                return true;
            }
            return false;
        });
        populateParticipationList();
    }

    private void populateParticipationList() {
        participationsBox.getChildren().clear();
        if (sortedParticipations.isEmpty()) {
            Label noParticipationsLabel = new Label("No participants found. Add one!");
            noParticipationsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6C757D;");
            
            VBox emptyState = new VBox(20, noParticipationsLabel);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(50));
            emptyState.setPrefWidth(participationsBox.getPrefWidth());
            emptyState.setPrefHeight(300);
            
            participationsBox.getChildren().add(emptyState);
        } else {
            for (Participation participation : sortedParticipations) {
                participationsBox.getChildren().add(createParticipationCard(participation));
            }
        }
    }

    private VBox createParticipationCard(Participation participation) {
        // Create a modern card for the participation
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        card.setPrefWidth(Double.MAX_VALUE);
        
        // Top row with name and status
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(participation.getNomParticipant());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #212529;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Using paimentMethod as status for display purposes
        String status = participation.getPaimentMethod() != null ? participation.getPaimentMethod() : "Pending";
        Label statusLabel = new Label(status);
        String statusColor = "#4B9CD3"; // Default blue
        switch (status.toLowerCase()) {
            case "confirmed":
            case "cash":
            case "card":
                statusColor = "#28a745"; // Green
                break;
            case "pending":
                statusColor = "#ffc107"; // Yellow
                break;
            case "cancelled":
                statusColor = "#dc3545"; // Red
                break;
        }
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-background-color: " + statusColor + 
                          "; -fx-background-radius: 3px; -fx-padding: 2 10 2 10;");
        
        topRow.getChildren().addAll(nameLabel, spacer, statusLabel);
        
        // Middle section - details
        VBox detailsBox = new VBox(5);
        
        if (participation.getEvenementNom() != null && !participation.getEvenementNom().isEmpty()) {
            Label eventLabel = new Label("Event: " + participation.getEvenementNom());
            eventLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6C757D;");
            detailsBox.getChildren().add(eventLabel);
        }
        
        if (participation.getTelephoneNumber() != null) {
            Label phoneLabel = new Label("Phone: " + participation.getTelephoneNumber());
            phoneLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6C757D;");
            detailsBox.getChildren().add(phoneLabel);
        }
        
        if (participation.getTicketCode() != null && !participation.getTicketCode().isEmpty()) {
            Label ticketLabel = new Label("Ticket: " + participation.getTicketCode());
            ticketLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6C757D;");
            detailsBox.getChildren().add(ticketLabel);
        }
        
        // Date if available
        if (participation.getDateInscription() != null) {
            Label dateLabel = new Label("Date: " + participation.getDateInscription().toString());
            dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6C757D;");
            detailsBox.getChildren().add(dateLabel);
        }
        
        Separator separator = new Separator();
        separator.setStyle("-fx-padding: 5 0;");
        
        // Buttons for actions
        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);
        
        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #5CB85C; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 5;");
        editButton.setOnAction(e -> {
            try {
                showEditParticipationForm(participation.getParticipantId());
            } catch (IOException ex) {
                showError("Failed to load edit form: " + ex.getMessage());
            }
        });
        
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 5;");
        deleteButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete participant " + participation.getNomParticipant() + "?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(type -> {
                if (type == ButtonType.YES) {
                    participationService.deleteParticipation(participation.getParticipantId());
                    initializeParticipationList(currentEventId);
                    populateParticipationList();
                }
            });
        });
        
        buttonRow.getChildren().addAll(deleteButton, editButton);
        
        // Add all components to card
        card.getChildren().addAll(topRow, detailsBox, separator, buttonRow);
        
        return card;
    }

    @FXML
    private void handleCreateParticipation() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        java.net.URL fxmlUrl = getClass().getResource("/Desktop/CreateParticipationForm.fxml");
        if (fxmlUrl == null) {
            throw new IOException("Cannot find /Desktop/CreateParticipationForm.fxml");
        }
        loader.setLocation(fxmlUrl);
        loader.setController(this);
        Scene scene = new Scene(loader.load(), 400, 500);
        java.net.URL cssUrl = getClass().getResource("/styles/main.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        stage.setScene(scene);
    }

    @FXML
    private void handleSaveParticipation() {
        try {
            Participation participation = new Participation();
            participation.setIdEvent(currentEventId);
            participation.setNomParticipant(nameField.getText());
            
            // Set the registration date if provided
            if (dateField.getValue() != null) {
                participation.setDateInscription(dateField.getValue());
            } else {
                participation.setDateInscription(LocalDate.now());
            }
            
            // Store the phone number - convert to Integer if not empty
            if (!phoneField.getText().isEmpty()) {
                try {
                    participation.setTelephoneNumber(Integer.parseInt(phoneField.getText()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Phone number must be numeric");
                }
            }
            
            // Handle fields that don't directly match between UI and entity
            participation.setTicketCode(ticketCodeField.getText());
            participation.setPaimentMethod(paymentMethodField.getText());
            
            // Get event name for reference
            Optional<Event> eventOpt = eventService.getEventById(currentEventId);
            if (eventOpt.isPresent()) {
                participation.setEvenementNom(eventOpt.get().getTitre());
            }
            
            participationService.createParticipation(participation);
            stage.setScene(new Scene(getParticipationListView(currentEventId), 1200, 800));
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
        } catch (IOException ex) {
            showError("Failed to return to participation list: " + ex.getMessage());
        }
    }

    private void showEditParticipationForm(Integer id) throws IOException {
        Optional<Participation> participationOpt = participationService.getParticipationById(id);
        if (participationOpt.isEmpty()) {
            showError("Participation not found");
            return;
        }

        currentParticipationId = id;
        Participation participation = participationOpt.get();
        FXMLLoader loader = new FXMLLoader();
        java.net.URL fxmlUrl = getClass().getResource("/Desktop/EditParticipationForm.fxml");
        if (fxmlUrl == null) {
            throw new IOException("Cannot find /Desktop/EditParticipationForm.fxml");
        }
        loader.setLocation(fxmlUrl);
        loader.setController(this);
        Scene scene = new Scene(loader.load(), 400, 500);
        java.net.URL cssUrl = getClass().getResource("/styles/main.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        nameField.setText(participation.getNomParticipant());
        
        // Set the date if available
        if (participation.getDateInscription() != null) {
            dateField.setValue(participation.getDateInscription());
        }
        
        // Set the event name if available
        if (participation.getEvenementNom() != null) {
            eventNameField.setText(participation.getEvenementNom());
        }
        
        // Convert phone number if available
        if (participation.getTelephoneNumber() != null) {
            phoneField.setText(participation.getTelephoneNumber().toString());
        } else {
            phoneField.setText("");
        }
        
        ticketCodeField.setText(participation.getTicketCode());
        paymentMethodField.setText(participation.getPaimentMethod());
        stage.setScene(scene);
    }

    @FXML
    private void handleUpdateParticipation() {
        try {
            Optional<Participation> participationOpt = participationService.getParticipationById(currentParticipationId);
            if (participationOpt.isEmpty()) {
                showError("Participation not found");
                return;
            }
            Participation participation = participationOpt.get();
            participation.setNomParticipant(nameField.getText());
            
            // Update date if changed
            if (dateField.getValue() != null) {
                participation.setDateInscription(dateField.getValue());
            }
            
            // Store the phone number - convert to Integer if not empty
            if (!phoneField.getText().isEmpty()) {
                try {
                    participation.setTelephoneNumber(Integer.parseInt(phoneField.getText()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Phone number must be numeric");
                }
            } else {
                participation.setTelephoneNumber(null);
            }
            
            participation.setTicketCode(ticketCodeField.getText());
            participation.setPaimentMethod(paymentMethodField.getText());
            
            participationService.updateParticipation(currentParticipationId, participation);
            stage.setScene(new Scene(getParticipationListView(currentEventId), 1200, 800));
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
        } catch (IOException ex) {
            showError("Failed to return to participation list: " + ex.getMessage());
        }
    }

    @FXML
    private void handleBackToEvent() {
        if (backToEventCallback != null) {
            backToEventCallback.get();
        }
    }

    @FXML
    private void handleCancel() throws IOException {
        stage.setScene(new Scene(getParticipationListView(currentEventId), 1200, 800));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    @FXML
    private void handleExportTicket() {
        try {
            // Get current participation and event
            Optional<Participation> participationOpt = participationService.getParticipationById(currentParticipationId);
            if (participationOpt.isEmpty()) {
                showError("Participation not found");
                return;
            }
            Participation participation = participationOpt.get();
            Optional<Event> eventOpt = eventService.getEventById(participation.getIdEvent());
            if (eventOpt.isEmpty()) {
                showError("Event not found");
                return;
            }
            Event event = eventOpt.get();
            try {
                String pdfPath = TicketPDFGenerator.generateTicket(participation, event);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(new File(pdfPath));
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", "Ticket generated successfully at: " + pdfPath);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate ticket: " + e.getMessage());
            }
        } catch (Exception e) {
            showError("Failed to export ticket: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("alert");
        alert.showAndWait();
    }

    private String generateRandomTicketCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }
}