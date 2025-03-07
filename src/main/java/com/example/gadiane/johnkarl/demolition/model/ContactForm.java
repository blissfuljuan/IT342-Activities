package com.example.gadiane.johnkarl.demolition.model;

public class ContactForm {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String resourceName;
    private String name; // Added for full name

    // Getters and setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    
    // Added for full name
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}