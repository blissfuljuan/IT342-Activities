package com.suson.googlecontactsintegration.controller;

import com.google.api.services.people.v1.model.Person;
import com.suson.googlecontactsintegration.service.GooglePeopleService;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactsController {

    private final GooglePeopleService googlePeopleService;

    public ContactsController(GooglePeopleService googlePeopleService) {
        this.googlePeopleService = googlePeopleService;
    }

    @GetMapping
    public List<Person> getContacts() throws IOException {
        return googlePeopleService.getContacts(); // Returns JSON
    }
}