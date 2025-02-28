package com.Lacaba.GoogleIntegration.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import com.google.api.services.people.v1.model.Person;



import java.util.Collections;
import java.util.Map;

@Controller
@RequestMapping("/contacts")
public class UserController {
    @Autowired
    private GoogleContactsService googleContactsService;

    @GetMapping
    public String listContacts(Model model, OAuth2AuthenticationToken authentication) {
        List<Person> contacts = googleContactsService.getContacts(authentication);
        model.addAttribute("contacts", contacts);
        return "contacts";
    }
}