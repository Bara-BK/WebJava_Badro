package com.example.eventmanagement.entity;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Participation {
    private Integer id;
    private Integer idEvent;
    private String nomParticipant;
    private LocalDate dateInscription;
    private String evenementNom;
    private Integer telephoneNumber;
    private String ticketCode;
    private String paimentMethod;

    public void setNomParticipant(String nomParticipant) {
        if (nomParticipant == null || nomParticipant.trim().isEmpty()) {
            throw new IllegalArgumentException("Participant name cannot be blank");
        }
        if (nomParticipant.length() < 2 || nomParticipant.length() > 255) {
            throw new IllegalArgumentException("Participant name must be between 2 and 255 characters");
        }
        this.nomParticipant = nomParticipant;
    }

    public void setIdEvent(Integer idEvent) {
        if (idEvent == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }
        this.idEvent = idEvent;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription; // Optional
    }

    public void setEvenementNom(String evenementNom) {
        if (evenementNom != null && evenementNom.length() > 255) {
            throw new IllegalArgumentException("Event name must be 255 characters or less");
        }
        this.evenementNom = evenementNom; // Optional
    }

    public void setTelephoneNumber(Integer telephoneNumber) {
        if (telephoneNumber != null && telephoneNumber.toString().length() > 11) {
            throw new IllegalArgumentException("Telephone number must be 11 digits or less");
        }
        this.telephoneNumber = telephoneNumber; // Optional
    }

    public void setTicketCode(String ticketCode) {
        if (ticketCode != null && ticketCode.length() > 255) {
            throw new IllegalArgumentException("Ticket code must be 255 characters or less");
        }
        this.ticketCode = ticketCode; // Optional
    }

    public void setPaimentMethod(String paimentMethod) {
        if (paimentMethod != null && paimentMethod.length() > 255) {
            throw new IllegalArgumentException("Payment method must be 255 characters or less");
        }
        this.paimentMethod = paimentMethod; // Optional
    }
}