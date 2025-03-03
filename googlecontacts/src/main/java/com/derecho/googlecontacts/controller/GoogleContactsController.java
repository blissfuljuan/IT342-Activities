package com.derecho.googlecontacts.controller;

import com.derecho.googlecontacts.service.GoogleContactsService;
import com.google.api.services.people.v1.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/contacts")
public class GoogleContactsController {

    @Autowired
    private GoogleContactsService googleContactsService;

    @GetMapping("/full-sync")
    public List<Person> fullSync() throws IOException {
        return googleContactsService.getAllContacts();
    }

    @GetMapping("/incremental-sync")
    public List<Person> incrementalSync() throws IOException {
        return googleContactsService.getIncrementalChanges();
    }

    @GetMapping("/search")
    public List<Person> searchContacts(@RequestParam String query) throws IOException, InterruptedException {
        return googleContactsService.searchContacts(query);
    }

    @PostMapping("/create")
    public Person createContact(@RequestParam String firstName, @RequestParam String lastName) throws IOException {
        return googleContactsService.createContact(firstName, lastName);
    }

    @PutMapping("/update")
    public Person updateContact(@RequestParam String resourceName, @RequestParam String email) throws IOException {
        return googleContactsService.updateContact(resourceName, email);
    }

    @DeleteMapping("/delete")
    public void deleteContact(@RequestParam String resourceName) throws IOException {
        googleContactsService.deleteContact(resourceName);
    }
}
