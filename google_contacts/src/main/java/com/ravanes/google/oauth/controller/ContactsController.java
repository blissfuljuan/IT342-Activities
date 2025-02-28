package com.ravanes.google.oauth.controller;

import com.ravanes.google.oauth.model.Contact;
import com.ravanes.google.oauth.service.GoogleContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactsController {

    @Autowired
    private GoogleContactsService googleContactsService;

    @GetMapping
    public List<Contact> getAllContacts() {
        return googleContactsService.getContacts();
    }

    @PostMapping
    public Contact addContact(@RequestBody Contact contact) {
        return googleContactsService.addContact(contact);
    }

    @PutMapping("/{id}")
    public Contact updateContact(@PathVariable String id, @RequestBody Contact contact) {
        return googleContactsService.updateContact(id, contact);
    }

    @DeleteMapping("/{id}")
    public void deleteContact(@PathVariable String id) {
        googleContactsService.deleteContact(id);
    }
}
