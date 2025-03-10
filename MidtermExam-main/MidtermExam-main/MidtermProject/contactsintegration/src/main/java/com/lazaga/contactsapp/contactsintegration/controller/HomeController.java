package com.lazaga.contactsapp.contactsintegration.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "home";
    }


    @GetMapping("/contacts")
    public String contacts(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "redirect:/";
        }
        model.addAttribute("userName", principal.getAttribute("name"));
        return "contacts";  // Loads contacts.html
    }
}