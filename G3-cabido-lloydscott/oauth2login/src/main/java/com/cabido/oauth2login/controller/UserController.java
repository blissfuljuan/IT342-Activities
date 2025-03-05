package com.cabido.oauth2login.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class UserController {

    // Home page (accessible without authentication)
    @GetMapping("/")
    public String index() {
        return "index"; // Renders index.html
    }

    // Endpoint to fetch user info as JSON (requires authentication)
    @GetMapping("/user-info")
    @ResponseBody
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User oAuth2User) {
        return oAuth2User.getAttributes(); // Returns user attributes as JSON
    }

    // Endpoint to display Google user info (requires authentication)
    @GetMapping("/google-user")
    public String getUserInfo(Model model, @AuthenticationPrincipal OAuth2User user) {
        model.addAttribute("name", user.getAttribute("name")); // Add name to model
        model.addAttribute("email", user.getAttribute("email")); // Add email to model
        model.addAttribute("picture", user.getAttribute("picture")); // Add picture to model
        return "userinfo"; // Renders userinfo.html
    }

    // Secured page (requires authentication)
    @GetMapping("/secured")
    public String secured() {
        return "secured"; // Renders secured.html
    }
}