package com.example.eventmanagement.service;

import com.example.eventmanagement.entity.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EventService {
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM evenement";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                events.add(mapToEvent(rs));
            }
            System.out.println("Fetched " + events.size() + " events");
        } catch (SQLException e) {
            System.err.println("Error fetching events: " + e.getMessage());
            e.printStackTrace();
        }
        return events;
    }

    public Optional<Event> getEventById(Integer id) {
        String sql = "SELECT * FROM evenement WHERE event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToEvent(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching event ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void createEvent(Event event) {
        String sql = "INSERT INTO evenement (titre, description, date, heure, lieu, type, organisateur_nom, nombre_max_participants, status, ticket_prix, periode_inscription_fin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, event.getTitre());
            stmt.setString(2, event.getDescription());
            stmt.setObject(3, event.getDate());
            stmt.setObject(4, event.getHeure());
            stmt.setString(5, event.getLieu());
            stmt.setString(6, event.getType());
            stmt.setString(7, event.getOrganisateurNom());
            stmt.setObject(8, event.getNombreMaxParticipants());
            stmt.setString(9, event.getStatus());
            stmt.setString(10, event.getTicketPrix());
            stmt.setString(11, event.getPeriodeInscriptionFin());
            stmt.executeUpdate();
            System.out.println("Created event: " + event.getTitre());
        } catch (SQLException e) {
            System.err.println("Error creating event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateEvent(Integer id, Event event) {
        String sql = "UPDATE evenement SET titre = ?, description = ?, date = ?, heure = ?, lieu = ?, type = ?, organisateur_nom = ?, nombre_max_participants = ?, status = ?, ticket_prix = ?, periode_inscription_fin = ? WHERE event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, event.getTitre());
            stmt.setString(2, event.getDescription());
            stmt.setObject(3, event.getDate());
            stmt.setObject(4, event.getHeure());
            stmt.setString(5, event.getLieu());
            stmt.setString(6, event.getType());
            stmt.setString(7, event.getOrganisateurNom());
            stmt.setObject(8, event.getNombreMaxParticipants());
            stmt.setString(9, event.getStatus());
            stmt.setString(10, event.getTicketPrix());
            stmt.setString(11, event.getPeriodeInscriptionFin());
            stmt.setInt(12, id);
            stmt.executeUpdate();
            System.out.println("Updated event ID " + id);
        } catch (SQLException e) {
            System.err.println("Error updating event ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteEvent(Integer id) {
        // First, delete associated participations
        String deleteParticipationsSql = "DELETE FROM participation WHERE id_event = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteParticipationsSql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Deleted participations for event ID " + id);
        } catch (SQLException e) {
            System.err.println("Error deleting participations for event ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }

        // Then, delete the event
        String deleteEventSql = "DELETE FROM evenement WHERE event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteEventSql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Deleted event ID " + id);
        } catch (SQLException e) {
            System.err.println("Error deleting event ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<String, Long> getEventTypeDistribution() {
        List<Event> events = getAllEvents();
        return events.stream()
                .collect(Collectors.groupingBy(
                        event -> event.getType() != null ? event.getType() : "None",
                        Collectors.counting()
                ));
    }

    private Event mapToEvent(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setEventId(rs.getInt("event_id"));
        event.setTitre(rs.getString("titre"));
        event.setDescription(rs.getString("description"));
        event.setDate(rs.getObject("date", LocalDate.class));
        event.setHeure(rs.getObject("heure", LocalTime.class));
        event.setLieu(rs.getString("lieu"));
        event.setType(rs.getString("type"));
        event.setOrganisateurNom(rs.getString("organisateur_nom"));
        event.setNombreMaxParticipants(rs.getInt("nombre_max_participants"));
        if (rs.wasNull()) {
            event.setNombreMaxParticipants(null);
        }
        event.setStatus(rs.getString("status"));
        event.setTicketPrix(rs.getString("ticket_prix"));
        event.setPeriodeInscriptionFin(rs.getString("periode_inscription_fin"));
        return event;
    }
}