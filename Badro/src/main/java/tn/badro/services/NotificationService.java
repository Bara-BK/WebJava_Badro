package tn.badro.services;

import tn.badro.entities.Preferences;

import java.util.function.Consumer;

public class NotificationService {

    private Consumer<String> guiNotifier;

    public NotificationService(Consumer<String> guiNotifier) {
        this.guiNotifier = guiNotifier;
    }

    public void sendNotification(Preferences pref, String message) {
        String fullMessage = "Notification pour étudiant ID=" + pref.getId() + ": " + message;

        if (guiNotifier != null) {
            guiNotifier.accept(fullMessage); // Affiche visuellement
        }

        // Optionnel : conserver aussi la trace console
        System.out.println(fullMessage);
    }
}


