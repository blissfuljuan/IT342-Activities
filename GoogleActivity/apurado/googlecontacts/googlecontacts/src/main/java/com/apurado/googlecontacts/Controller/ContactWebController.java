package com.apurado.googlecontacts.Controller;

import com.apurado.googlecontacts.Service.GoogleContactsService;
import com.google.api.services.people.v1.model.Person;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
public class ContactWebController {

    private final GoogleContactsService contactsService;

    public ContactWebController(GoogleContactsService contactsService) {
        this.contactsService = contactsService;
    }

    @GetMapping("/contacts")
    public String displayContacts(Model model) {
        try {
            List<Person> contacts = contactsService.fetchContacts();
            System.out.println("Fetched contacts count: " + contacts.size());
            model.addAttribute("contacts", contacts);
            return "contacts"; // Refers to contacts.html in /templates
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to fetch contacts.");
            return "error"; // Refers to error.html in /templates
        }
    }

    @PostMapping("/api/contacts/create")
    public String addContact(
            @RequestParam String givenName,
            @RequestParam String familyName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber) throws IOException {
        Person newContact = contactsService.addContact(givenName, familyName, email, phoneNumber);
        System.out.println("Contact created: " + newContact.getResourceName());
        return "redirect:/contacts";
    }

    @PostMapping("/api/contacts/update")
    public String modifyContact(
            @RequestParam String resourceName,
            @RequestParam String givenName,
            @RequestParam String familyName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber) {
        try {
            contactsService.modifyContact(resourceName, givenName, familyName, email, phoneNumber);
            System.out.println("Contact updated: " + resourceName);
            return "redirect:/contacts";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @PostMapping("/api/contacts/delete")
    public String removeContact(@RequestParam String resourceName) {
        try {
            contactsService.removeContact(resourceName);
            System.out.println("Deleted contact: " + resourceName);
            return "redirect:/contacts";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }
}