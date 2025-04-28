package tn.badro.services;

import javafx.collections.ObservableList;
import tn.badro.entities.Preferences;
import tn.badro.services.NotificationService;

import java.util.List;

public class MatchingService {

    private static NotificationService notificationService;

    public MatchingService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public static void detectAndNotifyMatch(Preferences newPref, ObservableList<Preferences> allPrefs) {
        for (Preferences existing : allPrefs) {
            if (existing == newPref) continue;

            if (existing.getDomain().equalsIgnoreCase(newPref.getDomain()) &&
                    existing.getCountry().equalsIgnoreCase(newPref.getCountry()) &&
                    existing.getPreferred_language().equalsIgnoreCase(newPref.getPreferred_language())) {

                // 🔔 Notification
                notificationService.sendNotification(existing, "Une nouvelle préférence similaire à la vôtre a été ajoutée.");
                notificationService.sendNotification(newPref, "Bonne nouvelle ! Un autre étudiant partage les mêmes préférences. Cela pourrait vous aider à mieux vous orienter.");
            }
        }
    }
}
