package com.revilleza.oauth2login.controller;

import com.revilleza.oauth2login.model.Contact;
import com.revilleza.oauth2login.service.GoogleContactsService;
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

    public ContactsController(GoogleContactsService contactsService) {
        this.contactsService = contactsService;
    }

    @GetMapping
    public String getContacts(@AuthenticationPrincipal OAuth2User principal, Model model) {
        List<Contact> contacts = contactsService.getContacts(principal.getName());
        model.addAttribute("contacts", contacts);
        return "contacts";
    }

    @PostMapping
    public String addContacts(@AuthenticationPrincipal OAuth2User principal, Model model) {
        List<Contact> contacts = contactsService.getContacts(principal.getName());
        model.addAttribute("contacts", contacts);
        return "contacts";
    }
}
