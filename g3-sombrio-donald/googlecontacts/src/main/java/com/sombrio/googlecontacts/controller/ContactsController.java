package com.sombrio.googlecontacts.controller;

import com.sombrio.googlecontacts.model.Contact;
import com.sombrio.googlecontacts.service.GoogleContactsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static org.apache.tomcat.util.net.openssl.OpenSSLStatus.getName;

@Controller
@RequestMapping("/contacts")
public class ContactsController {

    private final GoogleContactsService contactsService;

    public ContactsController(GoogleContactsService contactsService){
        this.contactsService = contactsService;
    }

    @GetMapping
    public String getContacts(@AuthenticationPrincipal OAuth2User principal, Model model ){
        List<Contact> contacts = contactsService.getContact(principal.getName());
        model.addAttribute("contacts", contacts);
        return "contacts";
    }
}
