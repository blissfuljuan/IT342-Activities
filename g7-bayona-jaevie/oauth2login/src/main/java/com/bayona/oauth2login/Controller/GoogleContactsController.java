package com.bayona.oauth2login.Controller;

import com.bayona.oauth2login.service.GooglePeopleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Logger;

@Controller
public class GoogleContactsController {

    private static final Logger logger = Logger.getLogger(GoogleContactsController.class.getName());
    private final GooglePeopleService googlePeopleService;

    public GoogleContactsController(GooglePeopleService googlePeopleService) {
        this.googlePeopleService = googlePeopleService;
    }

    @GetMapping("/contacts")
    public String getContacts(OAuth2AuthenticationToken authentication, Model model) {
        try {
            List<Person> contacts = googlePeopleService.getUserContacts(authentication);
            model.addAttribute("contacts", contacts);
        } catch (GeneralSecurityException | IOException e) {
            model.addAttribute("error", "Failed to fetch contacts: " + e.getMessage());
        }
        return "contacts";
    }

    @PostMapping("/contacts")
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

            logger.info("Creating contact with name: " + firstName + " " + lastName);
            Person createdContact = googlePeopleService.createContact(authentication, contact);
            logger.info("Created contact with resource name: " + createdContact.getResourceName());
            model.addAttribute("message", "Contact created successfully: " + createdContact.getResourceName());
        } catch (GeneralSecurityException | IOException e) {
            model.addAttribute("error", "Failed to create contact: " + e.getMessage());
        }
        return "redirect:/contacts";
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

            logger.info("Updating contact with resource name: " + resourceName);
            Person updatedContact = googlePeopleService.updateContact(authentication, resourceName, contact);
            logger.info("Updated contact with resource name: " + updatedContact.getResourceName());
            model.addAttribute("message", "Contact updated successfully: " + updatedContact.getResourceName());
        } catch (GeneralSecurityException | IOException e) {
            model.addAttribute("error", "Failed to update contact: " + e.getMessage());
        }
        return "redirect:/contacts";
    }

    @PostMapping("/contacts/delete")
    public String deleteContact(OAuth2AuthenticationToken authentication,
                                @RequestParam String resourceName,
                                Model model) {
        try {
            logger.info("Deleting contact with resource name: " + resourceName);
            googlePeopleService.deleteContact(authentication, resourceName);
            logger.info("Deleted contact with resource name: " + resourceName);
            model.addAttribute("message", "Contact deleted successfully: " + resourceName);
        } catch (GeneralSecurityException | IOException e) {
            model.addAttribute("error", "Failed to delete contact: " + e.getMessage());
        }
        return "redirect:/contacts";
    }
    // @GetMapping("/contacts/edit/{resourceName}")
    // public String editContactForm(OAuth2AuthenticationToken authentication,
    //                             @PathVariable String resourceName,
    //                             Model model) {
    //     try {
    //         // Add detailed logging
    //         System.out.println("Editing contact: " + resourceName);
            
    //         // Get contact details
    //         Person contact = googlePeopleService.getContact(authentication, resourceName);
            
    //         // Add to model
    //         model.addAttribute("contact", contact);
            
    //         // Return template name
    //         return "contactsUpdate";
    //     } catch (Exception e) {
    //         // Log the full exception
    //         e.printStackTrace();
    //         model.addAttribute("error", "Failed to load contact: " + e.getMessage());
    //         return "redirect:/contacts";
    //     }
    // }
    // Add this new method - don't remove your existing one
    @GetMapping("/edit")
public String simpleEditTest(@RequestParam String resourceName, 
                           Model model,
                           OAuth2AuthenticationToken authentication) {
    try {
        System.out.println("Simple edit test for: " + resourceName);
        Person contact = googlePeopleService.getContact(authentication, resourceName);
        model.addAttribute("contact", contact);
        return "contactsUpdate";
    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("error", "Error: " + e.getMessage());
        return "redirect:/contacts";
    }
}
    @GetMapping("/contacts/create")
    public String showCreateContactForm() {
        return "contactsCreate";
    }
}