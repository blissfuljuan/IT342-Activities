package com.miparanummidterm.Midterm.Miparanum.controller;

import com.miparanummidterm.Midterm.Miparanum.service.ContactsService;
import com.google.api.services.people.v1.model.Person;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import java.util.List;

@Controller
public class ContactsController {
    private final ContactsService contactsService;

    public ContactsController(ContactsService contactsService) {
        this.contactsService = contactsService;
    }

    @GetMapping({"/contacts"})
    public String getContacts(Authentication authentication, Model model) {
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User user = oauthToken.getPrincipal();
            String email = (String)user.getAttributes().get("email");
            String name = (String)user.getAttributes().get("name");
            String picture = (String)user.getAttributes().get("picture");
            model.addAttribute("email", email);
            model.addAttribute("name", name);
            model.addAttribute("picture", picture);

            try {
                List<Person> contacts = this.contactsService.getGoogleContacts(authentication);
                model.addAttribute("contacts", contacts);
            } catch (Exception var9) {
                Exception e = var9;
                model.addAttribute("error", "Failed to fetch contacts: " + e.getMessage());
            }
        }

        return "contacts";
    }
}