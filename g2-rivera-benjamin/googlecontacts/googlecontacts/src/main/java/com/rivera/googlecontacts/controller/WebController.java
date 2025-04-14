package com.rivera.googlecontacts.controller;

import com.google.api.services.people.v1.model.Person;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.rivera.googlecontacts.service.GoogleContactsService;

import java.io.IOException;
import java.util.List;

@Controller
public class WebController {
    private final GoogleContactsService googleContactsService;

    public WebController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping({"/contacts"})
    public String showContacts(Model model) {
        try {
            List<Person> contacts = this.googleContactsService.getContacts();
            System.out.println("Fetched contacts: " + contacts.size());
            model.addAttribute("contacts", contacts);
            return "contacts";
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to fetch contacts.");
            return "error";
        }
    }

    @PostMapping({"/api/contacts/create"})
    public String createContact(
            @RequestParam String givenName,
            @RequestParam String familyName,
            @RequestParam(required = false) String[] emails,
            @RequestParam(required = false) String[] phoneNumbers) throws IOException {
        Person newContact = this.googleContactsService.createContact(givenName, familyName, emails, phoneNumbers);
        System.out.println("Contact created: " + newContact.getResourceName());
        return "redirect:/contacts";
    }

    @PostMapping({"/api/contacts/update"})
    public String updateContact(
            @RequestParam String resourceName,
            @RequestParam String givenName,
            @RequestParam String familyName,
            @RequestParam(required = false) String[] emails,
            @RequestParam(required = false) String[] phoneNumbers) {
        try {
            this.googleContactsService.updateContact(resourceName, givenName, familyName, emails, phoneNumbers);
            System.out.println("Contact updated: " + resourceName);
            return "redirect:/contacts";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @PostMapping({"/api/contacts/delete"})
    public String deleteContact(@RequestParam String resourceName) {
        try {
            this.googleContactsService.deleteContact(resourceName);
            System.out.println("Deleted contact: " + resourceName);
            return "redirect:/contacts";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }
}