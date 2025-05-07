package tn.badro.entities;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Event {
    private Integer eventId;
    private String titre;
    private String description;
    private LocalDate date;
    private LocalTime heure;
    private String lieu;
    private String type;
    private String organisateurNom;
    private Integer nombreMaxParticipants;
    private String status;
    private String ticketPrix;
    private String periodeInscriptionFin;
    private List<Participation> participations = new ArrayList<>();

    public void setTitre(String titre) {
        if (titre == null || titre.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        if (titre.length() < 5 || titre.length() > 255) {
            throw new IllegalArgumentException("Title must be between 5 and 20 characters");
        }
        this.titre = titre;
    }

    public void setDescription(String description) {
        this.description = description; // Optional
    }

    public void setDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        this.date = date;
    }

    public void setHeure(LocalTime heure) {
        if (heure == null) {
            throw new IllegalArgumentException("Time cannot be null");
        }
        this.heure = heure;
    }

    public void setLieu(String lieu) {
        if (lieu != null && !lieu.trim().isEmpty()) {
            if (lieu.length() < 3 || lieu.length() > 255) {
                throw new IllegalArgumentException("Location must be between 3 and 50 characters if provided");
            }
        }
        this.lieu = lieu; // Optional
    }

    public void setType(String type) {
        this.type = type; // Optional
    }

    public void setOrganisateurNom(String organisateurNom) {
        this.organisateurNom = organisateurNom; // Optional
    }

    public void setNombreMaxParticipants(Integer nombreMaxParticipants) {
        this.nombreMaxParticipants = nombreMaxParticipants; // Optional
    }

    public void setStatus(String status) {
        this.status = status; // Optional
    }

    public void setTicketPrix(String ticketPrix) {
        this.ticketPrix = ticketPrix; // Optional
    }

    public void setPeriodeInscriptionFin(String periodeInscriptionFin) {
        this.periodeInscriptionFin = periodeInscriptionFin; // Optional
    }
}