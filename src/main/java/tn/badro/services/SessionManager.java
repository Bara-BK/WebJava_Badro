package tn.badro.services;

import tn.badro.entities.User;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class to manage user sessions
 */
public class SessionManager {
    private static SessionManager instance;
    private Map<String, Session> activeSessions;
    private static final long SESSION_TIMEOUT_MINUTES = 30;
    private User currentUser;
    private boolean loggedIn = false;

    private SessionManager() {
        activeSessions = new HashMap<>();
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Set the current logged-in user
     * @param user The user who has logged in
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.loggedIn = (user != null);
    }

    /**
     * Get the current logged-in user
     * @return The current user or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is currently logged in
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return loggedIn && currentUser != null;
    }

    /**
     * Log out the current user
     */
    public void logout() {
        this.currentUser = null;
        this.loggedIn = false;
    }
    
    /**
     * Get the current user's ID
     * @return The user ID or 0 if no user is logged in
     */
    public int getCurrentUserId() {
        return isLoggedIn() ? currentUser.getId() : 0;
    }

    /**
     * Check if the current user is an admin
     * @return true if the current user has admin role, false otherwise
     */
    public boolean isAdmin() {
        if (!isLoggedIn()) {
            return false;
        }
        String roles = currentUser.getRoles();
        return roles != null && (roles.contains("ADMIN") || roles.contains("admin"));
    }

    public String createSession(User user) {
        String sessionId = generateSessionId();
        Session session = new Session(user, LocalDateTime.now());
        activeSessions.put(sessionId, session);
        return sessionId;
    }

    public User getCurrentUser(String sessionId) {
        Session session = activeSessions.get(sessionId);
        if (session != null && !isSessionExpired(session)) {
            session.setLastActivity(LocalDateTime.now());
            return session.getUser();
        }
        return null;
    }

    public void invalidateSession(String sessionId) {
        activeSessions.remove(sessionId);
    }

    private boolean isSessionExpired(Session session) {
        return LocalDateTime.now().isAfter(
            session.getLastActivity().plusMinutes(SESSION_TIMEOUT_MINUTES)
        );
    }

    private String generateSessionId() {
        return java.util.UUID.randomUUID().toString();
    }

    private static class Session {
        private final User user;
        private LocalDateTime lastActivity;

        public Session(User user, LocalDateTime lastActivity) {
            this.user = user;
            this.lastActivity = lastActivity;
        }

        public User getUser() {
            return user;
        }

        public LocalDateTime getLastActivity() {
            return lastActivity;
        }

        public void setLastActivity(LocalDateTime lastActivity) {
            this.lastActivity = lastActivity;
        }
    }
} 