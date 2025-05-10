package com.cabana.oauth2.oauth2.service;

import com.cabana.oauth2.oauth2.model.Contact;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoogleContactService {

    private Map<String, Contact> contacts = new HashMap<>();

    public List<Contact> getAllContacts() {
        return new ArrayList<>(contacts.values());
    }

    public Contact getContactById(String id) {
        return contacts.get(id);
    }

    public Contact createContact(Contact contact) {
        String id = UUID.randomUUID().toString();
        contact.setId(id);
        contacts.put(id, contact);
        return contact;
    }

    public Contact updateContact(String id, Contact updatedContact) {
        if (contacts.containsKey(id)) {
            updatedContact.setId(id);
            contacts.put(id, updatedContact);
            return updatedContact;
        }
        return null;
    }

    public boolean deleteContact(String id) {
        return contacts.remove(id) != null;
    }
}