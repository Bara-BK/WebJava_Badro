package tn.badro.services;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.badro.entities.ExperienceLike;
import tn.badro.entities.User;
import tn.badro.entities.Notification;
import tn.badro.entities.NotificationManager;
import tn.badro.tools.MyDataBase;

public class LikeService {
    private Connection connection;
    private UserService userService;
    private ExperienceService experienceService;
    private NotificationManager notificationManager;
    
    public LikeService() {
        connection = MyDataBase.getInstance().getCnx();
        userService = new UserService();
        experienceService = new ExperienceService();
        notificationManager = NotificationManager.getInstance();
    }
    
    public boolean addLike(int userId, int experienceId) {
        // First check if the user already liked this experience
        if (hasUserLiked(userId, experienceId)) {
            return false; // Already liked
        }
        
        String query = "INSERT INTO experience_like (user_id, experience_id, date_liked) VALUES (?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, userId);
            pst.setInt(2, experienceId);
            pst.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            
            int rowsAffected = pst.executeUpdate();
            
            if (rowsAffected > 0) {
                // Send notification to the experience owner
                sendLikeNotification(userId, experienceId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding like: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Sends a notification to the experience owner when someone likes their experience
     */
    private void sendLikeNotification(int likerId, int experienceId) {
        try {
            // Get experience details
            var experience = experienceService.getById(experienceId);
            
            if (experience == null) {
                return;
            }
            
            // Only send notification if liker is not the experience owner
            int experienceOwnerId = experience.getUserId();
            
            if (experienceOwnerId == likerId) {
                return; // Don't notify yourself
            }
            
            // Get liker's name
            Optional<User> likerOpt = userService.getUserById(likerId);
            String likerName = likerOpt.map(u -> u.getNom() + " " + u.getPrenom()).orElse("Someone");
            
            // Create notification message
            String notificationMessage = likerName + " liked your experience: \"" + experience.getTitle() + "\"";
            
            // Add notification directly to the experience owner's notifications
            notificationManager.addNotificationForUser(experienceOwnerId, new Notification(notificationMessage));
            
            System.out.println("Like notification sent to user ID " + experienceOwnerId + ": " + notificationMessage);
        } catch (Exception e) {
            System.err.println("Error sending like notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean removeLike(int userId, int experienceId) {
        String query = "DELETE FROM experience_like WHERE user_id = ? AND experience_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.setInt(2, experienceId);
            
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error removing like: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean hasUserLiked(int userId, int experienceId) {
        String query = "SELECT COUNT(*) FROM experience_like WHERE user_id = ? AND experience_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.setInt(2, experienceId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error checking like: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public int getLikeCount(int experienceId) {
        String query = "SELECT COUNT(*) FROM experience_like WHERE experience_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, experienceId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error counting likes: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    public ObservableList<ExperienceLike> getLikesByExperienceId(int experienceId) {
        ObservableList<ExperienceLike> likes = FXCollections.observableArrayList();
        String query = "SELECT * FROM experience_like WHERE experience_id = ? ORDER BY date_liked DESC";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, experienceId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                ExperienceLike like = new ExperienceLike();
                like.setId(rs.getInt("id"));
                like.setUserId(rs.getInt("user_id"));
                like.setExperienceId(rs.getInt("experience_id"));
                
                Timestamp timestamp = rs.getTimestamp("date_liked");
                if (timestamp != null) {
                    like.setDateLiked(timestamp.toLocalDateTime());
                }
                
                likes.add(like);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving likes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return likes;
    }
    
    public ObservableList<ExperienceLike> getLikesByUserId(int userId) {
        ObservableList<ExperienceLike> likes = FXCollections.observableArrayList();
        String query = "SELECT * FROM experience_like WHERE user_id = ? ORDER BY date_liked DESC";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                ExperienceLike like = new ExperienceLike();
                like.setId(rs.getInt("id"));
                like.setUserId(rs.getInt("user_id"));
                like.setExperienceId(rs.getInt("experience_id"));
                
                Timestamp timestamp = rs.getTimestamp("date_liked");
                if (timestamp != null) {
                    like.setDateLiked(timestamp.toLocalDateTime());
                }
                
                likes.add(like);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user likes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return likes;
    }
} 