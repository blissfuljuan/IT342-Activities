package com.deloscientos.google.contacts.dto;

import java.util.List;

public class Contact {
    private String resourceName;
    private String etag;
    private String name;
    private List<String> emails;
    private List<String> phoneNumbers;

    

    public Contact() {}

    public Contact(String resourceName, String etag, String name, List<String> emails, List<String> phoneNumbers) {
        this.resourceName = resourceName;
        this.etag = etag;
        this.name = name;
        this.emails = emails;
        this.phoneNumbers = phoneNumbers;
        
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<String> getEmails() { return emails; }
    public void setEmails(List<String> emails) { this.emails = emails; }

    public List<String> getPhoneNumbers() { return phoneNumbers; }
    public void setPhoneNumbers(List<String> phoneNumbers) { this.phoneNumbers = phoneNumbers; }


    


    @Override
    public String toString() {
        return "Contact{" +
                "resourceName='" + resourceName + '\'' +
                ", etag='" + etag + '\'' +
                ", name='" + name + '\'' +
                ", email='" + emails + '\'' +
                ", phoneNumber='" + phoneNumbers + '\'' +
                ", birthday='" +  + '\'' +
                '}';
    }
}

