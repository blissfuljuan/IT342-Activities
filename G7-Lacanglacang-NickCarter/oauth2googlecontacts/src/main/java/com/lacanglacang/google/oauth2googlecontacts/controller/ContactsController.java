package com.lacanglacang.google.oauth2googlecontacts.controller;

import java.io.IOException;
import java.util.List;

import com.lacanglacang.google.oauth2googlecontacts.service.GoogleContactsService;
import com.google.api.services.people.v1.model.Person;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacts")
public class ContactsController {

    private final GoogleContactsService googleContactsService;

    public ContactsController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping
    public List<Person> getContacts() throws IOException {
        List<Person> contacts = googleContactsService.getContacts();
        System.out.println("Fetched Contacts: " + contacts);
        return contacts;
    }

}