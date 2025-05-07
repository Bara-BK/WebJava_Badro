package tn.badro.services;

import tn.badro.entities.University;
import tn.badro.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UniversityService {
    private final MyDataBase dbConnection;

    public UniversityService() {
        this.dbConnection = MyDataBase.getInstance();
    }
    
    public List<University> getAllUniversities() {
        List<University> universities = new ArrayList<>();
        String sql = "SELECT * FROM university";
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                universities.add(mapToUniversity(rs));
            }
            System.out.println("Fetched " + universities.size() + " universities");
        } catch (SQLException e) {
            System.err.println("Error fetching universities: " + e.getMessage());
            e.printStackTrace();
        }
        return universities;
    }

    public Optional<University> getUniversityById(Integer id) {
        String sql = "SELECT * FROM university WHERE id = ?";
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToUniversity(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching university ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void createUniversity(University university) {
        String sql = "INSERT INTO university (name, location, description, image) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, university.getName());
            stmt.setString(2, university.getLocation());
            stmt.setString(3, university.getDescription());
            stmt.setString(4, university.getImage());
            stmt.executeUpdate();
            System.out.println("Created university: " + university.getName());
        } catch (SQLException e) {
            System.err.println("Error creating university: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create university", e);
        }
    }

    public void updateUniversity(Integer id, University university) {
        String sql = "UPDATE university SET name = ?, location = ?, description = ?, image = ? WHERE id = ?";
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, university.getName());
            stmt.setString(2, university.getLocation());
            stmt.setString(3, university.getDescription());
            stmt.setString(4, university.getImage());
            stmt.setInt(5, id);
            stmt.executeUpdate();
            System.out.println("Updated university ID " + id);
        } catch (SQLException e) {
            System.err.println("Error updating university ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update university", e);
        }
    }

    public void deleteUniversity(Integer id) {
        String sql = "DELETE FROM university WHERE id = ?";
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Deleted university ID " + id);
        } catch (SQLException e) {
            System.err.println("Error deleting university ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete university", e);
        }
    }

    private University mapToUniversity(ResultSet rs) throws SQLException {
        University university = new University();
        university.setId(rs.getInt("id"));
        university.setName(rs.getString("name"));
        university.setLocation(rs.getString("location"));
        university.setDescription(rs.getString("description"));
        university.setImage(rs.getString("image"));
        return university;
    }

    public int getUniversityCount() {
        String sql = "SELECT COUNT(*) FROM university";
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting universities: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int getCountryCount() {
        String sql = "SELECT COUNT(DISTINCT location) FROM university";
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting countries: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
} 