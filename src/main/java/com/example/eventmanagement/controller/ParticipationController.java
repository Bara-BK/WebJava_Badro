package com.example.eventmanagement.controller;

import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.Participation;
import com.example.eventmanagement.service.EventService;
import com.example.eventmanagement.service.ParticipationService;
import com.example.eventmanagement.util.TicketPDFGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ParticipationController {
    private final EventService eventService;
    private final ParticipationService participationService;
    private Stage stage;
    private Integer currentEventId;
    private Integer currentParticipationId;
    private Runnable backToEventView;
    private FilteredList<Participation> filteredParticipations;
    private SortedList<Participation> sortedParticipations;

    // Common data
    private ObservableList<String> sortOptions = FXCollections.observableArrayList("Sort by Name", "Sort by Date", "Sort by ID");

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

    public void setBackToEventView(Runnable backToEventView) {
        this.backToEventView = backToEventView;
    }

    public BorderPane getParticipationListView(Integer eventId) throws IOException {
        currentEventId = eventId;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ParticipationListView.fxml"));
        loader.setController(this);
        BorderPane pane = loader.load();
        pane.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        initializeParticipationList();
        initializeSortChoiceBox();
        populateParticipationList();
        return pane;
    }

    private void initializeParticipationList() {
        List<Participation> allParticipations = participationService.getParticipationsByEventId(currentEventId);
        ObservableList<Participation> observableParticipations = FXCollections.observableArrayList(allParticipations);
        filteredParticipations = new FilteredList<>(observableParticipations, p -> true);
        sortedParticipations = new SortedList<>(filteredParticipations);
        sortedParticipations.setComparator(Comparator.comparing(Participation::getNomParticipant, String.CASE_INSENSITIVE_ORDER));
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
                sortedParticipations.setComparator(Comparator.comparing(Participation::getNomParticipant, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Sort by Date":
                sortedParticipations.setComparator(Comparator.comparing(Participation::getDateInscription, Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case "Sort by ID":
                sortedParticipations.setComparator(Comparator.comparing(Participation::getParticipantId));
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
            return participation.getNomParticipant().toLowerCase().contains(searchText) ||
                   (participation.getTicketCode() != null && participation.getTicketCode().toLowerCase().contains(searchText));
        });
        populateParticipationList();
    }

    private void populateParticipationList() {
        participationsBox.getChildren().clear();
        if (sortedParticipations.isEmpty()) {
            Label noParticipationsLabel = new Label("No participants found. Add one!");
            noParticipationsLabel.getStyleClass().add("no-events");
            participationsBox.getChildren().add(noParticipationsLabel);
        } else {
            for (Participation p : sortedParticipations) {
                HBox participationBox = new HBox(15);
                participationBox.getStyleClass().add("participation-item");
                participationBox.setAlignment(Pos.CENTER_LEFT);

                Label nameLabel = new Label(p.getNomParticipant());
                nameLabel.getStyleClass().add("participation-title");

                Button viewButton = new Button("View");
                viewButton.getStyleClass().add("primary-button");
                viewButton.setOnAction(e -> {
                    try {
                        showParticipationView(p.getParticipantId());
                    } catch (IOException ex) {
                        showError("Failed to load participation view");
                    }
                });

                Button editButton = new Button("Edit");
                editButton.getStyleClass().add("primary-button");
                editButton.setOnAction(e -> {
                    try {
                        showEditParticipationForm(p.getParticipantId());
                    } catch (IOException ex) {
                        showError("Failed to load edit form");
                    }
                });

                Button deleteButton = new Button("Delete");
                deleteButton.getStyleClass().add("delete-button");
                deleteButton.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + p.getNomParticipant() + "?", ButtonType.YES, ButtonType.NO);
                    confirm.getDialogPane().getStyleClass().add("alert");
                    confirm.showAndWait().ifPresent(type -> {
                        if (type == ButtonType.YES) {
                            participationService.deleteParticipation(p.getParticipantId());
                            initializeParticipationList();
                            populateParticipationList();
                        }
                    });
                });

                participationBox.getChildren().addAll(nameLabel, viewButton, editButton, deleteButton);
                participationsBox.getChildren().add(participationBox);
            }
        }
    }

    @FXML
    private void handleCreateParticipation() throws IOException {
        Optional<Event> eventOpt = eventService.getEventById(currentEventId);
        String eventName = eventOpt.isPresent() ? eventOpt.get().getTitre() : "Unknown Event";

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateParticipationForm.fxml"));
        loader.setController(this);
        Scene scene = new Scene(loader.load(), 400, 500);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        eventNameField.setText(eventName);
        ticketCodeField.setText(generateRandomTicketCode());
        stage.setScene(scene);
    }

    @FXML
    private void handleSaveParticipation() {
        try {
            String name = nameField.getText();
            if (name.length() < 2 || name.length() > 255) {
                throw new IllegalArgumentException("Name must be 2-255 characters");
            }
            String phone = phoneField.getText();
            if (!phone.isEmpty() && (phone.length() < 7 || phone.length() > 11 || !phone.matches("\\d+"))) {
                throw new IllegalArgumentException("Phone must be 7-11 digits");
            }
            String paymentMethod = paymentMethodField.getText();
            if (!paymentMethod.isEmpty() && !paymentMethod.matches("Credit|Debit|Cash|Online")) {
                throw new IllegalArgumentException("Payment method must be Credit, Debit, Cash, or Online");
            }

            Participation p = new Participation();
            p.setIdEvent(currentEventId);
            p.setNomParticipant(name);
            p.setDateInscription(dateField.getValue());
            p.setEvenementNom(eventNameField.getText());
            p.setTelephoneNumber(phone.isEmpty() ? null : Integer.parseInt(phone));
            p.setTicketCode(ticketCodeField.getText());
            p.setPaimentMethod(paymentMethod.isEmpty() ? null : paymentMethod);
            participationService.createParticipation(p);
            showParticipationListView();
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
        } catch (IOException ex) {
            showError("Failed to return to participation list");
        }
    }

    private void showEditParticipationForm(Integer id) throws IOException {
        Optional<Participation> participationOpt = participationService.getParticipationById(id);
        if (participationOpt.isEmpty()) {
            showError("Participation not found");
            return;
        }

        currentParticipationId = id;
        Participation p = participationOpt.get();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditParticipationForm.fxml"));
        loader.setController(this);
        Scene scene = new Scene(loader.load(), 400, 500);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        nameField.setText(p.getNomParticipant());
        dateField.setValue(p.getDateInscription());
        eventNameField.setText(p.getEvenementNom());
        phoneField.setText(p.getTelephoneNumber() != null ? p.getTelephoneNumber().toString() : "");
        ticketCodeField.setText(p.getTicketCode());
        paymentMethodField.setText(p.getPaimentMethod());
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
            Participation p = participationOpt.get();
            String name = nameField.getText();
            if (name.length() < 2 || name.length() > 255) {
                throw new IllegalArgumentException("Name must be 2-255 characters");
            }
            String phone = phoneField.getText();
            if (!phone.isEmpty() && (phone.length() < 7 || phone.length() > 11 || !phone.matches("\\d+"))) {
                throw new IllegalArgumentException("Phone must be 7-11 digits");
            }
            String paymentMethod = paymentMethodField.getText();
            if (!paymentMethod.isEmpty() && !paymentMethod.matches("Credit|Debit|Cash|Online")) {
                throw new IllegalArgumentException("Payment method must be Credit, Debit, Cash, or Online");
            }

            p.setNomParticipant(name);
            p.setDateInscription(dateField.getValue());
            p.setEvenementNom(eventNameField.getText());
            p.setTelephoneNumber(phone.isEmpty() ? null : Integer.parseInt(phone));
            p.setTicketCode(ticketCodeField.getText());
            p.setPaimentMethod(paymentMethod.isEmpty() ? null : paymentMethod);
            participationService.updateParticipation(p.getParticipantId(), p);
            showParticipationListView();
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
        } catch (IOException ex) {
            showError("Failed to return to participation list");
        }
    }

    private void showParticipationView(Integer id) throws IOException {
        Optional<Participation> participationOpt = participationService.getParticipationById(id);
        if (participationOpt.isEmpty()) {
            showError("Participation not found");
            return;
        }

        Participation p = participationOpt.get();
        currentParticipationId = id; // Store for export
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ParticipationView.fxml"));
        loader.setController(this);
        Scene scene = new Scene(loader.load(), 400, 500);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        nameLabelView.setText("Participant: " + p.getNomParticipant());
        dateText.setText("Date: " + (p.getDateInscription() != null ? p.getDateInscription().toString() : "None"));
        eventNameText.setText("Event: " + (p.getEvenementNom() != null ? p.getEvenementNom() : "None"));
        phoneText.setText("Phone: " + (p.getTelephoneNumber() != null ? p.getTelephoneNumber().toString() : "None"));
        ticketCodeText.setText("Ticket Code: " + (p.getTicketCode() != null ? p.getTicketCode() : "None"));
        paymentMethodText.setText("Payment Method: " + (p.getPaimentMethod() != null ? p.getPaimentMethod() : "None"));
        stage.setScene(scene);
    }

    @FXML
    private void handleExportTicket() {
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
            String filePath = TicketPDFGenerator.generateTicketPDF(participation, event);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Ticket exported to " + filePath, ButtonType.OK);
            alert.getDialogPane().getStyleClass().add("alert");
            alert.showAndWait();
        } catch (Exception e) {
            showError("Failed to export ticket: " + e.getMessage());
        }
    }

    private void showParticipationListView() throws IOException {
        Scene scene = new Scene(getParticipationListView(currentEventId), 400, 500);
        stage.setScene(scene);
    }

    @FXML
    private void handleCancel() throws IOException {
        showParticipationListView();
    }

    @FXML
    private void handleBackToEvent() {
        if (backToEventView != null) {
            backToEventView.run();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
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