package com.pepito.midterm.Controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.google.api.services.people.v1.model.Person;
import com.pepito.midterm.Service.GoogleContactService;

@Controller
public class GoogleContactController {

    @Autowired
    private GoogleContactService gserv;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("message", "Welcome, this is the landing page.");
        return "index";
    }

    @GetMapping("/user-info")
    public String googleHome(Model model, @AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User != null) {
            String fullName = oAuth2User.getAttribute("name");
            String email = oAuth2User.getAttribute("email");
            String picture = oAuth2User.getAttribute("picture");

            String[] names = fullName != null ? fullName.split(" ", 2) : new String[]{"", ""};
            String firstName = names[0];
            String lastName = names.length > 1 ? names[1] : "";

            model.addAttribute("fullName", fullName);
            model.addAttribute("firstName", firstName);
            model.addAttribute("lastName", lastName);
            model.addAttribute("email", email);
            model.addAttribute("picture", picture);
        }
        return "googleHome";
    }

    @GetMapping("/contacts")
    public String getContacts(Model model, @AuthenticationPrincipal OAuth2User oAuth2User) throws IOException, GeneralSecurityException {
        List<java.util.Map<String, Object>> contacts = gserv.listContacts(oAuth2User);
        model.addAttribute("contacts", contacts);
        return "contacts";
    }

    @PostMapping("/contacts/add")
    public String addContact(@AuthenticationPrincipal OAuth2User oAuth2User,
                             @RequestParam String givenName,
                             @RequestParam String familyName,
                             @RequestParam List<String> emails,
                             @RequestParam List<String> phones) throws IOException, GeneralSecurityException {
        gserv.addContact(oAuth2User, givenName, familyName, emails, phones);
        return "redirect:/contacts";
    }

    @PostMapping("/contacts/update")
    public String updateContact(@AuthenticationPrincipal OAuth2User oAuth2User,
                                @RequestParam String resourceName,
                                @RequestParam String givenName,
                                @RequestParam String familyName,
                                @RequestParam List<String> emails,
                                @RequestParam List<String> phones) throws IOException, GeneralSecurityException {

        System.out.println("Updating contact:");
        System.out.println("Resource Name: " + resourceName);
        System.out.println("Given Name: " + givenName);
        System.out.println("Family Name: " + familyName);
        System.out.println("Emails: " + emails);
        System.out.println("Phones: " + phones);

        gserv.updateContact(oAuth2User, resourceName, givenName, familyName, emails, phones);
        return "redirect:/contacts";
    }


    @PostMapping("/contacts/delete")
    public String deleteContact(@AuthenticationPrincipal OAuth2User oAuth2User,
                                @RequestParam String resourceName) throws IOException, GeneralSecurityException {
        gserv.deleteContact(oAuth2User, resourceName);
        return "redirect:/contacts";
    }
} 
