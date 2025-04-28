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
        String sql = "INSERT INTO preferences (climat_pref, country, domain, preferred_language, teaching_mode, university_type, cultural_activities, language_level) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, preferences.getClimat_pref());
            ps.setString(2, preferences.getCountry());
            ps.setString(3, preferences.getDomain());
            ps.setString(4, preferences.getPreferred_language());
            ps.setString(5, preferences.getTeaching_mode());
            ps.setString(6, preferences.getUniversity_type());
            ps.setString(7, preferences.getCultural_activities());
            ps.setString(8, preferences.getLanguage_level());
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
                "teaching_mode = ?, university_type = ?, cultural_activities = ?, language_level = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, preferences.getClimat_pref());
            ps.setString(2, preferences.getCountry());
            ps.setString(3, preferences.getDomain());
            ps.setString(4, preferences.getPreferred_language());
            ps.setString(5, preferences.getTeaching_mode());
            ps.setString(6, preferences.getUniversity_type());
            ps.setString(7, preferences.getCultural_activities());
            ps.setString(8, preferences.getLanguage_level());
            ps.setInt(9, preferences.getId());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Préférence modifiée avec succès.");
            } else {
                System.out.println("Aucune modification effectuée.");
            }
        }
    }


    @Override
    public  List<Preferences> recuperer() throws SQLException {

        String sql = "SELECT * FROM preferences";
        List<Preferences> preferencesList = new ArrayList<>();

        try (Statement ste = cnx.createStatement();
             ResultSet rs = ste.executeQuery(sql)) {

            while (rs.next()) {
                Preferences pref = new Preferences(
                        rs.getInt("id"),
                        rs.getString("climat_pref"),
                        rs.getString("country"),
                        rs.getString("domain"),
                        rs.getString("preferred_language"),
                        rs.getString("teaching_mode"),
                        rs.getString("university_type"),
                        rs.getString("cultural_activities"),
                        rs.getString("language_level")
                );
                preferencesList.add(pref);
            }
        }
        return preferencesList;
    }
}
