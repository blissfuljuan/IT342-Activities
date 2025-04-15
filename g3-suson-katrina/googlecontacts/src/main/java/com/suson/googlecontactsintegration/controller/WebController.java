package com.suson.googlecontactsintegration.controller;

import com.google.api.services.people.v1.model.Person;
import com.suson.googlecontactsintegration.service.GooglePeopleService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Comparator;

@Controller
public class WebController {

    private final GooglePeopleService googlePeopleService;

    public WebController(GooglePeopleService googlePeopleService) {
        this.googlePeopleService = googlePeopleService;
    }

    @GetMapping("/contacts")
    public String showContacts(Model model) {
        try {
            List<Person> contacts = googlePeopleService.getContacts();

            // Sort contacts alphabetically by display name
            contacts.sort(Comparator.comparing(
                    c -> c.getNames() != null && !c.getNames().isEmpty()
                            ? c.getNames().get(0).getDisplayName().toLowerCase()
                            : "",
                    String.CASE_INSENSITIVE_ORDER
            ));

            System.out.println("Fetched and sorted contacts: " + contacts.size()); // Debugging

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