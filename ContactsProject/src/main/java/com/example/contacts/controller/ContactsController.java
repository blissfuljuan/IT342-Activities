package com.example.contacts.controller;

import com.example.contacts.model.Contact;
import com.example.contacts.service.GoogleContactsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ContactsController {

    private final GoogleContactsService contactsService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null) {
            model.addAttribute("name", principal.getAttribute("name"));
            model.addAttribute("authenticated", true);
        } else {
            model.addAttribute("authenticated", false);
        }
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "logout", required = false) String logout,
                       @AuthenticationPrincipal OAuth2User principal,
                       Model model) {
        if (principal != null) {
            return "redirect:/";
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "login";
    }

    @GetMapping("/contacts")
    public String listContacts(OAuth2AuthenticationToken authentication,
                             @RequestParam(name = "sort", required = false) String sort,
                             Model model) {
        try {
            if (authentication == null) {
                return "redirect:/login";
            }
            List<Contact> contacts = contactsService.getContacts(authentication, sort);
            model.addAttribute("contacts", contacts);
            model.addAttribute("currentSort", sort);
        } catch (Exception e) {
            log.error("Error fetching contacts", e);
            model.addAttribute("errorMessage", "An error occurred while fetching contacts. Please try again.");
        }
        return "contacts";
    }

    @GetMapping("/contacts/new")
    public String newContactForm(Model model) {
        Contact contact = new Contact();
        contact.setEmailAddresses(new ArrayList<>());
        contact.getEmailAddresses().add(new Contact.EmailAddress());
        contact.setPhoneNumbers(new ArrayList<>());
        contact.getPhoneNumbers().add(new Contact.PhoneNumber());
        model.addAttribute("contact", contact);
        model.addAttribute("isNew", true);
        return "contact-form";
    }

    @PostMapping("/contacts")
    public String createContact(OAuth2AuthenticationToken authentication,
                                @ModelAttribute Contact contact,
                                RedirectAttributes redirectAttributes) {
        Contact createdContact = contactsService.createContact(authentication, contact);
        if (createdContact != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Contact created successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create contact!");
        }
        return "redirect:/contacts";
    }

    @GetMapping("/contacts/{resourceName}/edit")
    public String editContactForm(OAuth2AuthenticationToken authentication,
                                 @PathVariable String resourceName,
                                 Model model) {
        // resourceName comes from URL as "people/xxxxx", but we need to encode it properly
        String encodedResourceName = resourceName.replace("_", "/");
        Contact contact = contactsService.getContact(authentication, encodedResourceName);
        
        if (contact == null) {
            return "redirect:/contacts";
        }
        
        // Ensure we have at least one email and phone field for the form
        if (contact.getEmailAddresses() == null || contact.getEmailAddresses().isEmpty()) {
            contact.setEmailAddresses(new ArrayList<>());
            contact.getEmailAddresses().add(new Contact.EmailAddress());
        }
        
        if (contact.getPhoneNumbers() == null || contact.getPhoneNumbers().isEmpty()) {
            contact.setPhoneNumbers(new ArrayList<>());
            contact.getPhoneNumbers().add(new Contact.PhoneNumber());
        }
        
        model.addAttribute("contact", contact);
        model.addAttribute("isNew", false);
        return "contact-form";
    }

    @PostMapping("/contacts/{resourceName}")
    public String updateContact(OAuth2AuthenticationToken authentication,
                               @PathVariable String resourceName,
                               @ModelAttribute Contact contact,
                               RedirectAttributes redirectAttributes) {
        // resourceName comes from URL as "people/xxxxx", but we need to encode it properly
        String encodedResourceName = resourceName.replace("_", "/");
        contact.setResourceName(encodedResourceName);
        
        Contact updatedContact = contactsService.updateContact(authentication, contact);
        if (updatedContact != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Contact updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update contact!");
        }
        return "redirect:/contacts";
    }

    @GetMapping("/contacts/{resourceName}/delete")
    public String deleteContact(OAuth2AuthenticationToken authentication,
                               @PathVariable String resourceName,
                               RedirectAttributes redirectAttributes) {
        // resourceName comes from URL as "people/xxxxx", but we need to encode it properly
        String encodedResourceName = resourceName.replace("_", "/");
        
        boolean deleted = contactsService.deleteContact(authentication, encodedResourceName);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Contact deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete contact!");
        }
        return "redirect:/contacts";
    }
    
    // Helper method to add email address field in the form
    @GetMapping("/contacts/addEmailField")
    public String addEmailField(@RequestParam(required = false) Integer index, Model model) {
        model.addAttribute("emailIndex", index != null ? index + 1 : 0);
        return "fragments/email-field :: emailField";
    }
    
    // Helper method to add phone number field in the form
    @GetMapping("/contacts/addPhoneField")
    public String addPhoneField(@RequestParam(required = false) Integer index, Model model) {
        model.addAttribute("phoneIndex", index != null ? index + 1 : 0);
        return "fragments/phone-field :: phoneField";
    }
}
