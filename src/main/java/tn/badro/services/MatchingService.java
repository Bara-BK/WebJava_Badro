package tn.badro.services;

import tn.badro.entities.Preferences;
import tn.badro.tools.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchingService {

    public List<String> loadRecentNotifications() {
        List<String> notifications = new ArrayList<>();
        String sql = "SELECT status FROM matching ORDER BY date_matching DESC";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String status = rs.getString("status");
                notifications.add(status);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }
}
