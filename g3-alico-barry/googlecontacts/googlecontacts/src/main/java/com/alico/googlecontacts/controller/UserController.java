package com.alico.googlecontacts.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // Use @Controller instead of @RestController
public class UserController {

    @GetMapping("/landingpage")
    public String index() {
        return "landingpage"; // Refers to landingpage.html in /templates
    }

    @GetMapping("/user-info")
    public String getUserInfo(@AuthenticationPrincipal OAuth2User oAuth2User, Model model) {
        if (oAuth2User != null) {
            // Extract user details from OAuth2User
            String name = oAuth2User.getAttribute("name");
            String email = oAuth2User.getAttribute("email");
            String picture = oAuth2User.getAttribute("picture");

            // Add attributes to the model
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("picture", picture);
        }
        return "userinfo"; // Refers to userinfo.html in /templates
    }
}