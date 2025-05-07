package tn.badro.services;

import tn.badro.entities.Preferences;
import tn.badro.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PreferencesService implements IServices<Preferences> {
    private final Connection cnx;

    public PreferencesService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Preferences preferences) throws SQLException {
        String sql = "INSERT INTO preferences (climat_pref, country, domain, preferred_language, teaching_mode, university_type, cultural_activities, language_level, id_user) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, preferences.getClimat_pref());
            ps.setString(2, preferences.getCountry());
            ps.setString(3, preferences.getDomain());
            ps.setString(4, preferences.getPreferred_language());
            ps.setString(5, preferences.getTeaching_mode());
            ps.setString(6, preferences.getUniversity_type());
            ps.setString(7, preferences.getCultural_activities());
            ps.setString(8, preferences.getLanguage_level());
            ps.setInt(9, preferences.getId_user());
            ps.executeUpdate();
            System.out.println("Préférence ajoutée avec succès.");
        }
    }

    @Override
    public void supprimer(Preferences preferences) {
        String sql = "DELETE FROM preferences WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, preferences.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Préférence supprimée.");
            } else {
                System.out.println("Aucune préférence trouvée à supprimer.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @Override
    public void modifier(Preferences preferences) throws SQLException {
        String sql = "UPDATE preferences SET climat_pref = ?, country = ?, domain = ?, preferred_language = ?, " +
                "teaching_mode = ?, university_type = ?, cultural_activities = ?, language_level = ?, id_user = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, preferences.getClimat_pref());
            ps.setString(2, preferences.getCountry());
            ps.setString(3, preferences.getDomain());
            ps.setString(4, preferences.getPreferred_language());
            ps.setString(5, preferences.getTeaching_mode());
            ps.setString(6, preferences.getUniversity_type());
            ps.setString(7, preferences.getCultural_activities());
            ps.setString(8, preferences.getLanguage_level());
            ps.setInt(9, preferences.getId_user());
            ps.setInt(10, preferences.getId());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Préférence modifiée avec succès.");
            } else {
                System.out.println("Aucune modification effectuée.");
            }
        }
    }

    @Override
    public List<Preferences> recuperer() throws SQLException {
        List<Preferences> preferences = new ArrayList<>();
        String sql = "SELECT * FROM preferences";
        
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Preferences pref = new Preferences();
                pref.setId(rs.getInt("id"));
                pref.setClimat_pref(rs.getString("climat_pref"));
                pref.setCountry(rs.getString("country"));
                pref.setDomain(rs.getString("domain"));
                pref.setPreferred_language(rs.getString("preferred_language"));
                pref.setTeaching_mode(rs.getString("teaching_mode"));
                pref.setUniversity_type(rs.getString("university_type"));
                pref.setCultural_activities(rs.getString("cultural_activities"));
                pref.setLanguage_level(rs.getString("language_level"));
                pref.setId_user(rs.getInt("id_user"));
                
                preferences.add(pref);
            }
        }
        
        return preferences;
    }

    /**
     * Get all preferences for a specific user
     * @param userId The ID of the user
     * @return List of preferences for the user
     */
    public List<Preferences> getPreferencesByUserId(int userId) throws SQLException {
        List<Preferences> userPreferences = new ArrayList<>();
        String sql = "SELECT * FROM preferences WHERE id_user = ?";
        
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Preferences pref = new Preferences();
                pref.setId(rs.getInt("id"));
                pref.setClimat_pref(rs.getString("climat_pref"));
                pref.setCountry(rs.getString("country"));
                pref.setDomain(rs.getString("domain"));
                pref.setPreferred_language(rs.getString("preferred_language"));
                pref.setTeaching_mode(rs.getString("teaching_mode"));
                pref.setUniversity_type(rs.getString("university_type"));
                pref.setCultural_activities(rs.getString("cultural_activities"));
                pref.setLanguage_level(rs.getString("language_level"));
                pref.setId_user(rs.getInt("id_user"));
                
                userPreferences.add(pref);
            }
        }
        
        return userPreferences;
    }
    
    /**
     * Get preferences that match with a user's preference but belong to other users
     * @param userPref The user's preference
     * @return List of matching preferences from other users
     */
    public List<Preferences> findMatchingPreferences(Preferences userPref) throws SQLException {
        List<Preferences> matchingPreferences = new ArrayList<>();
        
        // SQL query to find preferences with same criteria but different user
        String sql = "SELECT * FROM preferences WHERE " +
                "climat_pref = ? AND country = ? AND domain = ? AND " +
                "preferred_language = ? AND teaching_mode = ? AND university_type = ? AND " +
                "cultural_activities = ? AND language_level = ? AND " +
                "id_user <> ?"; // Different user
        
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, userPref.getClimat_pref());
            ps.setString(2, userPref.getCountry());
            ps.setString(3, userPref.getDomain());
            ps.setString(4, userPref.getPreferred_language());
            ps.setString(5, userPref.getTeaching_mode());
            ps.setString(6, userPref.getUniversity_type());
            ps.setString(7, userPref.getCultural_activities());
            ps.setString(8, userPref.getLanguage_level());
            ps.setInt(9, userPref.getId_user());
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Preferences pref = new Preferences();
                pref.setId(rs.getInt("id"));
                pref.setClimat_pref(rs.getString("climat_pref"));
                pref.setCountry(rs.getString("country"));
                pref.setDomain(rs.getString("domain"));
                pref.setPreferred_language(rs.getString("preferred_language"));
                pref.setTeaching_mode(rs.getString("teaching_mode"));
                pref.setUniversity_type(rs.getString("university_type"));
                pref.setCultural_activities(rs.getString("cultural_activities"));
                pref.setLanguage_level(rs.getString("language_level"));
                pref.setId_user(rs.getInt("id_user"));
                
                matchingPreferences.add(pref);
            }
        }
        
        return matchingPreferences;
    }
}
