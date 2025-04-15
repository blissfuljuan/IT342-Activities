package com.vijar.oauth2login.model;

import java.util.List;

public class Contacts {
    private String resourceName;
    private String name;
    private List<String> email;
    private List<String> phoneNumber;

    public Contacts() {
    }
    public Contacts(String name, List<String> email) {
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

    public List<String> getEmail() {
        return email;
    }

    public void setEmail(List<String> email) {
        this.email = email;
    }

    public List<String> getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(List<String> phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}