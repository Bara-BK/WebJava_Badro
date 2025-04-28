package com.example.eventmanagement.entity;

import lombok.Data;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

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

    public void setTelephoneNumber(Integer telephoneNumber) {
        if (telephoneNumber != null) {
            String phoneStr = telephoneNumber.toString();
            if (phoneStr.length() < 7) {
                throw new IllegalArgumentException("Telephone number must be at least 7 digits");
            }
            if (phoneStr.length() > 11) {
                throw new IllegalArgumentException("Telephone number must be 11 digits or less");
            }
        }
        this.telephoneNumber = telephoneNumber; // Optional
    }

    public void setTicketCode(String ticketCode) {
        if (ticketCode != null && !ticketCode.trim().isEmpty()) {
            if (ticketCode.length() != 8) {
                throw new IllegalArgumentException("Ticket code must be exactly 8 characters");
            }
            if (!ticketCode.matches("[A-Za-z0-9]+")) {
                throw new IllegalArgumentException("Ticket code must be alphanumeric");
            }
        }
        this.ticketCode = ticketCode; // Optional
    }

    public void setPaimentMethod(String paimentMethod) {
        if (paimentMethod != null && !paimentMethod.trim().isEmpty()) {
            List<String> validMethods = Arrays.asList("Credit", "Debit", "Cash", "Online");
            if (!validMethods.contains(paimentMethod)) {
                throw new IllegalArgumentException("Payment method must be one of: Credit, Debit, Cash, Online");
            }
        }
        this.paimentMethod = paimentMethod; // Optional
    }
}