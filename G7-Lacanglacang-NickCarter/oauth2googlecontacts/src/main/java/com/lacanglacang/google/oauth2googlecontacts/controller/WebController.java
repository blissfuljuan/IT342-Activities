package com.lacanglacang.google.oauth2googlecontacts.controller;

import com.lacanglacang.google.oauth2googlecontacts.service.GoogleContactsService;
import com.google.api.services.people.v1.model.Person;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    // Service for handling Google Contacts operations
    private final GoogleContactsService googleContactsService;

    // Constructor for dependency injection of GoogleContactsService
    public WebController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping("/")
    public String getUser(@AuthenticationPrincipal OAuth2User oAuth2User, Map<String, Object> model) {
        if (oAuth2User != null) {
            model.put("user", oAuth2User.getAttributes());
        } else {
            model.put("error", "User not authenticated");
        }
        return "profile"; // Returns profile.html
    }

    // Endpoint to retrieve and display all contacts
    @GetMapping("/contacts")
    public String showContacts(Model model) {
        try {
            List<Person> contacts = googleContactsService.getContacts();
            System.out.println("Fetched contacts: " + contacts.size());
            model.addAttribute("contacts", contacts);
            return "contacts"; // Refers to contacts.html in /templates
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to fetch contacts.");
            return "error";// Refers to error.html in /templates
        }
    }

    // Endpoint to create a new contact with given details
    @PostMapping("/api/contacts/create")
    public String createContact(
            @RequestParam String givenName,
            @RequestParam String familyName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber) throws IOException {

        Person newContact = googleContactsService.createContact(givenName, familyName, email, phoneNumber);
        System.out.println("Contact created: " + newContact.getResourceName());
        return "redirect:/contacts";
    }

    // Endpoint to update an existing contact's information
    @PostMapping("/api/contacts/update")
    public String updateContact(
            @RequestParam String resourceName,
            @RequestParam String givenName,
            @RequestParam String familyName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber) {

        try {
            // Update contact using service method
            googleContactsService.updateContact(resourceName, givenName, familyName, email, phoneNumber);
            System.out.println("Contact updated: " + resourceName);
            return "redirect:/contacts";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    // Endpoint to delete a contact by its resource name
    @PostMapping("/api/contacts/delete")
    public String deleteContact(@RequestParam String resourceName) {
        try {
            // Delete contact using service method
            googleContactsService.deleteContact(resourceName);
            System.out.println("Deleted contact: " + resourceName);
            return "redirect:/contacts";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }
}