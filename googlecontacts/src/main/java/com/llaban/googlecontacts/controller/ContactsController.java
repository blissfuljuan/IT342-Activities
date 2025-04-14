package com.llaban.googlecontacts.controller;

import com.google.api.services.people.v1.model.Person;
import com.llaban.googlecontacts.service.GoogleContactService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/contacts") // Fix to ensure proper endpoint path
public class ContactsController {

    private final GoogleContactService googleContactService;

    public ContactsController(GoogleContactService googleContactService) {
        this.googleContactService = googleContactService;
    }

    @GetMapping
    public List<Person> getContacts() throws IOException {
        List<Person> contacts = googleContactService.getContacts();
        System.out.println("Fetched Contacts: " + contacts);
        return contacts;
    }
}
