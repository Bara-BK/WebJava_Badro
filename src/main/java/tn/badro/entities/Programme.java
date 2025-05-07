package tn.badro.entities;

public class Programme {
    private Integer id;
    private String name;
    private String type;
    private String description;
    private Integer universityId;

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
            throw new IllegalArgumentException("Programme name cannot be blank");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Programme name must be 255 characters or less");
        }
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Programme type cannot be blank");
        }
        // Normalize the type to handle case-insensitive comparison
        String normalizedType = type.trim().toLowerCase();
        if (!normalizedType.equals("scholarship") && !normalizedType.equals("paid")) {
            throw new IllegalArgumentException("Programme type must be either 'Scholarship' or 'Paid'");
        }
        // Store the type with first letter capitalized
        this.type = normalizedType.substring(0, 1).toUpperCase() + normalizedType.substring(1);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getUniversityId() {
        return universityId;
    }

    public void setUniversityId(Integer universityId) {
        this.universityId = universityId;
    }
} 