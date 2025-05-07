package tn.badro.entities;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collections;

public class NotificationManager {
    private static NotificationManager instance;
    private final ObservableList<Notification> notifications;

    private NotificationManager() {
        notifications = FXCollections.observableArrayList();
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void addNotification(Notification notification) {
        notifications.add(0, notification); // Add to top (most recent first)
    }

    public ObservableList<Notification> getNotifications() {
        return notifications;
    }

    public long getUnreadCount() {
        return notifications.stream().filter(n -> !n.isRead()).count();
    }

    public void markAllAsRead() {
        notifications.forEach(n -> n.setRead(true));
    }
} 