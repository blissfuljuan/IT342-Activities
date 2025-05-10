package com.duterte.googlecontacts.controller;

import com.google.api.services.people.v1.model.Person;
import com.duterte.googlecontacts.service.GoogleContactsService;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactsController {

    private final GoogleContactsService googleContactsService;

    public ContactsController(GoogleContactsService googleContactsService){
        this.googleContactsService = googleContactsService;
    }

    @GetMapping
    public List<Person> getContacts() throws IOException {
        List<Person> contacts = googleContactsService.getContacts();
        System.out.println("Fetched Contacts: " + contacts);
        return contacts;
    }
}
