package com.carbungco.GoogleContacts.Controller;

import com.carbungco.GoogleContacts.Service.GoogleContactsService;
import com.google.api.services.people.v1.model.Person;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
public class WebController {

    // Service for handling Google Contacts operations
    private final GoogleContactsService googleContactsService;

    // Constructor for dependency injection of GoogleContactsService
    public WebController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
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
            @RequestParam(required = false, defaultValue = "") List<String> emails,
            @RequestParam(required = false, defaultValue = "") List<String> phoneNumbers) throws IOException {

        System.out.println("Creating contact:");
        System.out.println("Given Name: " + givenName);
        System.out.println("Family Name: " + familyName);
        System.out.println("Emails: " + emails);
        System.out.println("Phone Numbers: " + phoneNumbers);

        googleContactsService.createContact(givenName, familyName, emails, phoneNumbers);
        return "redirect:/contacts";
    }



    // Endpoint to update an existing contact's information
    @PostMapping("/api/contacts/update")
    public String updateContact(
            @RequestParam String resourceName,
            @RequestParam String givenName,
            @RequestParam String familyName,
            @RequestParam(required = false, defaultValue = "") List<String> emails,
            @RequestParam(required = false, defaultValue = "") List<String> phoneNumbers) {

        try {
            googleContactsService.updateContact(resourceName, givenName, familyName, emails, phoneNumbers);
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
