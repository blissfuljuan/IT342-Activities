package com.baltazar.GoogleContacts.Controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import com.baltazar.GoogleContacts.service.GoogleContactsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ContactsController {

    private final GoogleContactsService googleContactsService;

    public ContactsController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping("/contacts")
    public String getContacts(Model model, @AuthenticationPrincipal OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            System.out.println("❌ Authentication is NULL. User is not logged in.");
            model.addAttribute("error", "User is not authenticated. Please log in.");
            return "error"; // Ensure you have an error.html page
        }

        String principalName = authentication.getName();
        System.out.println("✅ Authenticated User: " + principalName);

        List<String> contacts = googleContactsService.fetchContacts(principalName);
        model.addAttribute("contacts", contacts);

        return "contacts";
    }
}
