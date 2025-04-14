package com.Yu.midterm.Model;

import java.util.ArrayList;
import java.util.List;

public class Contact {
    private String resourceName;
    private String firstName;
    private String lastName;
    private List<String> emails = new ArrayList<>();
    private List<String> phoneNumbers = new ArrayList<>();

    // Getters and setters
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public List<String> getEmails() { return emails; }
    public void setEmails(List<String> emails) { this.emails = emails; }
    public List<String> getPhoneNumbers() { return phoneNumbers; }
    public void setPhoneNumbers(List<String> phoneNumbers) { this.phoneNumbers = phoneNumbers; }

    // Add single email/phone for form compatibility
    public String getEmail() { return emails.isEmpty() ? "" : emails.get(0); }
    public void setEmail(String email) {
        if (email != null && !email.isEmpty()) {
            if (emails.isEmpty()) emails.add(email);
            else emails.set(0, email);
        }
    }
    public String getPhoneNumber() { return phoneNumbers.isEmpty() ? "" : phoneNumbers.get(0); }
    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            if (phoneNumbers.isEmpty()) phoneNumbers.add(phoneNumber);
            else phoneNumbers.set(0, phoneNumber);
        }
    }
}