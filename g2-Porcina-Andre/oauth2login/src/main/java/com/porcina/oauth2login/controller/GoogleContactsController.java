package com.porcina.oauth2login.controller;

import com.porcina.oauth2login.model.Contacts;
import com.porcina.oauth2login.service.GoogleContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
public class GoogleContactsController {

    @Autowired
    private GoogleContactsService contactService;

    // Retrieve all contacts
    @GetMapping
    public ResponseEntity<List<Contacts>> getAllContacts() {
        return ResponseEntity.ok(contactService.getAllContacts());
    }

    // Retrieve a single contact by ID
    @GetMapping("/{id}")
    public ResponseEntity<Contacts> getContactById(@PathVariable String id) {
        Contacts contact = contactService.getContactById(id);
        return contact != null ? ResponseEntity.ok(contact) : ResponseEntity.notFound().build();
    }

    // Create a new contact
    @PostMapping
    public ResponseEntity<Contacts> createContact(@RequestBody Contacts contact) {
        Contacts newContact = contactService.createContact(contact);
        return ResponseEntity.status(HttpStatus.CREATED).body(newContact);
    }

    // Update an existing contact
    @PutMapping("/{id}")
    public ResponseEntity<Contacts> updateContact(@PathVariable String id, @RequestBody Contacts updatedContact) {
        Contacts contact = contactService.updateContact(id, updatedContact);
        return contact != null ? ResponseEntity.ok(contact) : ResponseEntity.notFound().build();
    }

    // Delete a contact
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable String id) {
        return contactService.deleteContact(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}