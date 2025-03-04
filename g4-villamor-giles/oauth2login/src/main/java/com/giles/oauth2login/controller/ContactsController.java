package com.giles.oauth2login.controller;

import com.giles.oauth2login.service.GooglePeopleService;
import com.google.api.services.people.v1.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/contacts")
public class ContactsController {

    private final GooglePeopleService googlePeopleService;

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

    @PostMapping("/update/{resourceName}")
    public String updateContact(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient, @PathVariable String resourceName, @ModelAttribute Person contact) throws IOException {
        googlePeopleService.updateContact(authorizedClient, resourceName, contact);
        return "redirect:/contacts";
    }

    @PostMapping("/delete/{resourceName}")
    public String deleteContact(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient, @PathVariable String resourceName) throws IOException {
        googlePeopleService.deleteContact(authorizedClient, resourceName);
        return "redirect:/contacts";
    }
}