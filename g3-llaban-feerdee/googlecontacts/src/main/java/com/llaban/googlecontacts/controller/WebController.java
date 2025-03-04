package com.llaban.googlecontacts.controller;

import com.google.api.services.people.v1.model.Person;
import com.llaban.googlecontacts.service.GoogleContactService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Controller

public class WebController {

    private final GoogleContactService googleContactService;

    public WebController(GoogleContactService googleContactService) {
        this.googleContactService = googleContactService;
    }

    @GetMapping("/contacts")
    public String showContacts(Model model) {
        try {
            List<Person> contacts = googleContactService.getContacts();

            // Ensure contacts are not null before sorting
            if (contacts != null) {
                contacts.sort(Comparator.comparing(
                        person -> (person.getNames() != null && !person.getNames().isEmpty())
                                ? person.getNames().get(0).getDisplayName()
                                : "",
                        String.CASE_INSENSITIVE_ORDER
                ));
            }

            System.out.println("Fetched contacts: " + contacts.size()); // Debugging
            model.addAttribute("contacts", contacts);
            return "contacts"; // Refers to contacts.html in /templates
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to fetch contacts.");
            return "error"; // Refers to error.html in /templates
        }
    }





    @PostMapping("/api/contacts/create")
    public String createContact(
            @RequestParam String givenName,
            @RequestParam String familyName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber) {
        try {
            Person newContact = googleContactService.createContact(givenName, familyName, email, phoneNumber);
            System.out.println("Contact created: " + newContact.getResourceName());
            return "redirect:/contacts"; // Refresh contacts page
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @PostMapping("/api/contacts/update")
    public String updateContact(
            @RequestParam String resourceName,
            @RequestParam String givenName,
            @RequestParam String familyName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber) {
        try {
            googleContactService.updateContact(resourceName, givenName, familyName, email, phoneNumber);
            System.out.println("Contact updated: " + resourceName);
            return "redirect:/contacts";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @PostMapping("/api/contacts/delete")
    public String deleteContact(@RequestParam String resourceName) {
        try {
            googleContactService.deleteContact(resourceName);
            System.out.println("Deleted contact: " + resourceName);
            return "redirect:/contacts";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }
}
