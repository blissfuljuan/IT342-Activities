package com.giles.oauth2login.controller;

import com.giles.oauth2login.service.GooglePeopleService;
import com.google.api.services.people.v1.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/contacts")
public class ContactsController {

    private final GooglePeopleService googlePeopleService;
    private static final Logger log = LoggerFactory.getLogger(ContactsController.class);

    @Autowired
    public ContactsController(GooglePeopleService googlePeopleService) {
        this.googlePeopleService = googlePeopleService;
    }

    @GetMapping
    public String getContacts(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient, Model model) throws IOException {
        List<Person> contacts = googlePeopleService.getContacts(authorizedClient);

        // Ensure the contacts list is never null
        if (contacts == null) {
            contacts = Collections.emptyList();
        }

        model.addAttribute("contacts", contacts);
        return "contacts";
    }

    @PostMapping
    public String createContact(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient, @ModelAttribute Person contact) throws IOException {
        googlePeopleService.createContact(authorizedClient, contact);
        return "redirect:/contacts";
    }

    @PostMapping("/update")
    public String updateContact(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                              @RequestParam String resourceName,
                              @ModelAttribute Person contact) {
        try {
            log.info("Received update request for contact: " + resourceName);
            googlePeopleService.updateContact(authorizedClient, resourceName, contact);
            return "redirect:/contacts";
        } catch (IOException e) {
            log.error("Failed to update contact: " + resourceName, e);
            return "redirect:/contacts?error=update_failed";
        }
    }

    @PostMapping("/delete")
    public String deleteContact(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                              @RequestParam String resourceName) {
        try {
            log.info("Received delete request for contact: " + resourceName);
            googlePeopleService.deleteContact(authorizedClient, resourceName);
            return "redirect:/contacts";
        } catch (IOException e) {
            log.error("Failed to delete contact: " + resourceName, e);
            return "redirect:/contacts?error=delete_failed";
        }
    }
}