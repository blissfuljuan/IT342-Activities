package com.example.contactsapp.service;

import com.example.contactsapp.model.Contact;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoogleContactService {

    // Simulated contact storage (replace with actual API calls)
    private Map<String, Contact> contacts = new HashMap<>();

    // Retrieve all contacts
    public List<Contact> getAllContacts() {
        return new ArrayList<>(contacts.values());
    }

    // Retrieve a specific contact by ID
    public Contact getContactById(String id) {
        return contacts.get(id);
    }

    // Create a new contact
    public Contact createContact(Contact contact) {
        String id = UUID.randomUUID().toString();
        contact.setId(id);
        contacts.put(id, contact);
        return contact;
    }

    // Update an existing contact
    public Contact updateContact(String id, Contact updatedContact) {
        if (contacts.containsKey(id)) {
            updatedContact.setId(id);
            contacts.put(id, updatedContact);
            return updatedContact;
        }
        return null;
    }

    // Delete a contact
    public boolean deleteContact(String id) {
        return contacts.remove(id) != null;
    }
}