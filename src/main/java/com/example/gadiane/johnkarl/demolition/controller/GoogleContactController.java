package com.example.gadiane.johnkarl.demolition.controller;

import com.example.gadiane.johnkarl.demolition.model.ContactForm;
import com.example.gadiane.johnkarl.demolition.service.GooglePeopleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{resourceId}")
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
