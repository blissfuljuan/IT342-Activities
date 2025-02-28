package com.baltazar.GoogleContacts.Controller;

import org.springframework.ui.Model;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class UserController {

    @GetMapping("/user-info")
    public String userInfo(Model model, OAuth2AuthenticationToken authentication) {
        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();

        // Extract user details
        String fullName = (String) attributes.get("name");
        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");
        String email = (String) attributes.get("email");

        // Add attributes to the model
        model.addAttribute("userName", fullName);  // Keep userName as full name
        model.addAttribute("fullName", fullName);
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("userEmail", email);

        return "user-info";
    }

}



