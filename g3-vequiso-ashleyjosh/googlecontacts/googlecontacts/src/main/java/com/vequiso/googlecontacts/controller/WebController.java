package com.vequiso.googlecontacts.controller;

import java.io.IOException;
import java.util.List;

import com.vequiso.googlecontacts.service.GooglePeopleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.api.services.people.v1.model.Person;

@Controller
public class WebController {

    private final com.vequiso.googlecontacts.service.GooglePeopleService googlePeopleService;

    public WebController(GooglePeopleService googlePeopleService) {
        this.googlePeopleService = googlePeopleService;
    }

    @GetMapping("/contacts")
    public String showContacts(Model model) {
        try {
            List<Person> contacts = googlePeopleService.getContacts();
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
        Person newContact = googlePeopleService.createContact(givenName, familyName, email, phoneNumber);
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
            googlePeopleService.updateContact(resourceName, givenName, familyName, email, phoneNumber);
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
            googlePeopleService.deleteContact(resourceName);
            System.out.println("Deleted contact: " + resourceName);
            return "redirect:/contacts"; // Refresh the contacts list
        } catch (IOException e) {
            e.printStackTrace();
            return "error"; // Show an error page
        }
    }

}
