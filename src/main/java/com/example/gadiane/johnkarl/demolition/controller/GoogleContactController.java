package com.example.gadiane.johnkarl.demolition.controller;

import com.example.gadiane.johnkarl.demolition.model.ContactForm;
import com.example.gadiane.johnkarl.demolition.service.GooglePeopleService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
@RequestMapping("/api/contacts")
public class GoogleContactController {

    private static final Logger logger = LoggerFactory.getLogger(GoogleContactController.class);
    
    private final GooglePeopleService googlePeopleService;

    @Autowired
    public GoogleContactController(GooglePeopleService googlePeopleService) {
        this.googlePeopleService = googlePeopleService;
    }

    @GetMapping("/view")
    public String viewContacts(Model model, OAuth2AuthenticationToken authentication) {
        // This would be implemented to show all contacts
        // For now, just returning the view name
        return "contacts";
    }

    @GetMapping("/contacts")
    public String listContacts(Model model, OAuth2AuthenticationToken authentication) {
        try {
            // In a real implementation, you would fetch contacts here
            // and add them to the model
            return "list";
        } catch (Exception e) {
            logger.error("Error listing contacts: {}", e.getMessage(), e);
            return "error";
        }
    }

    @GetMapping("/new")
    public String newContactForm(Model model) {
        model.addAttribute("contactForm", new ContactForm());
        model.addAttribute("title", "Add New Contact");
        return "form";
    }

    @PostMapping("/new")
    public String createNewContact(
            OAuth2AuthenticationToken authentication,
            @ModelAttribute ContactForm contactForm,
            RedirectAttributes redirectAttributes) {
        try {
            Map<String, Object> newContact = googlePeopleService.createContact(authentication, contactForm);
            redirectAttributes.addFlashAttribute("message", "Contact created successfully!");
            return "redirect:/contacts";
        } catch (Exception e) {
            logger.error("Error creating contact: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error creating contact: " + e.getMessage());
            return "redirect:/contacts";
        }
    }

    @GetMapping("/{resourceId}")
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

    @GetMapping("/edit/{resourceId}")
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
            // This would need to be implemented based on the structure of the contact map
            
            model.addAttribute("contactForm", contactForm);
            model.addAttribute("title", "Edit Contact");
            return "form";
        } catch (Exception e) {
            logger.error("Error editing contact: {}", e.getMessage(), e);
            return "error";
        }
    }

    @GetMapping("/delete/{resourceId}")
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

    @GetMapping("/data/{resourceId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getContact(
            OAuth2AuthenticationToken authentication,
            @PathVariable String resourceId) {
        try {
            String decodedResourceId = URLDecoder.decode(resourceId, StandardCharsets.UTF_8.toString());
            
            Map<String, Object> contact = googlePeopleService.getContact(authentication, decodedResourceId);
            return ResponseEntity.ok(contact);
        } catch (Exception e) {
            logger.error("Error getting contact: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/edit/{resourceId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateContact(
            OAuth2AuthenticationToken authentication,
            @PathVariable String resourceId,
            @RequestBody ContactForm contactForm) {
        try {
            logger.info("Attempting to update contact with resource ID: {}", resourceId);
            logger.info("Contact form data: firstName={}, lastName={}, email={}, phone={}", 
                      contactForm.getFirstName(), contactForm.getLastName(), 
                      contactForm.getEmail(), contactForm.getPhoneNumber());
            
            // URL decode the resource ID
            String decodedResourceId = URLDecoder.decode(resourceId, StandardCharsets.UTF_8.toString());
            
            Map<String, Object> updatedContact = googlePeopleService.updateContact(authentication, decodedResourceId, contactForm);
            
            logger.info("Contact updated successfully: {}", updatedContact);
            
            return ResponseEntity.ok(updatedContact);
        } catch (Exception e) {
            logger.error("Error updating contact: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createContact(
            OAuth2AuthenticationToken authentication,
            @RequestBody ContactForm contactForm) {
        try {
            Map<String, Object> newContact = googlePeopleService.createContact(authentication, contactForm);
            return ResponseEntity.ok(newContact);
        } catch (Exception e) {
            logger.error("Error creating contact: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{resourceId}")
    @ResponseBody
    public ResponseEntity<Void> deleteContact(
            OAuth2AuthenticationToken authentication,
            @PathVariable String resourceId) {
        try {
            String decodedResourceId = URLDecoder.decode(resourceId, StandardCharsets.UTF_8.toString());
            
            googlePeopleService.deleteContact(authentication, decodedResourceId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting contact: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
