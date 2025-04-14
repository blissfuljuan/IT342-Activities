package com.renato.oauth2login.controller;

import com.renato.oauth2login.service.ContactService;
import com.google.api.services.people.v1.model.Person;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Controller
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/contacts")
    public String getContacts(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        String principalName = principal.getName();

        try {
            List<Person> contacts = contactService.getContacts(principalName);
            model.addAttribute("contacts", contacts);
        } catch (GeneralSecurityException | IOException e) {
            model.addAttribute("error", "Error fetching contacts: " + e.getMessage());
        }

        return "contacts";  
    }

    @PostMapping("/add")
    public String addContact(@AuthenticationPrincipal OAuth2User principal,
                             @RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam(required = false) String phoneNumber) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            contactService.addContact(principal.getName(), firstName, lastName, phoneNumber);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        return "redirect:/contacts";
    }

    @PostMapping("/update")
    public String updateContact(@AuthenticationPrincipal OAuth2User principal,
                                @RequestParam String resourceName,
                                @RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam(required = false) String phoneNumber,
                                RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            if (resourceName == null || resourceName.isBlank() ||
                firstName == null || firstName.isBlank() ||
                lastName == null || lastName.isBlank()) {
                redirectAttributes.addFlashAttribute("error", "Invalid input: Fields cannot be empty.");
                return "redirect:/contacts";
            }

            contactService.updateContact(principal.getName(), resourceName, firstName, lastName, phoneNumber);

            redirectAttributes.addFlashAttribute("success", "Contact updated successfully!");
        } catch (GeneralSecurityException | IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update contact: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
        }

        return "redirect:/contacts";
    }



    @PostMapping("/delete")
    public String deleteContact(@AuthenticationPrincipal OAuth2User principal,
                                @RequestParam String resourceName) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            contactService.deleteContact(principal.getName(), resourceName);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        return "redirect:/contacts";
    }




}
