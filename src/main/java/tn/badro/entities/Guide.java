package tn.badro.entities;

public class Guide {
    private Integer id;
    private Integer universityId;
    private String title;
    private String country;
    private String description;

    public Guide() {}

    public Guide(Integer id, Integer universityId, String title, String country, String description) {
        this.id = id;
        this.universityId = universityId;
        this.title = title;
        this.country = country;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUniversityId() {
        return universityId;
    }

    public void setUniversityId(Integer universityId) {
        this.universityId = universityId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
} 