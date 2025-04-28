package com.example.eventmanagement.service;

import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.Participation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

public class ParticipationService {
    private final EventService eventService = new EventService();

    public List<Participation> getParticipationsByEventId(Integer eventId) {
        List<Participation> participations = new ArrayList<>();
        String sql = "SELECT * FROM participation WHERE id_event = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    participations.add(mapToParticipation(rs));
                }
            }
            System.out.println("Fetched " + participations.size() + " participations for event ID " + eventId);
        } catch (SQLException e) {
            System.err.println("Error fetching participations: " + e.getMessage());
            e.printStackTrace();
        }
        return participations;
    }

    public Optional<Participation> getParticipationById(Integer id) {
        String sql = "SELECT * FROM participation WHERE participant_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToParticipation(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching participation ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void createParticipation(Participation participation) {
        String sql = "INSERT INTO participation (id_event, nom_participant, date_inscription, evenement_nom, telephone_number, ticket_code, paiment_method) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, participation.getIdEvent());
            stmt.setString(2, participation.getNomParticipant());
            stmt.setObject(3, participation.getDateInscription());
            stmt.setString(4, participation.getEvenementNom());
            stmt.setObject(5, participation.getTelephoneNumber());
            stmt.setString(6, participation.getTicketCode());
            stmt.setString(7, participation.getPaimentMethod());
            stmt.executeUpdate();
            System.out.println("Created participation for " + participation.getNomParticipant());
        } catch (SQLException e) {
            System.err.println("Error creating participation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateParticipation(Integer id, Participation participation) {
        String sql = "UPDATE participation SET id_event = ?, nom_participant = ?, date_inscription = ?, evenement_nom = ?, telephone_number = ?, ticket_code = ?, paiment_method = ? WHERE participant_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, participation.getIdEvent());
            stmt.setString(2, participation.getNomParticipant());
            stmt.setObject(3, participation.getDateInscription());
            stmt.setString(4, participation.getEvenementNom());
            stmt.setObject(5, participation.getTelephoneNumber());
            stmt.setString(6, participation.getTicketCode());
            stmt.setString(7, participation.getPaimentMethod());
            stmt.setInt(8, id);
            stmt.executeUpdate();
            System.out.println("Updated participation ID " + id);
        } catch (SQLException e) {
            System.err.println("Error updating participation ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteParticipation(Integer id) {
        String sql = "DELETE FROM participation WHERE participant_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Deleted participation ID " + id);
        } catch (SQLException e) {
            System.err.println("Error deleting participation ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Participation mapToParticipation(ResultSet rs) throws SQLException {
        Participation participation = new Participation();
        participation.setParticipantId(rs.getInt("participant_id"));
        participation.setIdEvent(rs.getInt("id_event"));
        participation.setNomParticipant(rs.getString("nom_participant"));
        participation.setDateInscription(rs.getObject("date_inscription", LocalDate.class));
        participation.setEvenementNom(rs.getString("evenement_nom"));
        participation.setTelephoneNumber(rs.getInt("telephone_number"));
        participation.setTicketCode(rs.getString("ticket_code"));
        participation.setPaimentMethod(rs.getString("paiment_method"));
        return participation;
    }

    public Map<String, Long> getParticipantCountByEventType() {
        List<Event> events = eventService.getAllEvents();
        return events.stream()
                .collect(Collectors.groupingBy(
                        event -> event.getType() != null ? event.getType() : "None",
                        Collectors.summingLong(event -> 
                            getParticipationsByEventId(event.getEventId()).size()
                        )
                ));
    }
}