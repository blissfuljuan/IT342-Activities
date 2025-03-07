package com.cabana.oauth2.oauth2.controller;

import com.cabana.oauth2.oauth2.model.Contact;
import com.cabana.oauth2.oauth2.service.GoogleContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    @Autowired
    private GoogleContactService contactService;

    // Retrieve all contacts
    @GetMapping
    public ResponseEntity<List<Contact>> getAllContacts() {
        return ResponseEntity.ok(contactService.getAllContacts());
    }

    // Retrieve a single contact by ID
    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable String id) {
        Contact contact = contactService.getContactById(id);
        return contact != null ? ResponseEntity.ok(contact) : ResponseEntity.notFound().build();
    }

    // Create a new contact
    @PostMapping
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact) {
        Contact newContact = contactService.createContact(contact);
        return ResponseEntity.status(HttpStatus.CREATED).body(newContact);
    }

    // Update an existing contact
    @PutMapping("/update/{id}")
    public ResponseEntity<Contact> updateContact(@PathVariable String id, @RequestBody Contact updatedContact) {
        Contact contact = contactService.updateContact(id, updatedContact);
        return contact != null ? ResponseEntity.ok(contact) : ResponseEntity.notFound().build();
    }

    // Delete a contact
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable String id) {
        return contactService.deleteContact(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}