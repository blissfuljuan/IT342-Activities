package com.jamisola.contactspeopleApi.Controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public UserController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/")
    public String index() {
        return "index"; // Create index.html
    }

    @GetMapping("/user-profile")
    public String getUserProfile(Model model, @AuthenticationPrincipal OAuth2User user) {
        if (user != null) {
            model.addAllAttributes(user.getAttributes()); // Add all attributes to the model
        }
        return "user-profile"; // Ensure user-profile.html exists
    }
}
