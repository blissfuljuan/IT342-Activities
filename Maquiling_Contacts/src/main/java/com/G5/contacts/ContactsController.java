package com.G5.contacts;

import com.G5.contacts.GoogleContactsService;
import com.google.api.services.people.v1.model.Person;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ui.Model;
import java.io.IOException;
import java.util.List;


@Controller
@RequestMapping("")
public class ContactsController {

    private final GoogleContactsService googleContactsService;

    public ContactsController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping("/contacts")
    public String contacts(Model model, OAuth2AuthenticationToken authentication) {
        try {
            String fullName = authentication.getPrincipal().getAttribute("name"); // Fetch full name
            String firstName = (fullName != null) ? fullName.split(" ")[0] : "User"; // Extract first name
            model.addAttribute("firstName", firstName); // Pass first name to Thymeleaf
            List<Person> contacts = googleContactsService.getContacts(authentication);
            model.addAttribute("contacts", contacts != null ? contacts : List.of());
        } catch (IOException e) {
            model.addAttribute("error", "Failed to fetch contacts.");
        }
        return "contacts"; 
    }

    //Add Contact
    @PostMapping("/contacts/add")
    public String addContact(@RequestParam String name, @RequestParam String email, @RequestParam String phone, OAuth2AuthenticationToken authentication) {
        try {
            googleContactsService.createContact(authentication, name, email, phone);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/contacts";
    }

    //Update Contact
    @PostMapping("/contacts/update")
    public String updateContact(@RequestParam String resourceName, @RequestParam String name, @RequestParam String email, @RequestParam String phone, OAuth2AuthenticationToken authentication) {
        try {
            googleContactsService.updateContact(authentication, resourceName, name, email, phone);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/contacts";
    }

    //Delete Contact
    @PostMapping("/contacts/delete")
    public String deleteContact(@RequestParam String resourceName, OAuth2AuthenticationToken authentication) {
        try {
            googleContactsService.deleteContact(authentication, resourceName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/contacts";
    }
}
