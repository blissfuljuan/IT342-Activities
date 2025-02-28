package com.Lacaba.GoogleContacts.model;

public class Contacts {
    private String name;
    private String email;

    // **No-Args Constructor (Needed for Spring & JSON Processing)**
    public Contacts() {}

    // **Parameterized Constructor**
    public Contacts(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // **Getters and Setters**
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
}
