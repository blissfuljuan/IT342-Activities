package com.kho.googlecontacts.controller;

import com.google.api.services.people.v1.model.Person;
import com.kho.googlecontacts.service.GoogleContactsService;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactsController {

    private final GoogleContactsService googleContactsService;

    public ContactsController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping
    public List<Person> getContacts(
            @RequestParam(defaultValue = "50") int pageSize,
            @RequestParam(required = false) String pageToken) throws IOException {
        List<Person> contacts = googleContactsService.getContacts(pageSize, pageToken);
        System.out.println("Fetched Contacts: " + contacts);
        return contacts;
    }

    @GetMapping("/next-page")
    public String getNextPageToken(
            @RequestParam(defaultValue = "50") int pageSize,
            @RequestParam(required = false) String pageToken) throws IOException {
        return googleContactsService.getNextPageToken(pageSize, pageToken);
    }
}