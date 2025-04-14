package edu.cit.CIT_UCommunityForums.model;

public class Contact {
    private String name;
    private String email;
    private String phone;

    // No-arg constructor
    public Contact() {
    }

    // New two-arg constructor
    public Contact(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Existing three-arg constructor
    public Contact(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Getters & setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
