package com.pejana.oauth2login.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Contact {
    private final String resourceId;
    private final String name;
    private final String email;
    private final String phoneNumber;

    public Contact(String resourceId, String name, String email, String phoneNumber) {
        this.resourceId = resourceId;
        this.name = name != null ? name : "";
        this.email = email != null ? email : "";
        this.phoneNumber = phoneNumber != null ? phoneNumber : "";
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }


    @Override
    public String toString() {
        return "Contact{" +
                "resourceId='" + resourceId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}