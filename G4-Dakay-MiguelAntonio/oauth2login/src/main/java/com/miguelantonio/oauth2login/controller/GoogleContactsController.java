package com.miguelantonio.oauth2login.controller;

import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.miguelantonio.oauth2login.service.GooglePeopleService;
import com.google.api.services.people.v1.model.Person;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class GoogleContactsController {

    private final GooglePeopleService googlePeopleService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public GoogleContactsController(GooglePeopleService googlePeopleService, OAuth2AuthorizedClientService authorizedClientService) {
        this.googlePeopleService = googlePeopleService;
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/contacts")
    @PreAuthorize("isAuthenticated()")
    public String getContacts(@AuthenticationPrincipal OAuth2User user, Model model) throws IOException, GeneralSecurityException {
        // Get OAuth2AuthorizedClient from the authorized client service
        String registrationId = "google"; // Use the registration ID you have for Google (make sure this is the same as in application.properties)
        OAuth2AccessToken accessToken = authorizedClientService
                .loadAuthorizedClient(registrationId, user.getName())
                .getAccessToken();

        // Fetch contacts using the access token
        List<Person> contacts = googlePeopleService.getContacts(accessToken);

        System.out.println("Fetched Contacts: " + contacts);

        // Add contacts to the model
        model.addAttribute("contacts", contacts);
        return "contacts"; // Thymeleaf template will render this view
    }

    @PostMapping("/createContact")
    @PreAuthorize("isAuthenticated()")
    public String createContact(@RequestParam(required = true) String name,
                                @RequestParam(required = false) String email,
                                @AuthenticationPrincipal OAuth2User user) throws IOException, GeneralSecurityException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name parameter is required.");
        }

        // Retrieve access token
        OAuth2AccessToken accessToken = authorizedClientService
                .loadAuthorizedClient("google", user.getName())
                .getAccessToken();

        // Create a new contact object
        Person newContact = new Person();
        newContact.setNames(Collections.singletonList(new Name().setGivenName(name)));

        // Add email if provided
        if (email != null && !email.isEmpty()) {
            newContact.setEmailAddresses(Collections.singletonList(new EmailAddress().setValue(email)));
        }

        // Call the service to create the contact
        googlePeopleService.createContact(accessToken, newContact, email);

        return "redirect:/contacts";
    }

    @DeleteMapping("/deleteContact")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public String deleteContact(@RequestParam String resourceName, @AuthenticationPrincipal OAuth2User user) throws IOException, GeneralSecurityException {
        // Get OAuth2 access token
        OAuth2AccessToken accessToken = authorizedClientService
                .loadAuthorizedClient("google", user.getName())
                .getAccessToken();

        // Call the service to delete the contact
        googlePeopleService.deleteContact(accessToken, resourceName);

        return "Contact deleted successfully";
    }
}
