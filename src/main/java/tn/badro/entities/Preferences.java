package tn.badro.entities;

public class Preferences {
    private int id;
    private String climat_pref;
    private String country;
    private String domain;
    private String preferred_language;
    private String teaching_mode;
    private String university_type;
    private String cultural_activities;
    private String language_level;
    private int id_user;

    // Constructeur complet avec tous les attributs
    public Preferences(int id, String climat_pref, String country, String domain, String preferred_language, String teaching_mode, String university_type, String cultural_activities, String language_level, int id_user) {
        this.id = id;
        this.climat_pref = climat_pref;
        this.country = country;
        this.domain = domain;
        this.preferred_language = preferred_language;
        this.teaching_mode = teaching_mode;
        this.university_type = university_type;
        this.cultural_activities = cultural_activities;
        this.language_level = language_level;
        this.id_user = id_user;
    }

    public Preferences() {
    }

    // Constructeur sans idPreference (pour une nouvelle Preference)
    public Preferences(String climat_pref, String country, String domain, String preferred_language,
                       String teaching_mode, String university_type, String cultural_activities, String language_level, int id_user) {
        this.climat_pref = climat_pref;
        this.country = country;
        this.domain = domain;
        this.preferred_language = preferred_language;
        this.teaching_mode = teaching_mode;
        this.university_type = university_type;
        this.cultural_activities = cultural_activities;
        this.language_level = language_level;
        this.id_user = id_user;
    }
    
    // Constructeur sans id_user pour la compatibilité avec le code existant
    public Preferences(String climat_pref, String country, String domain, String preferred_language,
                       String teaching_mode, String university_type, String cultural_activities, String language_level) {
        this.climat_pref = climat_pref;
        this.country = country;
        this.domain = domain;
        this.preferred_language = preferred_language;
        this.teaching_mode = teaching_mode;
        this.university_type = university_type;
        this.cultural_activities = cultural_activities;
        this.language_level = language_level;
        this.id_user = 0; // Default value
    }

    @Override
    public String toString() {
        return "Preferences{" +
                "climat_pref='" + climat_pref + '\'' +
                ", country='" + country + '\'' +
                ", domain='" + domain + '\'' +
                ", preferred_language='" + preferred_language + '\'' +
                ", teaching_mode='" + teaching_mode + '\'' +
                ", university_type='" + university_type + '\'' +
                ", cultural_activities='" + cultural_activities + '\'' +
                ", language_level='" + language_level + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClimat_pref() {
        return climat_pref;
    }

    public void setClimat_pref(String climat_pref) {
        this.climat_pref = climat_pref;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPreferred_language() {
        return preferred_language;
    }

    public void setPreferred_language(String preferred_language) {
        this.preferred_language = preferred_language;
    }

    public String getTeaching_mode() {
        return teaching_mode;
    }

    public void setTeaching_mode(String teaching_mode) {
        this.teaching_mode = teaching_mode;
    }

    public String getUniversity_type() {
        return university_type;
    }

    public void setUniversity_type(String university_type) {
        this.university_type = university_type;
    }

    public String getCultural_activities() {
        return cultural_activities;
    }

    public void setCultural_activities(String cultural_activities) {
        this.cultural_activities = cultural_activities;
    }

    public String getLanguage_level() {
        return language_level;
    }

    public void setLanguage_level(String language_level) {
        this.language_level = language_level;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }
}
