package com.gitgano.googlecontacts.Controller;

import com.gitgano.googlecontacts.model.Contact;
import com.gitgano.googlecontacts.Service.GooglePeopleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactController {

    @Autowired
    private GooglePeopleService peopleService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/contacts";
    }

    @GetMapping("/contacts")
    public String getContacts(Model model, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {
        try {
            model.addAttribute("contacts", peopleService.getContacts(authorizedClient));
            model.addAttribute("contact", new Contact()); // Add empty contact for form binding
            return "contacts";
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching contacts");
            model.addAttribute("contact", new Contact());
            return "contacts";
        }
    }

    @PostMapping("/contacts/add")
    public String addContact(@ModelAttribute Contact contact, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient, RedirectAttributes redirectAttributes) {
        try {
            peopleService.createContact(authorizedClient, contact);
            redirectAttributes.addFlashAttribute("success", "Contact added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding contact");
        }
        return "redirect:/contacts";
    }

    @PostMapping("/contacts/update")
    public String updateContact(
            @RequestParam String resourceName,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
            RedirectAttributes redirectAttributes) {
        try {
            Contact contact = new Contact(firstName, lastName, email, phoneNumber, resourceName);
            peopleService.updateContact(authorizedClient, resourceName, contact);
            redirectAttributes.addFlashAttribute("success", "Contact updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating contact: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/contacts";
    }

    @PostMapping("/contacts/delete")
    public String deleteContact(
            @RequestParam String resourceName,
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
            RedirectAttributes redirectAttributes) {
        try {
            peopleService.deleteContact(authorizedClient, resourceName);
            redirectAttributes.addFlashAttribute("success", "Contact deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting contact");
        }
        return "redirect:/contacts";
    }
}

