package com.apurado.googlecontacts.Controller;

import com.apurado.googlecontacts.Service.GoogleContactsService;
import com.google.api.services.people.v1.model.Person;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactApiController {

    private final GoogleContactsService contactsService;

    public ContactApiController(GoogleContactsService contactsService) {
        this.contactsService = contactsService;
    }

    @GetMapping
    public List<Person> listContacts() throws IOException {
        List<Person> contacts = contactsService.fetchContacts();
        System.out.println("Fetched Contacts: " + contacts);
        return contacts;
    }
}
