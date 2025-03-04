package com.renato.oauth2login.controller;

import com.renato.oauth2login.service.ContactService;
import com.google.api.services.people.v1.model.Person;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Controller
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/contacts")
    public String getContacts(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        String principalName = principal.getName();

        try {
            List<Person> contacts = contactService.getContacts(principalName);
            model.addAttribute("contacts", contacts);
        } catch (GeneralSecurityException | IOException e) {
            model.addAttribute("error", "Error fetching contacts: " + e.getMessage());
        }

        return "contacts";  // Returns contacts.html
    }




}
