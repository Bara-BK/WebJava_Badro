package tn.badro.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import tn.badro.services.SessionManager;

/**
 * Singleton class to manage in-app notifications
 */
public class NotificationManager {
    private static NotificationManager instance;
    private final Map<Integer, List<Notification>> userNotifications;
    private final Map<Integer, Runnable> userCallbacks;
    private final SessionManager sessionManager;
    
    private NotificationManager() {
        userNotifications = new ConcurrentHashMap<>();
        userCallbacks = new ConcurrentHashMap<>();
        sessionManager = SessionManager.getInstance();
    }
    
    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }
    
    /**
     * Add a notification for a specific user
     * @param userId The user ID to add the notification for
     * @param notification The notification to add
     */
    public void addNotificationForUser(int userId, Notification notification) {
        userNotifications.computeIfAbsent(userId, k -> new ArrayList<>()).add(notification);
        
        // Execute callback for this user if exists
        if (userCallbacks.containsKey(userId)) {
            Runnable callback = userCallbacks.get(userId);
            if (callback != null) {
                callback.run();
            }
        }
        
        System.out.println("Added notification for user ID " + userId + ": " + notification.getMessage());
    }
    
    /**
     * Add a notification to the current logged-in user
     * @param notification The notification to add
     */
    public void addNotification(Notification notification) {
        if (!sessionManager.isLoggedIn()) {
            System.err.println("Cannot add notification: No user logged in");
            return;
        }
        
        int currentUserId = sessionManager.getCurrentUserId();
        addNotificationForUser(currentUserId, notification);
    }
    
    /**
     * Get all notifications for the current user
     * @return List of all notifications for the current user
     */
    public List<Notification> getNotifications() {
        if (!sessionManager.isLoggedIn()) {
            return new ArrayList<>();
        }
        
        int currentUserId = sessionManager.getCurrentUserId();
        return getNotificationsForUser(currentUserId);
    }
    
    /**
     * Get notifications for a specific user
     * @param userId The user ID to get notifications for
     * @return List of notifications for the specified user
     */
    public List<Notification> getNotificationsForUser(int userId) {
        List<Notification> notifications = userNotifications.get(userId);
        return notifications != null ? new ArrayList<>(notifications) : new ArrayList<>();
    }
    
    /**
     * Get the count of unread notifications for the current user
     * @return Number of unread notifications for the current user
     */
    public int getUnreadCount() {
        if (!sessionManager.isLoggedIn()) {
            return 0;
        }
        
        int currentUserId = sessionManager.getCurrentUserId();
        return getUnreadCountForUser(currentUserId);
    }
    
    /**
     * Get the count of unread notifications for a specific user
     * @param userId The user ID to get the unread count for
     * @return Number of unread notifications for the specified user
     */
    public int getUnreadCountForUser(int userId) {
        List<Notification> notifications = userNotifications.get(userId);
        if (notifications == null) {
            return 0;
        }
        
        return (int) notifications.stream()
                .filter(notification -> !notification.isRead())
                .count();
    }
    
    /**
     * Mark all notifications as read for the current user
     */
    public void markAllAsRead() {
        if (!sessionManager.isLoggedIn()) {
            return;
        }
        
        int currentUserId = sessionManager.getCurrentUserId();
        markAllAsReadForUser(currentUserId);
    }
    
    /**
     * Mark all notifications as read for a specific user
     * @param userId The user ID to mark notifications as read for
     */
    public void markAllAsReadForUser(int userId) {
        List<Notification> notifications = userNotifications.get(userId);
        if (notifications != null) {
            notifications.forEach(notification -> notification.setRead(true));
            
            // Execute callback for this user if exists
            if (userCallbacks.containsKey(userId)) {
                Runnable callback = userCallbacks.get(userId);
                if (callback != null) {
                    callback.run();
                }
            }
        }
    }
    
    /**
     * Clear all notifications for the current user
     */
    public void clearAll() {
        if (!sessionManager.isLoggedIn()) {
            return;
        }
        
        int currentUserId = sessionManager.getCurrentUserId();
        clearAllForUser(currentUserId);
    }
    
    /**
     * Clear all notifications for a specific user
     * @param userId The user ID to clear notifications for
     */
    public void clearAllForUser(int userId) {
        List<Notification> notifications = userNotifications.get(userId);
        if (notifications != null) {
            notifications.clear();
            
            // Execute callback for this user if exists
            if (userCallbacks.containsKey(userId)) {
                Runnable callback = userCallbacks.get(userId);
                if (callback != null) {
                    callback.run();
                }
            }
        }
    }
    
    /**
     * Set a callback to be called when a notification is added for the current user
     * @param callback The callback to be called
     */
    public void setOnNotificationAddedCallback(Runnable callback) {
        if (!sessionManager.isLoggedIn()) {
            return;
        }
        
        int currentUserId = sessionManager.getCurrentUserId();
        setOnNotificationAddedCallbackForUser(currentUserId, callback);
    }
    
    /**
     * Set a callback to be called when a notification is added for a specific user
     * @param userId The user ID to set the callback for
     * @param callback The callback to be called
     */
    public void setOnNotificationAddedCallbackForUser(int userId, Runnable callback) {
        userCallbacks.put(userId, callback);
    }
} 