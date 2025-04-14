package com.revilleza.google.contacts.controller;

import com.revilleza.google.contacts.model.Contact;
import com.revilleza.google.contacts.service.GoogleContactsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationToken;
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

    public ContactsController(GoogleContactsService contactsService) {
        this.contactsService = contactsService;
    }

    @GetMapping
    public String getContacts(OAuth2AuthenticationToken token, Model model) {
        List<Contact> contacts = contactsService.getContacts(token);
        model.addAttribute("contacts", contacts);
        return "contacts";
    }

    @PostMapping
    public String addContacts(OAuth2AuthenticationToken token, Model model) {
        List<Contact> contacts = contactsService.getContacts(token);
        model.addAttribute("contacts", contacts);
        return "contacts";
    }
}
