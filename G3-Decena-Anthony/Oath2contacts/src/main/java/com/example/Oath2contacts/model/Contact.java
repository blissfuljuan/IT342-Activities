package com.example.Oath2contacts.model;

import java.util.List;

public class Contact {
    private String name;
    private List<String> emails;
    private List<String> phones;

    public Contact(String name){
        this.name = name;
    }
    public String getName(){return name;}
    public List<String> getEmails(){return emails;}
    public void setEmails(List<String> emails){
        this.emails=emails;
    }

    public List<String> getPhones(){
        return phones;
    }
    public void setPhones(List<String> phones){
        this.phones = phones;
    }
}
