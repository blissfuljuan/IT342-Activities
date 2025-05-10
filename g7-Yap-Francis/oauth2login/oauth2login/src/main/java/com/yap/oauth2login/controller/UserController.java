package com.yap.oauth2login.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Controller
public class UserController {

    @GetMapping("/")
    public String home() {
        return "index"; // Thymeleaf template
    }

    @GetMapping("/contacts")
    public String getContacts(@AuthenticationPrincipal OAuth2User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }

        String accessToken = user.getAttribute("access_token");

        String contactsUrl = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses";
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> contacts = restTemplate.getForObject(contactsUrl + "&access_token=" + accessToken, Map.class);

        model.addAttribute("contacts", contacts);
        return "contacts"; // Thymeleaf template
    }
}