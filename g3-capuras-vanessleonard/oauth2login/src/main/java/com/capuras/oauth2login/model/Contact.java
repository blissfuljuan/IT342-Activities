package com.capuras.oauth2login.model;

public class Contact {

    private String resourceId;
    private String name;
    private String email;
    private String phoneNumber;

    public Contact() {
    }

    public Contact(String resourceId, String name, String email, String phoneNumber) {
        this.resourceId = resourceId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
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