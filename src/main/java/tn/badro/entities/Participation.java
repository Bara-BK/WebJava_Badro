package tn.badro.entities;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Participation {
    private Integer participantId;
    private Integer idEvent;
    private String nomParticipant;
    private LocalDate dateInscription;
    private String evenementNom;
    private Integer telephoneNumber;
    private String ticketCode;
    private String paimentMethod;

    public void setIdEvent(Integer idEvent) {
        if (idEvent == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }
        this.idEvent = idEvent;
    }

    public void setNomParticipant(String nomParticipant) {
        if (nomParticipant == null || nomParticipant.trim().isEmpty()) {
            throw new IllegalArgumentException("Participant name cannot be blank");
        }
        if (nomParticipant.length() < 2 || nomParticipant.length() > 255) {
            throw new IllegalArgumentException("Participant name must be between 2 and 255 characters");
        }
        this.nomParticipant = nomParticipant;
    }

    public void setDateInscription(LocalDate dateInscription) {
        if (dateInscription != null && dateInscription.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Registration date cannot be in the future");
        }
        this.dateInscription = dateInscription; // Optional
    }

    public void setEvenementNom(String evenementNom) {
        if (evenementNom != null && evenementNom.length() > 255) {
            throw new IllegalArgumentException("Event name must be 255 characters or less");
        }
        this.evenementNom = evenementNom; // Optional
    }
}