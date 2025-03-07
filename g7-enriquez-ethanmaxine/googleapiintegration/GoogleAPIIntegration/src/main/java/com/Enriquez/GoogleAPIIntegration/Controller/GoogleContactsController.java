package com.Enriquez.GoogleAPIIntegration.Controller;

import com.Enriquez.GoogleAPIIntegration.Service.GoogleContactsServices;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.Enriquez.GoogleAPIIntegration.DTO.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.GeneralSecurityException;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class GoogleContactsController {
    
    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    private final GoogleContactsServices googleContactsService;

    public GoogleContactsController(GoogleContactsServices googleContactsService) {
        this.googleContactsService= googleContactsService;
    }


    @GetMapping("/dashboard")
    public String getContacts(OAuth2AuthenticationToken authentication, Model model) {
        try {
            List<Person> contacts = googleContactsService.getUserContacts(authentication);
            model.addAttribute("contacts", contacts);
        } catch (GeneralSecurityException | IOException e) {
            model.addAttribute("error", "Failed to fetch contacts: " + e.getMessage());
        }
        return "dashboard";
    }


    @PostMapping("/create-contact")
    public String createContact(OAuth2AuthenticationToken authentication,
                                @RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String email,
                                @RequestParam String phone,
                                Model model) {
        try {
            Person contact = new Person()
                    .setNames(List.of(new Name().setGivenName(firstName).setFamilyName(lastName)))
                    .setEmailAddresses(List.of(new EmailAddress().setValue(email)))
                    .setPhoneNumbers(List.of(new PhoneNumber().setValue(phone)));

            Person createdContact = googleContactsService.createContact(authentication, contact);
            model.addAttribute("message", "Contact created successfully: " + createdContact.getResourceName());
        } catch (GeneralSecurityException | IOException e) {
            model.addAttribute("error", "Failed to create contact: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/contacts/update")
    public String updateContact(OAuth2AuthenticationToken authentication,
                                @RequestParam String resourceName,
                                @RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String email,
                                @RequestParam String phone,
                                Model model) {

        try {
            Person contact = new Person()
                    .setNames(List.of(new Name().setGivenName(firstName).setFamilyName(lastName)))
                    .setEmailAddresses(List.of(new EmailAddress().setValue(email)))
                    .setPhoneNumbers(List.of(new PhoneNumber().setValue(phone)));


            Person updatedContact = googleContactsService.updateContact(authentication, resourceName, contact);
            model.addAttribute("message", "Contact updated successfully: " + updatedContact.getResourceName());
        } catch (GeneralSecurityException | IOException e) {
            model.addAttribute("error", "Failed to update contact: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }
    @PostMapping("/contacts/delete")
    public String deleteContact(OAuth2AuthenticationToken authentication,
                                @RequestParam String resourceName,
                                Model model) {
        try {

            googleContactsService.deleteContact(authentication, resourceName);
            model.addAttribute("message", "Contact deleted successfully: " + resourceName);
        } catch (GeneralSecurityException | IOException e) {
            model.addAttribute("error", "Failed to delete contact: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }
}