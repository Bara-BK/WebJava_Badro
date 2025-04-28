package tn.badro.Controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import tn.badro.services.NotificationService;
import tn.badro.services.PreferencesService;
import tn.badro.services.MatchingService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.badro.entities.Preferences;
import javafx.event.ActionEvent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.media.AudioClip;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class PreferenceController {
    private PreferencesService preferencesService = new PreferencesService();

    @FXML private TableView<Preferences> preferenceTable;
    @FXML private TableColumn<Preferences, String> climatColumn;
    @FXML private TableColumn<Preferences, String> countryColumn;
    @FXML private TableColumn<Preferences, String> domainColumn;
    @FXML private TableColumn<Preferences, String> preferredLanguageColumn;
    @FXML private TableColumn<Preferences, String> teachingModeColumn;
    @FXML private TableColumn<Preferences, String> universityTypeColumn;
    @FXML private TableColumn<Preferences, String> culturalActivitiesColumn;
    @FXML private TableColumn<Preferences, String> levelColumn;

    @FXML private ComboBox<String> countryComboBox;
    @FXML private RadioButton englishRadio, frenchRadio, arabicRadio;
    @FXML private RadioButton onlineRadio, inPersonRadio;
    @FXML private TextField levelTextField;
    @FXML private TextField UniversityType;
    @FXML private TextField culturalAct;
    @FXML private TextField Domain;
    @FXML private TextField Climat;
    @FXML private Button addPreferenceButton;
    @FXML private TabPane tabPane;
    @FXML private Tab tabAdd;
    @FXML private Button Update;
    @FXML private AnchorPane mainAnchorPane;



    @FXML
    public void initialize() {
        NotificationService notificationService = new NotificationService(this::showToast);
        MatchingService matchingService = new MatchingService(notificationService);

        // Liaison des colonnes avec les attributs de Preferences.java
        climatColumn.setCellValueFactory(new PropertyValueFactory<>("climat_pref"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
        domainColumn.setCellValueFactory(new PropertyValueFactory<>("domain"));
        preferredLanguageColumn.setCellValueFactory(new PropertyValueFactory<>("preferred_language"));
        teachingModeColumn.setCellValueFactory(new PropertyValueFactory<>("teaching_mode"));
        universityTypeColumn.setCellValueFactory(new PropertyValueFactory<>("university_type"));
        culturalActivitiesColumn.setCellValueFactory(new PropertyValueFactory<>("cultural_activities"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("language_level"));

        // Valeurs de test pour le ComboBox
        countryComboBox.getItems().addAll("France", "Germany", "Canada", "Japan");
        try {
            preferenceTable.getItems().addAll(preferencesService.recuperer());
        } catch (SQLException e) {
            e.printStackTrace();
            // Optionnel : afficher une alerte
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", null,"Impossible de charger les préférences depuis la base de données.");

        }
        preferenceTable.setRowFactory(tv -> {
            TableRow<Preferences> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Preferences selectedPref = row.getItem();
                    fillFormFields(selectedPref);  // <== À ajouter
                    tabPane.getSelectionModel().select(tabAdd); // change d'onglet vers "Add"
                }
            });
            return row;
        });

    }

    private void fillFormFields(Preferences p) {
        Climat.setText(p.getClimat_pref());
        countryComboBox.setValue(p.getCountry());
        Domain.setText(p.getDomain());
        setLanguageRadio(p.getPreferred_language());
        setTeachingModeRadio(p.getTeaching_mode());
        UniversityType.setText(p.getUniversity_type());
        culturalAct.setText(p.getCultural_activities());
        levelTextField.setText(p.getLanguage_level());

    }


    private void setLanguageRadio(String language) {
        // Réinitialiser toutes les radios
        englishRadio.setSelected(false);
        frenchRadio.setSelected(false);
        arabicRadio.setSelected(false);

        // Sélectionner celle qui correspond
        switch (language) {
            case "English" -> englishRadio.setSelected(true);
            case "French" -> frenchRadio.setSelected(true);
            case "Arabic" -> arabicRadio.setSelected(true);
        }
    }

    private void setTeachingModeRadio(String mode) {
        // Réinitialiser les deux radios
        onlineRadio.setSelected(false);
        inPersonRadio.setSelected(false);

        // Sélectionner celle qui correspond
        switch (mode) {
            case "Online" -> onlineRadio.setSelected(true);
            case "In-person classes" -> inPersonRadio.setSelected(true);
        }
    }




    @FXML
    public void handleAddPreference(ActionEvent event) {
        String climat = Climat.getText();
        String country = countryComboBox.getValue();
        String language = getSelectedLanguage();
        String teachingMode = getSelectedTeachingMode();
        String universityType =UniversityType.getText();
        String culturalActivities = culturalAct.getText();
        String domain = Domain.getText();
        String level = levelTextField.getText();

        // Étape 1 : Vérifie les champs vides
        if (climat == null || climat.trim().isEmpty() ||
                country == null || country.trim().isEmpty() ||
                domain == null || domain.trim().isEmpty() ||
                language == null || language.trim().isEmpty() ||
                teachingMode == null || teachingMode.trim().isEmpty() ||
                universityType == null || universityType.trim().isEmpty() ||
                culturalActivities == null || culturalActivities.trim().isEmpty() ||
                level == null || level.trim().isEmpty()) {

            showAlert(Alert.AlertType.WARNING, "Champs manquants", null,
                    "Veuillez remplir tous les champs avant d'ajouter une préférence.");
            return;
        }

        // Étape 2 : Vérifie que certains champs ne contiennent pas de chiffres
        if (climat.matches(".*\\d.*")) {
            showAlert(Alert.AlertType.ERROR, "Climat invalide", null, "Le champ 'climat' ne doit pas contenir de chiffres.");
            return;
        }
        if (domain.matches(".*\\d.*")) {
            showAlert(Alert.AlertType.ERROR, "Domaine invalide", null, "Le champ 'domaine' ne doit pas contenir de chiffres.");
            return;
        }
        if (universityType.matches(".*\\d.*")) {
            showAlert(Alert.AlertType.ERROR, "Type d'université invalide", null, "Le champ 'type d'université' ne doit pas contenir de chiffres.");
            return;
        }
        if (culturalActivities.matches(".*\\d.*")) {
            showAlert(Alert.AlertType.ERROR, "Activités culturelles invalides", null, "Le champ 'activités culturelles' ne doit pas contenir de chiffres.");
            return;
        }

        // Étape 2 : Contrôle du format du niveau
        if (!level.matches("^(A[1-2]|B[1-2]|C[1-2])$")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Niveau invalide");
            alert.setHeaderText(null);
            alert.setContentText("Le niveau doit être : A1, A2, B1, B2, C1 ou C2.");
            alert.showAndWait();
            return;
        }

        // Étape 3 : Créer l’objet et l’enregistrer en base
        Preferences newPref = new Preferences(climat, country, domain, language, teachingMode, universityType, culturalActivities, level);
        try {
            preferencesService.ajouter(newPref); // ← C’EST ICI que tu appelles la BDD
            preferenceTable.getItems().add(newPref); // ← Optionnel, ou appelle récupérer()
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", null,"Préférence ajoutée avec succès !");

            MatchingService.detectAndNotifyMatch(newPref, preferenceTable.getItems());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur base de données", null,"Une erreur est survenue : " + e.getMessage());
        }
    }
    private void clearForm() {
        countryComboBox.setValue(null);
        levelTextField.clear();
        // et réinitialise les sélections de tes autres champs (si ce sont des CheckBox, ComboBox, etc.)
    }


    @FXML
    private void modifierPreference() {
        Preferences selected = preferenceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", null, "Veuillez sélectionner une préférence à modifier.");
            return;
        }

        String climat = Climat.getText();
        String country = countryComboBox.getValue();
        String domain = Domain.getText();
        String language = getSelectedLanguage();
        String teachingMode = getSelectedTeachingMode();
        String universityType = UniversityType.getText();
        String culturalActivities = culturalAct.getText();
        String level = levelTextField.getText();

        // ✅ Étape 1 : Vérifie les champs vides
        if (climat == null || climat.isBlank() ||
                country == null || country.isBlank() ||
                domain == null || domain.isBlank() ||
                language == null || language.isBlank() ||
                teachingMode == null || teachingMode.isBlank() ||
                universityType == null || universityType.isBlank() ||
                culturalActivities == null || culturalActivities.isBlank() ||
                level == null || level.isBlank()) {

            showAlert(Alert.AlertType.WARNING, "Champs manquants", null, "Veuillez remplir tous les champs avant de modifier la préférence.");
            return;
        }

        // ✅ Étape 2 : Vérifie que certains champs ne soient pas numériques uniquement
        if (climat.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Climat invalide", "Erreur de saisie", "Le champ 'climat' ne doit pas contenir uniquement des chiffres.");
            return;
        }
        if (domain.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Domaine invalide", "Erreur de saisie", "Le champ 'domaine' ne doit pas contenir uniquement des chiffres.");
            return;
        }
        if (universityType.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Type d'université invalide", "Erreur de saisie", "Le champ 'type d'université' ne doit pas contenir uniquement des chiffres.");
            return;
        }
        if (culturalActivities.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Activités culturelles invalides", "Erreur de saisie", "Le champ 'activités culturelles' ne doit pas contenir uniquement des chiffres.");
            return;
        }

        // ✅ Étape 3 : Vérifie le niveau
        if (!level.matches("^(A[1-2]|B[1-2]|C[1-2])$")) {
            showAlert(Alert.AlertType.ERROR, "Niveau invalide", null, "Le niveau doit être : A1, A2, B1, B2, C1 ou C2.");
            return;
        }

        // ✅ Étape 4 : Mise à jour des données
        try {
            selected.setClimat_pref(climat);
            selected.setCountry(country);
            selected.setDomain(domain);
            selected.setPreferred_language(language);
            selected.setTeaching_mode(teachingMode);
            selected.setUniversity_type(universityType);
            selected.setCultural_activities(culturalActivities);
            selected.setLanguage_level(level);

            preferencesService.modifier(selected);
            preferenceTable.refresh();

            showAlert(Alert.AlertType.INFORMATION, "Modification réussie", null, "Préférence modifiée avec succès !");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur base de données", null, "Une erreur est survenue : " + e.getMessage());
        }
    }

    @FXML
    private void supprimerPreference() {
        Preferences selected = preferenceTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // Optionnel : Demander confirmation à l'utilisateur
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation de suppression");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Voulez-vous vraiment supprimer cette préférence ?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Suppression de la base de données
                preferencesService.supprimer(selected);

                // Suppression de la TableView
                preferenceTable.getItems().remove(selected);

                // Alerte de succès
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Suppression réussie");
                alert.setHeaderText(null);
                alert.setContentText("Préférence supprimée avec succès !");
                alert.showAndWait();
            }
        } else {
            showAlert(Alert.AlertType.ERROR,"Attention", null,"Veuillez sélectionner une préférence à supprimer.");
        }
    }



    @FXML
    public String getSelectedLanguage() {
        if (englishRadio.isSelected()) return "English";
        if (frenchRadio.isSelected()) return "French";
        if (arabicRadio.isSelected()) return "Arabic";
        return "";
    }

    @FXML
    public String getSelectedTeachingMode() {
        if (onlineRadio.isSelected()) return "Online";
        if (inPersonRadio.isSelected()) return "In-person classes";
        return "";
    }



    private boolean isLevelValid() {
        String level = levelTextField.getText();
        if (level != null && level.matches("^(A[1-2]|B[1-2]|C[1-2])$")) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Niveau invalide");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez entrer un niveau valide parmi : A1, A2, B1, B2, C1, C2.");
            alert.showAndWait();
            return false;
        }
    }


    public void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void showToast(String message) {
        if (mainAnchorPane == null || mainAnchorPane.getScene() == null) {
            System.err.println("Erreur : mainAnchorPane ou sa scène est null.");
            return;
        }

        Stage ownerStage = (Stage) mainAnchorPane.getScene().getWindow();

        Stage toastStage = new Stage();
        toastStage.initOwner(ownerStage);
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        Label label = new Label(message);
        label.setWrapText(true);
        label.setStyle(
                "-fx-background-color: linear-gradient(to right, #2193b0, #6dd5ed);" + // joli dégradé bleu
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 20px 30px;" +
                        "-fx-background-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 2);"
        );

        StackPane root = new StackPane(label);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: transparent;");
        root.setMaxWidth(400);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        toastStage.setScene(scene);

        // Position : bas droite de l'écran
        toastStage.setX(Screen.getPrimary().getVisualBounds().getMaxX() - 450);
        toastStage.setY(Screen.getPrimary().getVisualBounds().getMaxY() - 150);

        playNotificationSound();

        toastStage.show();

        // Animation d'apparition
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Fermeture automatique après 4s avec fondu de sortie
        PauseTransition delay = new PauseTransition(Duration.seconds(7));
        delay.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> toastStage.close());
            fadeOut.play();
        });
        delay.play();
    }

    public void playNotificationSound() {
        try {
            String soundPath = getClass().getResource("/sounds/ding.wav").toString(); // fais bien attention au chemin
            AudioClip clip = new AudioClip(soundPath);
            clip.setVolume(0.6); // Réglage du volume
            clip.play();         // Lecture du son
        } catch (Exception e) {
            System.err.println("Erreur de lecture du son : " + e.getMessage());
        }
    }

}



