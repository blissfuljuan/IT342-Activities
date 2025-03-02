package com.canal.GoogleContact.controller;

import com.canal.GoogleContact.service.GoogleContactsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class GoogleContactsController {

    private final GoogleContactsService googleContactsService;

    public GoogleContactsController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    // Fetch and display all contacts
    @GetMapping("/contacts")
    public String getContacts(OAuth2AuthenticationToken authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            List<Map<String, Object>> contacts = googleContactsService.getContacts(authentication);
            model.addAttribute("contacts", contacts);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch contacts: " + e.getMessage());
        }

        return "dashboard";
    }

    // Display user profile
    @GetMapping("/profile")
    public String getUserProfile(OAuth2AuthenticationToken authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        // Fetch user profile details from OAuth2 authentication
        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
        model.addAttribute("picture", attributes.get("picture"));
        model.addAttribute("displayName", attributes.get("name"));
        model.addAttribute("firstName", attributes.get("given_name"));
        model.addAttribute("lastName", attributes.get("family_name"));
        model.addAttribute("email", attributes.get("email"));

        return "profile"; // Ensure this matches the name of your profile.html file
    }

    // Show Add Contact Form
    @GetMapping("/contacts/add")
    public String showAddContactForm() {
        return "add_contact";
    }

    // Handle Adding Contact
    @PostMapping("/contacts/add")
    public String addContact(@RequestParam String name,
                             @RequestParam(required = false) String email,
                             @RequestParam(required = false) String phone,
                             OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            googleContactsService.addContact(authentication, name, email, phone);
        } catch (Exception e) {
            return "redirect:/contacts?error=" + e.getMessage();
        }

        return "redirect:/contacts";
    }

    // Handle Deleting Contact
    @PostMapping("/contacts/delete/{resourceName}")
    public String deleteContact(@PathVariable String resourceName, OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            googleContactsService.deleteContact(authentication, resourceName);
        } catch (Exception e) {
            return "redirect:/contacts?error=" + e.getMessage();
        }

        return "redirect:/contacts";
    }

    // Show Edit Contact Form
    @GetMapping("/contacts/edit/{resourceName}")
    public String showEditContactForm(@PathVariable String resourceName, OAuth2AuthenticationToken authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            Map<String, Object> contact = googleContactsService.getContactByResourceName(authentication, resourceName);
            model.addAttribute("contact", contact);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch contact: " + e.getMessage());
        }

        return "edit_contact";
    }

    // Handle Updating Contact
    @PostMapping("/contacts/update/{resourceName}")
    public String updateContact(@PathVariable String resourceName,
                                @RequestParam String name,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phone,
                                OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            googleContactsService.updateContact(authentication, resourceName, name, email, phone);
        } catch (Exception e) {
            return "redirect:/contacts?error=" + e.getMessage();
        }

        return "redirect:/contacts";
    }
}