package tn.badro.services;

import tn.badro.entities.Guide;
import tn.badro.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuideService {
    private final MyDataBase dbConnection;

    public GuideService() {
        this.dbConnection = MyDataBase.getInstance();
    }

    public List<Guide> getAllGuides() {
        List<Guide> guides = new ArrayList<>();
        String sql = "SELECT * FROM guide";

        try (Connection conn = dbConnection.getCnx();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Guide guide = new Guide();
                guide.setId(rs.getInt("id"));
                guide.setUniversityId(rs.getInt("university_id"));
                guide.setTitle(rs.getString("title"));
                guide.setCountry(rs.getString("country"));
                guide.setDescription(rs.getString("description"));
                guides.add(guide);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching guides: " + e.getMessage(), e);
        }
        return guides;
    }

    public Optional<Guide> getGuideById(Integer id) {
        String sql = "SELECT * FROM guide WHERE id = ?";

        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Guide guide = new Guide();
                    guide.setId(rs.getInt("id"));
                    guide.setUniversityId(rs.getInt("university_id"));
                    guide.setTitle(rs.getString("title"));
                    guide.setCountry(rs.getString("country"));
                    guide.setDescription(rs.getString("description"));
                    return Optional.of(guide);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching guide: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void createGuide(Guide guide) {
        String sql = "INSERT INTO guide (university_id, title, country, description) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, guide.getUniversityId());
            pstmt.setString(2, guide.getTitle());
            pstmt.setString(3, guide.getCountry());
            pstmt.setString(4, guide.getDescription());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating guide: " + e.getMessage(), e);
        }
    }

    public void updateGuide(Integer id, Guide guide) {
        String sql = "UPDATE guide SET university_id = ?, title = ?, country = ?, description = ? WHERE id = ?";

        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, guide.getUniversityId());
            pstmt.setString(2, guide.getTitle());
            pstmt.setString(3, guide.getCountry());
            pstmt.setString(4, guide.getDescription());
            pstmt.setInt(5, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating guide: " + e.getMessage(), e);
        }
    }

    public void deleteGuide(Integer id) {
        String sql = "DELETE FROM guide WHERE id = ?";

        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting guide: " + e.getMessage(), e);
        }
    }
} 