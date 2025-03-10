package com.delmar.googlecontacts.model;

public class Contacts {
    private String resourceName;
    private String name;
    private String email;
    private String phoneNumber;

    public Contacts() {
    }
    public Contacts(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}