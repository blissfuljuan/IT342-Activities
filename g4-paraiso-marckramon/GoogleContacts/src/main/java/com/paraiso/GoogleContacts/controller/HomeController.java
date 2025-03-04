package com.paraiso.GoogleContacts.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            return "redirect:/user-info";
        }
        return "redirect:/oauth2/authorization/google";
    }

    @GetMapping("/user-info")
    public String userInfo(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null) {
            model.addAttribute("name", principal.getAttribute("name"));
            model.addAttribute("email", principal.getAttribute("email"));
        }
        return "user-info";
    }
} 