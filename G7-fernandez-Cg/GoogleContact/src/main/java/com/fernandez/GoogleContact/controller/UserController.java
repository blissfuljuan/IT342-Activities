package com.fernandez.GoogleContact.controller;

import com.fernandez.GoogleContact.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    @GetMapping("/")
    public String index() {
        return "index";  // Returns index.html from the templates folder
    }

    @GetMapping("/user-info")
    public String getUser(@AuthenticationPrincipal OAuth2User oAuth2User, Model model) {
        if (oAuth2User != null) {
            User user = new User(
                    oAuth2User.getAttribute("sub"),
                    oAuth2User.getAttribute("name"),
                    oAuth2User.getAttribute("given_name"),
                    oAuth2User.getAttribute("family_name"),
                    oAuth2User.getAttribute("email"),
                    oAuth2User.getAttribute("picture")
            );
            model.addAttribute("user", user);
        }
        return "user-info";  // Loads user-info.html from the templates folder
    }

    @GetMapping("/contacts")
    public String getContacts(@AuthenticationPrincipal OAuth2User oAuth2User, Model model) {
        if (oAuth2User != null) {
            List<Map<String, Object>> contacts = fetchContacts(oAuth2User.getAttribute("email"));
            model.addAttribute("contacts", contacts);
        }
        return "contacts";  // Loads contacts.html from the templates folder
    }

    private List<Map<String, Object>> fetchContacts(String email) {
        // Dummy contacts for now (Replace with Google Contacts API logic)
        return List.of(
                Map.of("name", "John Doe", "email", "john.doe@example.com"),
                Map.of("name", "Jane Smith", "email", "jane.smith@example.com")
        );
    }
}
