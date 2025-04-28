package tn.badro.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainFx extends Application {
    private static final Logger LOGGER = Logger.getLogger(MainFx.class.getName());

    @Override
    public void start(Stage stage) {
        try {
            // Correct path to FXML file
            URL fxmlLocation = getClass().getResource("/Desktop/preference.fxml");
            if (fxmlLocation == null) {
                LOGGER.log(Level.SEVERE, "FXML non trouvé : /Desktop/preference.fxml");
                throw new IllegalStateException("FXML non trouvé : /Desktop/preference.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load(); //Utiliser Parent à la place de BorderPane

            Scene scene = new Scene(root, 1000, 600);
            stage.setTitle("Gestion des Préférences");
            stage.setScene(scene);
            stage.show();
            LOGGER.log(Level.INFO, "Application démarrée avec succès.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur au démarrage de l'application", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
