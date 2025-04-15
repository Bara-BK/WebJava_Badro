package com.example.eventmanagement;

import com.example.eventmanagement.controller.EventController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        EventController eventController = new EventController();
        eventController.setStage(primaryStage);
        Scene scene = new Scene(eventController.getEventListView(), 800, 600);
        primaryStage.setTitle("Event Management");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}