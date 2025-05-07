package tn.badro.services;

import tn.badro.entities.ProgramApplication;
import tn.badro.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing program applications
 */
public class ProgramApplicationService {
    private final MyDataBase dbConnection;

    public ProgramApplicationService() {
        this.dbConnection = MyDataBase.getInstance();
    }

    /**
     * Create a new program application
     */
    public boolean createProgramApplication(ProgramApplication application) throws SQLException {
        String query = "INSERT INTO program_applications (application_id, user_id, programme_id, motivation_letter, start_date, end_date, application_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, application.getApplicationId());
            stmt.setInt(2, application.getUserId());
            stmt.setInt(3, application.getProgrammeId());
            stmt.setString(4, application.getMotivationLetter());
            stmt.setDate(5, java.sql.Date.valueOf(application.getStartDate()));
            stmt.setDate(6, java.sql.Date.valueOf(application.getEndDate()));
            stmt.setDate(7, java.sql.Date.valueOf(application.getApplicationDate()));
            stmt.setString(8, application.getStatus());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Get application by ID
     */
    public ProgramApplication getApplicationById(String applicationId) throws SQLException {
        String query = "SELECT * FROM program_applications WHERE application_id = ?";
        
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, applicationId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToApplication(rs);
                }
            }
        }
        
        return null;
    }

    /**
     * Get all applications for a user
     */
    public List<ProgramApplication> getApplicationsByUserId(int userId) throws SQLException {
        List<ProgramApplication> applications = new ArrayList<>();
        String query = "SELECT * FROM program_applications WHERE user_id = ?";
        
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapResultSetToApplication(rs));
                }
            }
        }
        
        return applications;
    }

    /**
     * Get all applications for a program
     */
    public List<ProgramApplication> getApplicationsByProgramId(int programId) throws SQLException {
        List<ProgramApplication> applications = new ArrayList<>();
        String query = "SELECT * FROM program_applications WHERE programme_id = ?";
        
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, programId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapResultSetToApplication(rs));
                }
            }
        }
        
        return applications;
    }

    /**
     * Update application status
     */
    public boolean updateApplicationStatus(String applicationId, String status) throws SQLException {
        String query = "UPDATE program_applications SET status = ? WHERE application_id = ?";
        
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setString(2, applicationId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Delete an application
     */
    public boolean deleteApplication(String applicationId) throws SQLException {
        String query = "DELETE FROM program_applications WHERE application_id = ?";
        
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, applicationId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Get all applications
     */
    public List<ProgramApplication> getAllApplications() throws SQLException {
        List<ProgramApplication> applications = new ArrayList<>();
        String query = "SELECT * FROM program_applications ORDER BY application_date DESC";
        
        try (Connection conn = dbConnection.getCnx();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                applications.add(mapResultSetToApplication(rs));
            }
        }
        
        return applications;
    }
    
    /**
     * Get applications by status
     */
    public List<ProgramApplication> getApplicationsByStatus(String status) throws SQLException {
        List<ProgramApplication> applications = new ArrayList<>();
        String query = "SELECT * FROM program_applications WHERE status = ? ORDER BY application_date DESC";
        
        try (Connection conn = dbConnection.getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapResultSetToApplication(rs));
                }
            }
        }
        
        return applications;
    }

    /**
     * Map ResultSet to Application object
     */
    private ProgramApplication mapResultSetToApplication(ResultSet rs) throws SQLException {
        ProgramApplication application = new ProgramApplication();
        
        application.setApplicationId(rs.getString("application_id"));
        application.setUserId(rs.getInt("user_id"));
        application.setProgrammeId(rs.getInt("programme_id"));
        application.setMotivationLetter(rs.getString("motivation_letter"));
        application.setStartDate(rs.getDate("start_date").toLocalDate());
        application.setEndDate(rs.getDate("end_date").toLocalDate());
        application.setApplicationDate(rs.getDate("application_date").toLocalDate());
        application.setStatus(rs.getString("status"));
        
        return application;
    }
} 