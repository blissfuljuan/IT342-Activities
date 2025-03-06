package com.Enriquez.GoogleAPIIntegration.Controller;

import com.Enriquez.GoogleAPIIntegration.Service.GoogleContactsServices;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.Enriquez.GoogleAPIIntegration.DTO.*;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class GoogleContactsController {

    private final GoogleContactsServices googleContactsService;

    public GoogleContactsController(GoogleContactsServices googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping("/dashboard")
    public String getContacts(OAuth2AuthenticationToken authentication, Model model) {
        List<Map<String, Object>> contacts = googleContactsService.getGoogleContacts(authentication);
        model.addAttribute("contacts", contacts);
        return "dashboard"; // Thymeleaf template name
    }


    @PostMapping("/create-contact")
    public String createContact(@ModelAttribute Contact contact, OAuth2AuthenticationToken authentication) {
        googleContactsService.createGoogleContact(authentication, contact);
        return "redirect:/dashboard";
    }

    
}