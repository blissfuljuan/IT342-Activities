package com.Largo.GoogleContacts.controller;

import com.Largo.GoogleContacts.model.Contacts;
import com.Largo.GoogleContacts.service.GoogleContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Collections;
import java.util.Map;

@Controller
public class UserController {
    @Autowired
    private GoogleContactsService googleContactsService;
    @GetMapping("")
    public String index(){
        return "<h1>Welcome, This is the landing page</h1>";
    }

    @GetMapping("/user-info")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User oAuth2User){
        if(oAuth2User!=null){
            return oAuth2User.getAttributes();
        }else{
            return Collections.emptyMap();
        }
    }

    /*
    @GetMapping("/contacts")
    public String getContacts(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null) {
            List<Contacts> contacts = googleContactsService.getContacts(principal);
            model.addAttribute("contacts", contacts);
        }
        return "contact";
    }*/
    @GetMapping("/add-contact")
    public String addContactForm() {
        return "addContact";
    }
/*
    @PostMapping("/add-contact")
    public String addContact(@RequestParam String name, @RequestParam String email,
                             @AuthenticationPrincipal OAuth2User principal) {
        googleContactsService.getContacts(principal, name, email);
        return "redirect:/contacts";
    }*/
}
