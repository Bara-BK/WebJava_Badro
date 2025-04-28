package com.example.eventmanagement;

import com.example.eventmanagement.controller.EventController;
import com.example.eventmanagement.service.EventService;
import com.example.eventmanagement.service.ParticipationService;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            EventService eventService = new EventService();
            ParticipationService participationService = new ParticipationService();
            EventController eventController = new EventController(eventService, participationService);
            eventController.setStage(primaryStage);
            eventController.showEventListView();
            primaryStage.setTitle("Event Management");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to start application: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}