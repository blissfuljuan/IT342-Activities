package com.cabalida.oauth2login.controller;

import com.cabalida.oauth2login.service.GoogleContactsService;
import com.google.api.services.people.v1.model.Person;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Controller
public class ContactsController {

    private final GoogleContactsService googleContactsService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    public ContactsController(GoogleContactsService googleContactsService, OAuth2AuthorizedClientService authorizedClientService) {
        this.googleContactsService = googleContactsService;
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/contacts")
    public String showContacts(Model model, org.springframework.security.core.Authentication authentication)
            throws GeneralSecurityException, IOException {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient("google", authentication.getName());

        if (authorizedClient == null) {
            throw new RuntimeException("No authorized client found. User may not be authenticated.");
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        List<Person> contacts = googleContactsService.getContacts(accessToken.getTokenValue());

        model.addAttribute("contacts", contacts);
        return "contacts";
    }

    @GetMapping("/new-contact")
    public String showNewContactForm(Model model, org.springframework.security.core.Authentication authentication)
            throws GeneralSecurityException, IOException {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient("google", authentication.getName());

        if (authorizedClient == null) {
            throw new RuntimeException("No authorized client found. User may not be authenticated.");
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        model.addAttribute("accessToken", accessToken.getTokenValue());

        return "new-contact";
    }

    @PostMapping("/new-contact")
    public String addContact(@RequestParam String name, @RequestParam(required = false) String email,
                             @RequestParam(required = false) String phone, @RequestParam String accessToken) throws GeneralSecurityException, IOException {
        googleContactsService.addContact(name, email, phone, accessToken);
        try {
            List<Person> updatedContacts = googleContactsService.getContacts(accessToken);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "redirect:/contacts";
    }

    @GetMapping("/edit-contact")
    public String showEditContactForm(@RequestParam String id, Model model, org.springframework.security.core.Authentication authentication)
            throws GeneralSecurityException, IOException {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient("google", authentication.getName());
        if (authorizedClient == null) throw new RuntimeException("No authorized client found.");

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        Person contact = googleContactsService.getContactById(id, accessToken.getTokenValue());

        model.addAttribute("contact", contact);
        return "edit-contact";
    }

    @PostMapping("/update-contact")
    public String updateContact(@RequestParam String id, @RequestParam String name,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phone,
                                org.springframework.security.core.Authentication authentication)
            throws GeneralSecurityException, IOException {

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient("google", authentication.getName());
        if (authorizedClient == null) throw new RuntimeException("No authorized client found.");

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        googleContactsService.updateContact(id, name, email, phone, accessToken.getTokenValue());

        return "redirect:/contacts";
    }

    @PostMapping("/delete-contact")
    public String deleteContact(@RequestParam String id,
                                org.springframework.security.core.Authentication authentication)
            throws GeneralSecurityException, IOException {

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient("google", authentication.getName());
        if (authorizedClient == null) throw new RuntimeException("No authorized client found.");

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        googleContactsService.deleteContact(id, accessToken.getTokenValue());

        return "redirect:/contacts";
    }
}
