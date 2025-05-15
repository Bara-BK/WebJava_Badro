package tn.badro.entities;

import java.time.LocalDateTime;
import javafx.beans.property.*;

public class ExperienceComment {
    private final IntegerProperty id;
    private final StringProperty content;
    private final ObjectProperty<LocalDateTime> datePosted;
    private final IntegerProperty userId;
    private final IntegerProperty experienceId;
    private StringProperty userName; // For display purposes
    
    public ExperienceComment() {
        this.id = new SimpleIntegerProperty();
        this.content = new SimpleStringProperty();
        this.datePosted = new SimpleObjectProperty<>();
        this.userId = new SimpleIntegerProperty();
        this.experienceId = new SimpleIntegerProperty();
        this.userName = new SimpleStringProperty();
    }
    
    public ExperienceComment(int id, String content, LocalDateTime datePosted, 
                           int userId, int experienceId) {
        this.id = new SimpleIntegerProperty(id);
        this.content = new SimpleStringProperty(content);
        this.datePosted = new SimpleObjectProperty<>(datePosted);
        this.userId = new SimpleIntegerProperty(userId);
        this.experienceId = new SimpleIntegerProperty(experienceId);
        this.userName = new SimpleStringProperty();
    }
    
    // ID
    public int getId() {
        return id.get();
    }
    
    public void setId(int value) {
        id.set(value);
    }
    
    public IntegerProperty idProperty() {
        return id;
    }
    
    // Content
    public String getContent() {
        return content.get();
    }
    
    public void setContent(String value) {
        content.set(value);
    }
    
    public StringProperty contentProperty() {
        return content;
    }
    
    // Date Posted
    public LocalDateTime getDatePosted() {
        return datePosted.get();
    }
    
    public void setDatePosted(LocalDateTime value) {
        datePosted.set(value);
    }
    
    public ObjectProperty<LocalDateTime> datePostedProperty() {
        return datePosted;
    }
    
    // User ID
    public int getUserId() {
        return userId.get();
    }
    
    public void setUserId(int value) {
        userId.set(value);
    }
    
    public IntegerProperty userIdProperty() {
        return userId;
    }
    
    // Experience ID
    public int getExperienceId() {
        return experienceId.get();
    }
    
    public void setExperienceId(int value) {
        experienceId.set(value);
    }
    
    public IntegerProperty experienceIdProperty() {
        return experienceId;
    }
    
    // User Name
    public String getUserName() {
        return userName.get();
    }
    
    public void setUserName(String value) {
        userName.set(value);
    }
    
    public StringProperty userNameProperty() {
        return userName;
    }
} 