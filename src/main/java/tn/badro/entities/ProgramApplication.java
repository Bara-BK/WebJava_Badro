package tn.badro.entities;

import java.time.LocalDate;

/**
 * Entity representing a student application for an exchange program
 */
public class ProgramApplication {
    private String applicationId;
    private int userId;
    private int programmeId;
    private String motivationLetter;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate applicationDate;
    private String status;  // Pending, Approved, Rejected
    
    // Constructors
    public ProgramApplication() {
    }
    
    public ProgramApplication(String applicationId, int userId, int programmeId, String motivationLetter, 
                             LocalDate startDate, LocalDate endDate, LocalDate applicationDate, String status) {
        this.applicationId = applicationId;
        this.userId = userId;
        this.programmeId = programmeId;
        this.motivationLetter = motivationLetter;
        this.startDate = startDate;
        this.endDate = endDate;
        this.applicationDate = applicationDate;
        this.status = status;
    }
    
    // Getters and Setters
    public String getApplicationId() {
        return applicationId;
    }
    
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getProgrammeId() {
        return programmeId;
    }
    
    public void setProgrammeId(int programmeId) {
        this.programmeId = programmeId;
    }
    
    public String getMotivationLetter() {
        return motivationLetter;
    }
    
    public void setMotivationLetter(String motivationLetter) {
        this.motivationLetter = motivationLetter;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public LocalDate getApplicationDate() {
        return applicationDate;
    }
    
    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "ProgramApplication{" +
                "applicationId='" + applicationId + '\'' +
                ", userId=" + userId +
                ", programmeId=" + programmeId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", applicationDate=" + applicationDate +
                ", status='" + status + '\'' +
                '}';
    }
} 