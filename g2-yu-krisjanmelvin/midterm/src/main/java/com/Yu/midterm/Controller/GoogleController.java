package com.Yu.midterm.Controller;

import com.Yu.midterm.Model.Contact;
import com.Yu.midterm.Service.GooglePeopleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Controller
public class GoogleController {
    private static final Logger logger = LoggerFactory.getLogger(GoogleController.class);
    private final GooglePeopleService contactsService;

    public GoogleController(GooglePeopleService contactsService) {
        this.contactsService = contactsService;
    }

    @GetMapping("/contacts")
    public String getContacts(Model model, @AuthenticationPrincipal OAuth2User principal) throws GeneralSecurityException, IOException {
        String userId = principal.getName();
        model.addAttribute("contacts", contactsService.getContacts(userId));
        model.addAttribute("newContact", new Contact());
        return "contacts";
    }

    @PostMapping("/contacts/add")
    public String addContact(@ModelAttribute Contact newContact, @AuthenticationPrincipal OAuth2User principal) throws GeneralSecurityException, IOException {
        String userId = principal.getName();
        contactsService.addContact(userId, newContact);
        return "redirect:/contacts";
    }

    @GetMapping("/contacts/edit/{resourceName}")
    public String showEditForm(@PathVariable String resourceName, Model model, @AuthenticationPrincipal OAuth2User principal) throws GeneralSecurityException, IOException {
        String userId = principal.getName();
        String formattedResourceName = resourceName.replace("_", "/");
        Contact contact = contactsService.getContact(userId, formattedResourceName);
        if (contact == null || contact.getResourceName() == null) {
            logger.warn("Contact not found for resourceName: {}", formattedResourceName);
            contact = new Contact();
            contact.setResourceName(formattedResourceName);
        }
        model.addAttribute("contact", contact);
        return "edit-contact";
    }

    @PostMapping("/contacts/update/{resourceName}")
    public String updateContact(@PathVariable String resourceName, @ModelAttribute Contact contact, @AuthenticationPrincipal OAuth2User principal) throws GeneralSecurityException, IOException {
        String userId = principal.getName();
        String formattedResourceName = resourceName.replace("_", "/");
        if (contact.getResourceName() == null) {
            contact.setResourceName(formattedResourceName);
        }
        contactsService.updateContact(userId, formattedResourceName, contact);
        return "redirect:/contacts";
    }

    @GetMapping("/contacts/delete/{resourceName}")
    public String deleteContact(@PathVariable String resourceName, @AuthenticationPrincipal OAuth2User principal) throws GeneralSecurityException, IOException {
        String userId = principal.getName();
        contactsService.deleteContact(userId, resourceName.replace("_", "/"));
        return "redirect:/contacts";
    }
}