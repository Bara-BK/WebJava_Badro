package tn.badro.entities;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

public class User {
    private final IntegerProperty id;
    private final StringProperty nom;
    private final StringProperty prenom;
    private final StringProperty email;
    private final StringProperty password;
    private final StringProperty numtlf;
    private final IntegerProperty age;
    private final StringProperty roles;

    public User() {
        this.id = new SimpleIntegerProperty();
        this.nom = new SimpleStringProperty();
        this.prenom = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.password = new SimpleStringProperty();
        this.numtlf = new SimpleStringProperty();
        this.age = new SimpleIntegerProperty();
        this.roles = new SimpleStringProperty();
    }

    public User(int id, String nom, String prenom, String email, String password, 
                String numtlf, int age, String roles) {
        this.id = new SimpleIntegerProperty(id);
        this.nom = new SimpleStringProperty(nom);
        this.prenom = new SimpleStringProperty(prenom);
        this.email = new SimpleStringProperty(email);
        this.password = new SimpleStringProperty(password);
        this.numtlf = new SimpleStringProperty(numtlf);
        this.age = new SimpleIntegerProperty(age);
        this.roles = new SimpleStringProperty(roles);
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

    // Nom
    public String getNom() {
        return nom.get();
    }
    public void setNom(String value) {
        nom.set(value);
    }
    public StringProperty nomProperty() {
        return nom;
    }

    // Prenom
    public String getPrenom() {
        return prenom.get();
    }
    public void setPrenom(String value) {
        prenom.set(value);
    }
    public StringProperty prenomProperty() {
        return prenom;
    }

    // Email
    public String getEmail() {
        return email.get();
    }
    public void setEmail(String value) {
        email.set(value);
    }
    public StringProperty emailProperty() {
        return email;
    }

    // Password
    public String getPassword() {
        return password.get();
    }
    public void setPassword(String value) {
        password.set(value);
    }
    public StringProperty passwordProperty() {
        return password;
    }

    // Telephone
    public String getNumtlf() {
        return numtlf.get();
    }
    public void setNumtlf(String value) {
        numtlf.set(value);
    }
    public StringProperty numtlfProperty() {
        return numtlf;
    }

    // Age
    public int getAge() {
        return age.get();
    }
    public void setAge(int value) {
        age.set(value);
    }
    public IntegerProperty ageProperty() {
        return age;
    }

    // Roles
    public String getRoles() {
        return roles.get();
    }
    public void setRoles(String value) {
        roles.set(value);
    }
    public StringProperty rolesProperty() {
        return roles;
    }

    @Override
    public String toString() {
        return nom.get() + " " + prenom.get();
    }
}