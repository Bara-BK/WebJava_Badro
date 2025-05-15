package tn.badro.services;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.badro.entities.Experience;
import tn.badro.tools.MyDataBase;

public class ExperienceService {
    private Connection connection;
    
    public ExperienceService() {
        connection = MyDataBase.getInstance().getCnx();
    }
    
    public boolean add(Experience experience) {
        String query = "INSERT INTO experience (title, description, date_posted, location, user_id, image_path, destination) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, experience.getTitle());
            pst.setString(2, experience.getDescription());
            
            LocalDateTime datePosted = experience.getDatePosted();
            if (datePosted == null) {
                datePosted = LocalDateTime.now();
            }
            pst.setTimestamp(3, Timestamp.valueOf(datePosted));
            
            pst.setString(4, experience.getLocation());
            pst.setInt(5, experience.getUserId());
            pst.setString(6, experience.getImagePath());
            pst.setString(7, experience.getDestination());
            
            int rowsAffected = pst.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    experience.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding experience: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean update(Experience experience) {
        String query = "UPDATE experience SET title = ?, description = ?, location = ?, image_path = ?, destination = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, experience.getTitle());
            pst.setString(2, experience.getDescription());
            pst.setString(3, experience.getLocation());
            pst.setString(4, experience.getImagePath());
            pst.setString(5, experience.getDestination());
            pst.setInt(6, experience.getId());
            
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating experience: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean delete(int id) {
        String query = "DELETE FROM experience WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting experience: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public Experience getById(int id) {
        String query = "SELECT * FROM experience WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                Experience experience = new Experience();
                experience.setId(rs.getInt("id"));
                experience.setTitle(rs.getString("title"));
                experience.setDescription(rs.getString("description"));
                
                Timestamp timestamp = rs.getTimestamp("date_posted");
                if (timestamp != null) {
                    experience.setDatePosted(timestamp.toLocalDateTime());
                }
                
                experience.setLocation(rs.getString("location"));
                experience.setUserId(rs.getInt("user_id"));
                experience.setImagePath(rs.getString("image_path"));
                experience.setDestination(rs.getString("destination"));
                
                return experience;
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error retrieving experience: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public ObservableList<Experience> getAll() {
        ObservableList<Experience> experiences = FXCollections.observableArrayList();
        String query = "SELECT * FROM experience ORDER BY date_posted DESC";
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            
            while (rs.next()) {
                Experience experience = new Experience();
                experience.setId(rs.getInt("id"));
                experience.setTitle(rs.getString("title"));
                experience.setDescription(rs.getString("description"));
                
                Timestamp timestamp = rs.getTimestamp("date_posted");
                if (timestamp != null) {
                    experience.setDatePosted(timestamp.toLocalDateTime());
                }
                
                experience.setLocation(rs.getString("location"));
                experience.setUserId(rs.getInt("user_id"));
                experience.setImagePath(rs.getString("image_path"));
                experience.setDestination(rs.getString("destination"));
                
                experiences.add(experience);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving experiences: " + e.getMessage());
            e.printStackTrace();
        }
        
        return experiences;
    }
    
    public ObservableList<Experience> getByUserId(int userId) {
        ObservableList<Experience> experiences = FXCollections.observableArrayList();
        String query = "SELECT * FROM experience WHERE user_id = ? ORDER BY date_posted DESC";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                Experience experience = new Experience();
                experience.setId(rs.getInt("id"));
                experience.setTitle(rs.getString("title"));
                experience.setDescription(rs.getString("description"));
                
                Timestamp timestamp = rs.getTimestamp("date_posted");
                if (timestamp != null) {
                    experience.setDatePosted(timestamp.toLocalDateTime());
                }
                
                experience.setLocation(rs.getString("location"));
                experience.setUserId(rs.getInt("user_id"));
                experience.setImagePath(rs.getString("image_path"));
                experience.setDestination(rs.getString("destination"));
                
                experiences.add(experience);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user experiences: " + e.getMessage());
            e.printStackTrace();
        }
        
        return experiences;
    }
    
    public ObservableList<Experience> searchByKeyword(String keyword) {
        ObservableList<Experience> experiences = FXCollections.observableArrayList();
        String query = "SELECT * FROM experience WHERE title LIKE ? OR description LIKE ? ORDER BY date_posted DESC";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            String searchTerm = "%" + keyword + "%";
            pst.setString(1, searchTerm);
            pst.setString(2, searchTerm);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                Experience experience = new Experience();
                experience.setId(rs.getInt("id"));
                experience.setTitle(rs.getString("title"));
                experience.setDescription(rs.getString("description"));
                
                Timestamp timestamp = rs.getTimestamp("date_posted");
                if (timestamp != null) {
                    experience.setDatePosted(timestamp.toLocalDateTime());
                }
                
                experience.setLocation(rs.getString("location"));
                experience.setUserId(rs.getInt("user_id"));
                experience.setImagePath(rs.getString("image_path"));
                experience.setDestination(rs.getString("destination"));
                
                experiences.add(experience);
            }
        } catch (SQLException e) {
            System.err.println("Error searching experiences: " + e.getMessage());
            e.printStackTrace();
        }
        
        return experiences;
    }
    
    public ObservableList<Experience> searchByDestination(String destination) {
        ObservableList<Experience> experiences = FXCollections.observableArrayList();
        String query = "SELECT * FROM experience WHERE destination LIKE ? ORDER BY date_posted DESC";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            String searchTerm = "%" + destination + "%";
            pst.setString(1, searchTerm);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                Experience experience = new Experience();
                experience.setId(rs.getInt("id"));
                experience.setTitle(rs.getString("title"));
                experience.setDescription(rs.getString("description"));
                
                Timestamp timestamp = rs.getTimestamp("date_posted");
                if (timestamp != null) {
                    experience.setDatePosted(timestamp.toLocalDateTime());
                }
                
                experience.setLocation(rs.getString("location"));
                experience.setUserId(rs.getInt("user_id"));
                experience.setImagePath(rs.getString("image_path"));
                experience.setDestination(rs.getString("destination"));
                
                experiences.add(experience);
            }
        } catch (SQLException e) {
            System.err.println("Error searching experiences by destination: " + e.getMessage());
            e.printStackTrace();
        }
        
        return experiences;
    }
    
    public ObservableList<Experience> advancedSearch(String keyword, String destination) {
        ObservableList<Experience> experiences = FXCollections.observableArrayList();
        
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM experience WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryBuilder.append(" AND (title LIKE ? OR description LIKE ?)");
            String keywordTerm = "%" + keyword + "%";
            params.add(keywordTerm);
            params.add(keywordTerm);
        }
        
        if (destination != null && !destination.trim().isEmpty()) {
            queryBuilder.append(" AND destination LIKE ?");
            params.add("%" + destination + "%");
        }
        
        queryBuilder.append(" ORDER BY date_posted DESC");
        
        try (PreparedStatement pst = connection.prepareStatement(queryBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pst.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                Experience experience = new Experience();
                experience.setId(rs.getInt("id"));
                experience.setTitle(rs.getString("title"));
                experience.setDescription(rs.getString("description"));
                
                Timestamp timestamp = rs.getTimestamp("date_posted");
                if (timestamp != null) {
                    experience.setDatePosted(timestamp.toLocalDateTime());
                }
                
                experience.setLocation(rs.getString("location"));
                experience.setUserId(rs.getInt("user_id"));
                experience.setImagePath(rs.getString("image_path"));
                experience.setDestination(rs.getString("destination"));
                
                experiences.add(experience);
            }
        } catch (SQLException e) {
            System.err.println("Error performing advanced search: " + e.getMessage());
            e.printStackTrace();
        }
        
        return experiences;
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
    
    public int getCommentCount(int experienceId) {
        String query = "SELECT COUNT(*) FROM experience_comment WHERE experience_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, experienceId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error counting comments: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    public List<String> getAllDestinations() {
        List<String> destinations = new ArrayList<>();
        String query = "SELECT DISTINCT destination FROM experience WHERE destination IS NOT NULL AND destination != '' ORDER BY destination";
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            
            while (rs.next()) {
                destinations.add(rs.getString("destination"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving destinations: " + e.getMessage());
            e.printStackTrace();
        }
        
        return destinations;
    }
} 