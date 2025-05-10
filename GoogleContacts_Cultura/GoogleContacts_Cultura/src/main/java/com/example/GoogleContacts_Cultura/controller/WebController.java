package com.example.GoogleContacts_Cultura.controller;

import java.io.IOException;
import java.util.List;

import com.google.api.services.people.v1.model.Person;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.GoogleContacts_Cultura.controller.GoogleContactsController;
import com.example.GoogleContacts_Cultura.service.GoogleContactsService;

@Controller
public class WebController {

    private final GoogleContactsService googleContactsService;

    public WebController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping("/contacts")
    public String showContacts(Model model) {
        try {
            List<Person> contacts = googleContactsService.getContacts();
            if (contacts == null || contacts.isEmpty()) {
                model.addAttribute("error", "No contacts found.");
                return "contacts"; // Return the template with an error message
            }
            model.addAttribute("contacts", contacts);
            return "contacts";
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to fetch contacts.");
            return "error";
        }
    }

    @PostMapping("/api/contacts/create")
    public String createContact(
            @RequestParam String givenName,
            @RequestParam String familyName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber) throws IOException {
        // Call the service to create the contact
        Person newContact = googleContactsService.createContact(givenName, familyName, email, phoneNumber);
        System.out.println("Contact created: " + newContact.getResourceName());
        // Redirect back to the contacts page
        return "redirect:/contacts"; // Redirect to the HTML page served by WebController
    }

    @PostMapping("/api/contacts/update")
    public String updateContact(
            @RequestParam String resourceName,
            @RequestParam String givenName,
            @RequestParam String familyName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber) {
        try {
            googleContactsService.updateContact(resourceName, givenName, familyName, email, phoneNumber);
            System.out.println("Contact updated: " + resourceName);
            return "redirect:/contacts"; // Refresh the contact list
        } catch (IOException e) {
            e.printStackTrace();
            return "error"; // Show an error page
        }
    }

    @PostMapping("/api/contacts/delete")
    public String deleteContact(@RequestParam String resourceName) {
        try {
            googleContactsService.deleteContact(resourceName);
            System.out.println("Deleted contact: " + resourceName);
            return "redirect:/contacts"; // Refresh the contacts list
        } catch (IOException e) {
            e.printStackTrace();
            return "error"; // Show an error page
        }
    }

}