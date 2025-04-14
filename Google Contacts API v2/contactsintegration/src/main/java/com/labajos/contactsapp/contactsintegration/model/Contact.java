package com.labajos.contactsapp.contactsintegration.model;

import java.util.ArrayList;
import java.util.List;

public class Contact {
    private String name;
    private List<String> email;
    private List<String> phone;
    private String resourceName;

    public Contact() {}

    public Contact(String name, List<String> email, List<String> phone, String resourceName) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.resourceName = resourceName;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public List<String> getEmail() { return email != null ? email : new ArrayList<>(); }
    public void setEmail(List<String> email) { this.email = email; }

    public List<String> getPhone() { return phone != null ? phone : new ArrayList<>(); }
    public void setPhone(List<String> phone) { this.phone = phone; }
    
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
}