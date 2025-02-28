package com.ravanes.google.oauth.service;

import com.ravanes.google.oauth.model.Contact;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleContactsService {

    public List<Contact> getContacts() {
        // Placeholder for actual API integration
        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact("1", "John Doe", "john.doe@example.com", "+123456789"));
        contacts.add(new Contact("2", "Jane Smith", "jane.smith@example.com", "+987654321"));
        return contacts;
    }

    public Contact addContact(Contact contact) {
        // Placeholder logic
        contact.setId(String.valueOf(System.currentTimeMillis()));
        return contact;
    }

    public Contact updateContact(String id, Contact contact) {
        // Placeholder logic
        contact.setId(id);
        return contact;
    }

    public void deleteContact(String id) {
        // Placeholder logic (actual API call would delete the contact)
    }
}
