package tn.badro.services;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.badro.entities.ExperienceComment;
import tn.badro.entities.User;
import tn.badro.entities.Notification;
import tn.badro.entities.NotificationManager;
import tn.badro.tools.MyDataBase;

public class CommentService {
    private Connection connection;
    private UserService userService;
    private ExperienceService experienceService;
    private NotificationManager notificationManager;
    
    public CommentService() {
        connection = MyDataBase.getInstance().getCnx();
        userService = new UserService();
        experienceService = new ExperienceService();
        notificationManager = NotificationManager.getInstance();
    }
    
    public boolean add(ExperienceComment comment) {
        String query = "INSERT INTO experience_comment (content, date_posted, user_id, experience_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, comment.getContent());
            
            LocalDateTime datePosted = comment.getDatePosted();
            if (datePosted == null) {
                datePosted = LocalDateTime.now();
            }
            pst.setTimestamp(2, Timestamp.valueOf(datePosted));
            
            pst.setInt(3, comment.getUserId());
            pst.setInt(4, comment.getExperienceId());
            
            int rowsAffected = pst.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    comment.setId(rs.getInt(1));
                }
                
                // Send notification to the experience owner
                sendCommentNotification(comment);
                
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding comment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Sends a notification to the experience owner when someone comments on their experience
     */
    private void sendCommentNotification(ExperienceComment comment) {
        try {
            // Get experience details
            int experienceId = comment.getExperienceId();
            var experience = experienceService.getById(experienceId);
            
            if (experience == null) {
                return;
            }
            
            // Only send notification if comment author is not the experience owner
            int experienceOwnerId = experience.getUserId();
            int commentAuthorId = comment.getUserId();
            
            if (experienceOwnerId == commentAuthorId) {
                return; // Don't notify yourself
            }
            
            // Get commenter's name
            Optional<User> commenterOpt = userService.getUserById(commentAuthorId);
            String commenterName = commenterOpt.map(u -> u.getNom() + " " + u.getPrenom()).orElse("Someone");
            
            // Create notification message
            String notificationMessage = commenterName + " commented on your experience: \"" + experience.getTitle() + "\"";
            
            // Add notification directly to the experience owner's notifications
            notificationManager.addNotificationForUser(experienceOwnerId, new Notification(notificationMessage));
            
            System.out.println("Comment notification sent to user ID " + experienceOwnerId + ": " + notificationMessage);
        } catch (Exception e) {
            System.err.println("Error sending comment notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean update(ExperienceComment comment) {
        String query = "UPDATE experience_comment SET content = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, comment.getContent());
            pst.setInt(2, comment.getId());
            
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating comment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean delete(int id) {
        String query = "DELETE FROM experience_comment WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting comment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public ExperienceComment getById(int id) {
        String query = "SELECT * FROM experience_comment WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                ExperienceComment comment = new ExperienceComment();
                comment.setId(rs.getInt("id"));
                comment.setContent(rs.getString("content"));
                
                Timestamp timestamp = rs.getTimestamp("date_posted");
                if (timestamp != null) {
                    comment.setDatePosted(timestamp.toLocalDateTime());
                }
                
                comment.setUserId(rs.getInt("user_id"));
                comment.setExperienceId(rs.getInt("experience_id"));
                
                // Set user name
                Optional<User> userOpt = userService.getUserById(comment.getUserId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    comment.setUserName(user.getNom() + " " + user.getPrenom());
                }
                
                return comment;
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error retrieving comment: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public ObservableList<ExperienceComment> getByExperienceId(int experienceId) {
        ObservableList<ExperienceComment> comments = FXCollections.observableArrayList();
        String query = "SELECT c.*, u.nom, u.prenom FROM experience_comment c " +
                      "JOIN user u ON c.user_id = u.id " +
                      "WHERE c.experience_id = ? ORDER BY c.date_posted DESC";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, experienceId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                ExperienceComment comment = new ExperienceComment();
                comment.setId(rs.getInt("id"));
                comment.setContent(rs.getString("content"));
                
                Timestamp timestamp = rs.getTimestamp("date_posted");
                if (timestamp != null) {
                    comment.setDatePosted(timestamp.toLocalDateTime());
                }
                
                comment.setUserId(rs.getInt("user_id"));
                comment.setExperienceId(rs.getInt("experience_id"));
                comment.setUserName(rs.getString("nom") + " " + rs.getString("prenom"));
                
                comments.add(comment);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving comments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return comments;
    }
    
    public ObservableList<ExperienceComment> getByUserId(int userId) {
        ObservableList<ExperienceComment> comments = FXCollections.observableArrayList();
        String query = "SELECT * FROM experience_comment WHERE user_id = ? ORDER BY date_posted DESC";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                ExperienceComment comment = new ExperienceComment();
                comment.setId(rs.getInt("id"));
                comment.setContent(rs.getString("content"));
                
                Timestamp timestamp = rs.getTimestamp("date_posted");
                if (timestamp != null) {
                    comment.setDatePosted(timestamp.toLocalDateTime());
                }
                
                comment.setUserId(rs.getInt("user_id"));
                comment.setExperienceId(rs.getInt("experience_id"));
                
                // Set user name
                Optional<User> userOpt = userService.getUserById(comment.getUserId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    comment.setUserName(user.getNom() + " " + user.getPrenom());
                }
                
                comments.add(comment);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user comments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return comments;
    }
} 