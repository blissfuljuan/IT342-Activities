package com.porcina.oauth2login.service;

import com.porcina.oauth2login.model.Contacts;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoogleContactsService {

    // Simulated contact storage (replace with actual API calls)
    private Map<String, Contacts> contacts = new HashMap<>();

    // Retrieve all contacts
    public List<Contacts> getAllContacts() {
        return new ArrayList<>(contacts.values());
    }

    // Retrieve a specific contact by ID
    public Contacts getContactById(String id) {
        return contacts.get(id);
    }

    // Create a new contact
    public Contacts createContact(Contacts contact) {
        String id = UUID.randomUUID().toString();
        contact.setId(id);
        contacts.put(id, contact);
        return contact;
    }

    // Update an existing contact
    public Contacts updateContact(String id, Contacts updatedContact) {
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