package tn.badro.entities;

public class University {
    private Integer id;
    private String name;
    private String location;
    private String description;
    private String image;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("University name cannot be blank");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("University name must be 255 characters or less");
        }
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be blank");
        }
        if (location.length() > 255) {
            throw new IllegalArgumentException("Location must be 255 characters or less");
        }
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        if (image != null && image.length() > 255) {
            throw new IllegalArgumentException("Image path must be 255 characters or less");
        }
        this.image = image;
    }
} 