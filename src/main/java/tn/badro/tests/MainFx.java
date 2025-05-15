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
            // Load the login FXML instead of main menu
            URL fxmlLocation = getClass().getResource("/Desktop/login.fxml");
            if (fxmlLocation == null) {
                LOGGER.log(Level.SEVERE, "FXML not found: /Desktop/login.fxml");
                throw new IllegalStateException("FXML not found: /Desktop/login.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setTitle("BADRO - Login");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
            LOGGER.log(Level.INFO, "Application started successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting the application", e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
