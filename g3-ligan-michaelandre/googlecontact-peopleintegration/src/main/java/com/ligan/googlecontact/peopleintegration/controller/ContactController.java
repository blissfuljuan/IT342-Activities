package com.ligan.googlecontact.peopleintegration.controller;

import com.ligan.googlecontact.peopleintegration.service.ContactService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
@Slf4j
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<?> getAllContacts() {
        try {
            List<Map<String, Object>> contacts = contactService.getAllContacts();
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch contacts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getContact(@RequestParam String resourceName) {
        try {
            Map<String, Object> contact = contactService.getContact(resourceName);
            return ResponseEntity.ok(contact);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    /*@GetMapping("/test-api")
    public ResponseEntity<?> testApiConnection() {
        try {
            Map<String, Object> result = contactService.testApiConnection();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error testing API connection", e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }*/

    @PostMapping
    public ResponseEntity<?> createContact(@RequestBody Map<String, Object> contact) {
        try {
            Map<String, Object> newContact = contactService.createContact(contact);
            return ResponseEntity.status(HttpStatus.CREATED).body(newContact);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to create contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateContact(
            @RequestParam String resourceName,
            @RequestBody Map<String, Object> contact) {
        try {
            // Clean up resourceName if needed
            if (resourceName.startsWith("people%2F")) {
                resourceName = resourceName.replace("people%2F", "people/");
            }

            Map<String, Object> updatedContact = contactService.updateContact(resourceName, contact);
            return ResponseEntity.ok(updatedContact);
        } catch (Exception e) {
            log.error("Failed to update contact", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update contact: " + e.getMessage());
            error.put("details", e.toString());
            if (e.getCause() != null) {
                error.put("cause", e.getCause().toString());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteContact(@RequestParam String resourceName) {
        try {
            contactService.deleteContact(resourceName);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to delete contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}