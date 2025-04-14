package com.saludsod.contactsapp.contactsintegration.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(OAuth2AuthenticationToken token) {
        if (token != null) {
            System.out.println("User Info: " + token.getPrincipal().getAttributes());
        }
        return "home";
    }
}

