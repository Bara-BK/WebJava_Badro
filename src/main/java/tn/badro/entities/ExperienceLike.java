package tn.badro.entities;

import java.time.LocalDateTime;
import javafx.beans.property.*;

public class ExperienceLike {
    private final IntegerProperty id;
    private final IntegerProperty userId;
    private final IntegerProperty experienceId;
    private final ObjectProperty<LocalDateTime> dateLiked;
    
    public ExperienceLike() {
        this.id = new SimpleIntegerProperty();
        this.userId = new SimpleIntegerProperty();
        this.experienceId = new SimpleIntegerProperty();
        this.dateLiked = new SimpleObjectProperty<>();
    }
    
    public ExperienceLike(int id, int userId, int experienceId, LocalDateTime dateLiked) {
        this.id = new SimpleIntegerProperty(id);
        this.userId = new SimpleIntegerProperty(userId);
        this.experienceId = new SimpleIntegerProperty(experienceId);
        this.dateLiked = new SimpleObjectProperty<>(dateLiked);
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
    
    // Date Liked
    public LocalDateTime getDateLiked() {
        return dateLiked.get();
    }
    
    public void setDateLiked(LocalDateTime value) {
        dateLiked.set(value);
    }
    
    public ObjectProperty<LocalDateTime> dateLikedProperty() {
        return dateLiked;
    }
} 