package tn.badro.entities;

import java.time.LocalDateTime;
import javafx.beans.property.*;

public class Experience {
    private final IntegerProperty id;
    private final StringProperty title;
    private final StringProperty description;
    private final ObjectProperty<LocalDateTime> datePosted;
    private final StringProperty location;
    private final IntegerProperty userId;
    private final StringProperty imagePath;
    private final StringProperty destination;
    
    public Experience() {
        this.id = new SimpleIntegerProperty();
        this.title = new SimpleStringProperty();
        this.description = new SimpleStringProperty();
        this.datePosted = new SimpleObjectProperty<>();
        this.location = new SimpleStringProperty();
        this.userId = new SimpleIntegerProperty();
        this.imagePath = new SimpleStringProperty();
        this.destination = new SimpleStringProperty();
    }
    
    public Experience(int id, String title, String description, LocalDateTime datePosted, 
                     String location, int userId, String imagePath, String destination) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.datePosted = new SimpleObjectProperty<>(datePosted);
        this.location = new SimpleStringProperty(location);
        this.userId = new SimpleIntegerProperty(userId);
        this.imagePath = new SimpleStringProperty(imagePath);
        this.destination = new SimpleStringProperty(destination);
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
    
    // Title
    public String getTitle() {
        return title.get();
    }
    
    public void setTitle(String value) {
        title.set(value);
    }
    
    public StringProperty titleProperty() {
        return title;
    }
    
    // Description
    public String getDescription() {
        return description.get();
    }
    
    public void setDescription(String value) {
        description.set(value);
    }
    
    public StringProperty descriptionProperty() {
        return description;
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
    
    // Location
    public String getLocation() {
        return location.get();
    }
    
    public void setLocation(String value) {
        location.set(value);
    }
    
    public StringProperty locationProperty() {
        return location;
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
    
    // Image Path
    public String getImagePath() {
        return imagePath.get();
    }
    
    public void setImagePath(String value) {
        imagePath.set(value);
    }
    
    public StringProperty imagePathProperty() {
        return imagePath;
    }
    
    // Destination
    public String getDestination() {
        return destination.get();
    }
    
    public void setDestination(String value) {
        destination.set(value);
    }
    
    public StringProperty destinationProperty() {
        return destination;
    }
    
    @Override
    public String toString() {
        return title.get();
    }
} 