package edu.cit.CIT_UCommunityForums.controller;

import edu.cit.CIT_UCommunityForums.model.Contact;
import edu.cit.CIT_UCommunityForums.service.GoogleContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class ContactsController {

    @Autowired
    private GoogleContactsService googleContactsService;

    @GetMapping("/contacts")
    public String viewContacts(Model model, OAuth2AuthenticationToken authentication) {
        try {
            List<Contact> contacts = googleContactsService.getContacts(authentication);
            model.addAttribute("contacts", contacts);
            return "contacts"; // Thymeleaf template name
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error retrieving contacts: " + e.getMessage());
            return "error"; // A simple error page
        }
    }
}
