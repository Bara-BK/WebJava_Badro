package tn.badro.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import tn.badro.entities.Preferences;
import tn.badro.services.PreferencesService;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.sql.SQLException;

public class PreferencesController {
    @FXML private TableView<Preferences> tableView;
    @FXML private TableColumn<Preferences, String> colClimat, colCountry, colDomain, colLanguage, colTeaching, colUniversity, colActivities, colLanguageLevel;

    @FXML private TextField climatField, countryField, domainField, languageField, teachingModeField, universityTypeField, culturalActivitiesField, languageLevelField;

    private PreferencesService service;
    private ObservableList<Preferences> data;

    @FXML
    private Button Ajout;

    @FXML
    public void initialize() {
        service = new PreferencesService();
        data = FXCollections.observableArrayList();

        colClimat.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getClimat_pref()));
        colCountry.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCountry()));
        colDomain.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDomain()));
        colLanguage.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getPreferred_language()));
        colTeaching.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getTeaching_mode()));
        colUniversity.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getUniversity_type()));
        colActivities.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCultural_activities()));
        colLanguageLevel.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getLanguage_level()));

        try {
            data.addAll(service.recuperer());
            tableView.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void ajouter(ActionEvent event) {
        Preferences p = new Preferences(
                climatField.getText(),
                countryField.getText(),
                domainField.getText(),
                languageField.getText(),
                teachingModeField.getText(),
                universityTypeField.getText(),
                culturalActivitiesField.getText(),
                languageLevelField.getText()
        );

        try {
            service.ajouter(p);
            data.clear();
            data.addAll(service.recuperer());
            tableView.setItems(data);
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        climatField.clear();
        countryField.clear();
        domainField.clear();
        languageField.clear();
        teachingModeField.clear();
        universityTypeField.clear();
        culturalActivitiesField.clear();
        languageLevelField.clear();
    }
}
