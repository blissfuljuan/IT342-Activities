package com.example.gadiane.johnkarl.demolition.controller;

import com.example.gadiane.johnkarl.demolition.service.GoogleContactsService;
import com.example.gadiane.johnkarl.demolition.service.GoogleCredentialService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.people.v1.model.Person;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactsController {

    private final GoogleContactsService contactsService;
    private final GoogleCredentialService credentialService;

    public ContactsController(GoogleContactsService contactsService, GoogleCredentialService credentialService) {
        this.contactsService = contactsService;
        this.credentialService = credentialService;
    }

    @GetMapping
    public ResponseEntity<List<Person>> getAllContacts() {
        try {
            Credential credential = credentialService.getCredential();
            List<Person> contacts = contactsService.listContacts(credential);
            return ResponseEntity.ok(contacts);
        } catch (GeneralSecurityException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{resourceName}")
    public ResponseEntity<Person> getContact(@PathVariable String resourceName) {
        try {
            Credential credential = credentialService.getCredential();
            Person contact = contactsService.getContact(credential, resourceName);
            return ResponseEntity.ok(contact);
        } catch (GeneralSecurityException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Person> createContact(@RequestBody Person person) {
        try {
            Credential credential = credentialService.getCredential();
            Person createdContact = contactsService.createContact(credential, person);
            return ResponseEntity.ok(createdContact);
        } catch (GeneralSecurityException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{resourceName}")
    public ResponseEntity<Person> updateContact(@PathVariable String resourceName, @RequestBody Person person) {
        try {
            Credential credential = credentialService.getCredential();
            Person updatedContact = contactsService.updateContact(credential, resourceName, person);
            return ResponseEntity.ok(updatedContact);
        } catch (GeneralSecurityException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{resourceName}")
    public ResponseEntity<Void> deleteContact(@PathVariable String resourceName) {
        try {
            Credential credential = credentialService.getCredential();
            contactsService.deleteContact(credential, resourceName);
            return ResponseEntity.noContent().build();
        } catch (GeneralSecurityException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}