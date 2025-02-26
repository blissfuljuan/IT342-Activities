package com.capuras.oauth2login.controller;

import com.capuras.oauth2login.model.Contact;
import com.capuras.oauth2login.service.GooglePeopleService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class UserController {

    private final GooglePeopleService googlePeopleService;

    public UserController(GooglePeopleService googlePeopleService) {
        this.googlePeopleService = googlePeopleService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/googleuser";
    }

    @GetMapping("/user-info")
    @ResponseBody
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User oAuth2User) {
        return oAuth2User.getAttributes();
    }

    @GetMapping("/googleuser")
    public String getUserInfo(Model model, OAuth2AuthenticationToken authentication) {
        // Basic user info from OAuth
        OAuth2User user = authentication.getPrincipal();
        model.addAttribute("name", user.getAttribute("name"));
        model.addAttribute("email", user.getAttribute("email"));
        model.addAttribute("picture", user.getAttribute("picture"));

        // Get the user's own phone number
        String phoneNumber = googlePeopleService.getPhoneNumber(authentication);
        model.addAttribute("phone", phoneNumber);

        // Get user's contacts with phone numbers
        Map<String, Contact> contactsMap = googlePeopleService.getContactsMap(authentication);
        model.addAttribute("contactsMap", contactsMap);

        return "userinfo";
    }

    @GetMapping("/secured")
    public String secured() {
        return "Secured";
    }
}