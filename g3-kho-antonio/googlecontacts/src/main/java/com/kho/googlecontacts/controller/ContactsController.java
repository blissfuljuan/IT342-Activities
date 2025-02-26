package com.kho.googlecontacts.controller;

import com.kho.googlecontacts.model.Contact;

import com.kho.googlecontacts.service.GoogleContactsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/contacts")
public class ContactsController {
    private final GoogleContactsService contactsService;

    // Constructor Injection
    public ContactsController(GoogleContactsService contactsService) {
        this.contactsService = contactsService;
    }

    @GetMapping
    public String getContacts(@AuthenticationPrincipal OAuth2User principal, Model model) {
        // Retrieve contacts for the authenticated user
        List<Contact> contacts = contactsService.getContacts(principal);
        model.addAttribute("contacts", contacts);
        return "contacts";  // Return to the 'contacts.html' template
    }

    // Modify Contact
    @PostMapping("/modify")
    public String modifyContact(@AuthenticationPrincipal OAuth2User principal, Contact contact) {
        contactsService.modifyContact(principal, contact);
        return "redirect:/contacts";  // After modification, go back to the contacts page
    }

    // Add Contact
    @PostMapping("/add")
    public String addContact(@AuthenticationPrincipal OAuth2User principal, Contact contact) {
        contactsService.addContact(principal, contact);
        return "redirect:/contacts";
    }

    // Remove Contact
    @PostMapping("/remove")
    public String removeContact(@AuthenticationPrincipal OAuth2User principal, Long contactId) {
        contactsService.removeContact(principal, contactId);
        return "redirect:/contacts";
    }
}
