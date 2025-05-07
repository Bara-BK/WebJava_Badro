package tn.badro.services;

import tn.badro.entities.Programme;
import tn.badro.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProgrammeService {
    private final MyDataBase dbConnection;

    public ProgrammeService() {
        this.dbConnection = MyDataBase.getInstance();
    }

    public List<Programme> getProgrammesByUniversityId(int universityId) {
        List<Programme> programmes = new ArrayList<>();
        String sql = "SELECT * FROM programme WHERE university_id = ?";
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, universityId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Programme programme = new Programme();
                    programme.setId(rs.getInt("id"));
                    programme.setName(rs.getString("name"));
                    programme.setType(rs.getString("type"));
                    programme.setDescription(rs.getString("description"));
                    programme.setUniversityId(rs.getInt("university_id"));
                    programmes.add(programme);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching programmes: " + e.getMessage(), e);
        }
        return programmes;
    }

    public List<Programme> getAllProgrammes() {
        List<Programme> programmes = new ArrayList<>();
        String sql = "SELECT * FROM programme";

        try (Connection conn = dbConnection.getCnx();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Programme programme = new Programme();
                programme.setId(rs.getInt("id"));
                programme.setName(rs.getString("name"));
                programme.setType(rs.getString("type"));
                programme.setDescription(rs.getString("description"));
                programme.setUniversityId(rs.getInt("university_id"));
                programmes.add(programme);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching programmes: " + e.getMessage(), e);
        }
        return programmes;
    }

    public Optional<Programme> getProgrammeById(Integer id) {
        String sql = "SELECT * FROM programme WHERE id = ?";

        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Programme programme = new Programme();
                    programme.setId(rs.getInt("id"));
                    programme.setName(rs.getString("name"));
                    programme.setType(rs.getString("type"));
                    programme.setDescription(rs.getString("description"));
                    programme.setUniversityId(rs.getInt("university_id"));
                    return Optional.of(programme);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching programme: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void createProgramme(Programme programme) {
        String sql = "INSERT INTO programme (name, type, description, university_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, programme.getName());
            pstmt.setString(2, programme.getType());
            pstmt.setString(3, programme.getDescription());
            pstmt.setInt(4, programme.getUniversityId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating programme: " + e.getMessage(), e);
        }
    }

    public void updateProgramme(Programme programme) {
        String sql = "UPDATE programme SET name = ?, type = ?, description = ?, university_id = ? WHERE id = ?";

        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, programme.getName());
            pstmt.setString(2, programme.getType());
            pstmt.setString(3, programme.getDescription());
            pstmt.setInt(4, programme.getUniversityId());
            pstmt.setInt(5, programme.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating programme: " + e.getMessage(), e);
        }
    }

    public void deleteProgramme(Integer id) {
        String sql = "DELETE FROM programme WHERE id = ?";

        try (Connection conn = dbConnection.getCnx();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting programme: " + e.getMessage(), e);
        }
    }

    public int getProgrammeCount() {
        String sql = "SELECT COUNT(*) FROM programme";
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting programmes: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
} 