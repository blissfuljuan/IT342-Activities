package com.example.gadiane.johnkarl.demolition.controller;

import com.example.gadiane.johnkarl.demolition.model.ContactForm;
import com.example.gadiane.johnkarl.demolition.service.GooglePeopleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling contact view pages
 */
@Controller
public class ContactsViewController {

    private static final Logger logger = LoggerFactory.getLogger(ContactsViewController.class);

    private final GooglePeopleService googlePeopleService;

    @Autowired
    public ContactsViewController(GooglePeopleService googlePeopleService) {
        this.googlePeopleService = googlePeopleService;
    }

    @GetMapping("/contacts")
    public String listContacts(Model model, OAuth2AuthenticationToken authentication) {
        try {
            List<Map<String, Object>> contacts = googlePeopleService.listContacts(authentication);
            model.addAttribute("contacts", contacts);
            return "list";
        } catch (Exception e) {
            logger.error("Error listing contacts: {}", e.getMessage(), e);
            return "error";
        }
    }
    
    @GetMapping("/contacts/new")
    public String newContactForm(Model model) {
        model.addAttribute("contactForm", new ContactForm());
        model.addAttribute("title", "Add New Contact");
        return "form";
    }
    
    @GetMapping("/contacts/{resourceId}")
    public String viewContact(
            OAuth2AuthenticationToken authentication,
            @PathVariable String resourceId,
            Model model) {
        try {
            String decodedResourceId = URLDecoder.decode(resourceId, StandardCharsets.UTF_8.toString());
            
            Map<String, Object> contact = googlePeopleService.getContact(authentication, decodedResourceId);
            model.addAttribute("contact", contact);
            return "view";
        } catch (Exception e) {
            logger.error("Error viewing contact: {}", e.getMessage(), e);
            return "error";
        }
    }
    
    @GetMapping("/contacts/edit/{resourceId}")
    public String editContactForm(
            OAuth2AuthenticationToken authentication,
            @PathVariable String resourceId,
            Model model) {
        try {
            String decodedResourceId = URLDecoder.decode(resourceId, StandardCharsets.UTF_8.toString());
            
            Map<String, Object> contact = googlePeopleService.getContact(authentication, decodedResourceId);
            
            // Convert the contact map to a ContactForm object
            ContactForm contactForm = new ContactForm();
            contactForm.setResourceName(decodedResourceId);
            
            // Extract name, email, and phone from the contact map
            if (contact.containsKey("names") && ((List<?>)contact.get("names")).size() > 0) {
                Map<String, Object> name = (Map<String, Object>)((List<?>)contact.get("names")).get(0);
                contactForm.setName((String)name.getOrDefault("displayName", ""));
            }
            
            if (contact.containsKey("emailAddresses") && ((List<?>)contact.get("emailAddresses")).size() > 0) {
                Map<String, Object> email = (Map<String, Object>)((List<?>)contact.get("emailAddresses")).get(0);
                contactForm.setEmail((String)email.getOrDefault("value", ""));
            }
            
            if (contact.containsKey("phoneNumbers") && ((List<?>)contact.get("phoneNumbers")).size() > 0) {
                Map<String, Object> phone = (Map<String, Object>)((List<?>)contact.get("phoneNumbers")).get(0);
                contactForm.setPhoneNumber((String)phone.getOrDefault("value", ""));
            }
            
            model.addAttribute("contactForm", contactForm);
            model.addAttribute("title", "Edit Contact");
            return "form";
        } catch (Exception e) {
            logger.error("Error editing contact: {}", e.getMessage(), e);
            return "error";
        }
    }
    
    @GetMapping("/contacts/delete/{resourceId}")
    public String deleteContact(
            OAuth2AuthenticationToken authentication,
            @PathVariable String resourceId,
            RedirectAttributes redirectAttributes) {
        try {
            String decodedResourceId = URLDecoder.decode(resourceId, StandardCharsets.UTF_8.toString());
            
            googlePeopleService.deleteContact(authentication, decodedResourceId);
            redirectAttributes.addFlashAttribute("message", "Contact deleted successfully!");
            return "redirect:/contacts";
        } catch (Exception e) {
            logger.error("Error deleting contact: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error deleting contact: " + e.getMessage());
            return "redirect:/contacts";
        }
    }
}
