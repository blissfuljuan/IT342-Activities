package com.rivera.googlecontacts.controller;

import com.google.api.services.people.v1.model.Person;

import java.io.IOException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.rivera.googlecontacts.service.GoogleContactsService;

@RestController
@RequestMapping({"/api/contacts"})
public class ContactsController {
    private final GoogleContactsService googleContactsService;

    public ContactsController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping
    public List<Person> getContacts() throws IOException {
        List<Person> contacts = this.googleContactsService.getContacts();
        System.out.println("Fetched Contacts: " + String.valueOf(contacts));
        return contacts;
    }
}