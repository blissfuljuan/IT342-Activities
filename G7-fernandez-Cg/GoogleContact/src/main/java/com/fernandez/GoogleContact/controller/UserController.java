package com.fernandez.GoogleContact.controller;

import com.fernandez.GoogleContact.Service.GoogleContactsService;
import com.fernandez.GoogleContact.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @Autowired
    private GoogleContactsService GoogleContactsService;
    @GetMapping("/user-info")
    public String getUser(@AuthenticationPrincipal OAuth2User oAuth2User, Model model) {
        if (oAuth2User != null) {
            UserEntity user = new UserEntity(
                    oAuth2User.getAttribute("sub"),
                    oAuth2User.getAttribute("name"),
                    oAuth2User.getAttribute("given_name"),
                    oAuth2User.getAttribute("family_name"),
                    oAuth2User.getAttribute("email"),
                    oAuth2User.getAttribute("picture")
            );
            model.addAttribute("user", user);
        }
        return "user-info";
    }
}
