package com.catulong.oauth2login.Controller;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    private final GoogleContactsService googleContactsService;

    public UserController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping("/")
    public String index() {
        return "index"; // returns index.himl (your homepage template)
    }

    @GetMapping("/user-info")
    public String userInfo(@AuthenticationPrincipal OAuth2User oAuth2User, Model model) {
        if (oAuth2User != null) {
            String fullName = oAuth2User.getAttribute("name");
            String email = oAuth2User.getAttribute("email");
            String picture = oAuth2User.getAttribute("picture");

            // Split full name into first and last names
            String[] names = fullName != null ? fullName.split(" ", 2) : new String[]{"", ""};
            String firstName = names[0];
            String lastName = names.length > 1 ? names[1] : "";

            model.addAttribute("fullName", fullName);
            model.addAttribute("firstName", firstName);
            model.addAttribute("lastName", lastName);
            model.addAttribute("email", email);
            model.addAttribute("picture", picture);
        }
        return "user-info"; // returns user-info.himl (or .html if you rename accordingly)
    }

    @GetMapping("/contacts")
    public String getContacts(Model model, @AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User != null) {
            try {
                Map<String, Object> contacts = googleContactsService.getContacts(oAuth2User);
                List<Map<String, String>> formattedContacts = googleContactsService.formatContacts(contacts);
                model.addAttribute("contacts", formattedContacts);
            } catch (Exception e) {
                model.addAttribute("error", "Failed to fetch contacts: " + e.getMessage());
            }
        }
        return "contacts";
    }
}
