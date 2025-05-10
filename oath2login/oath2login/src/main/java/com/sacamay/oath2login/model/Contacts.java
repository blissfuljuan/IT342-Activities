package com.sacamay.oath2login.model;

public class Contacts {
    private String sub;           // Unique identifier (subject ID)
    private String name;          // Full name
    private String givenName;     // First name
    private String familyName;    // Last name
    private String picture;       // Profile picture URL
    private String email;         // Authenticated email address
    private boolean emailVerified; // Indicates if the email is verified

    // Constructor
    public Contacts() {}

    public Contacts(String sub, String name, String givenName, String familyName, String picture, String email, boolean emailVerified) {
        this.sub = sub;
        this.name = name;
        this.givenName = givenName;
        this.familyName = familyName;
        this.picture = picture;
        this.email = email;
        this.emailVerified = emailVerified;
    }

    // Getters and Setters
    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    // ToString Method
    @Override
    public String toString() {
        return "Contacts{" +
                "sub='" + sub + '\'' +
                ", name='" + name + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", picture='" + picture + '\'' +
                ", email='" + email + '\'' +
                ", emailVerified=" + emailVerified +
                '}';
    }
}
