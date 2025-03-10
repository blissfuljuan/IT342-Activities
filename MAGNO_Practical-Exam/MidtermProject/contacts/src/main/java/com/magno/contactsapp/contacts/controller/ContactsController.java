package com.magno.contactsapp.contacts.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import com.magno.contactsapp.contacts.service.GoogleContactsService;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "http://localhost:8080")
public class ContactsController {

    private final GoogleContactsService googleContactsService;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public ContactsController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping
    public ResponseEntity<String> getAllContacts(@AuthenticationPrincipal OAuth2User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body("{\"error\": \"User not authenticated\"}");
            }
            String result = googleContactsService.getContacts(principal.getName());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    @PostMapping
    public ResponseEntity<String> createContact(@AuthenticationPrincipal OAuth2User principal, @RequestBody Map<String, Object> contactData) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body("{\"error\": \"User not authenticated\"}");
            }
            validateContactData(contactData);
            String response = googleContactsService.createContact(principal.getName(), contactData);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"Failed to create contact\"}");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateContact(
            @RequestParam String resourceName,
            @RequestParam String familyName,
            @RequestParam(required = false) List<String> emails,
            @RequestParam(required = false) List<String> phoneNumbers) {
        try {
            if (familyName == null || familyName.trim().isEmpty()) {
                throw new IllegalArgumentException("Name cannot be empty.");
            }
            if (phoneNumbers == null || phoneNumbers.isEmpty() || phoneNumbers.stream().allMatch(String::isBlank)) {
                throw new IllegalArgumentException("At least one phone number is required.");
            }
            if (emails != null) {
                for (String email : emails) {
                    if (!email.isBlank() && !EMAIL_PATTERN.matcher(email).matches()) {
                        throw new IllegalArgumentException("Invalid email format: " + email);
                    }
                }
            }
            if (phoneNumbers != null) {
                for (String phone : phoneNumbers) {
                    if (!phone.isBlank() && !phone.matches("\\+?[0-9]+")) {
                        throw new IllegalArgumentException("Phone number must contain only digits and an optional '+': " + phone);
                    }
                }
            }
            googleContactsService.updateContact(resourceName, familyName, emails, phoneNumbers);
            System.out.println("Contact updated: " + resourceName);
            return ResponseEntity.ok("Contact updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("{\"error\": \"Failed to update contact\"}");
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteContact(@RequestParam String resourceName) {
        try {
            googleContactsService.deleteContact(resourceName);
            System.out.println("Deleted contact: " + resourceName);
            return ResponseEntity.ok("Contact deleted successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("{\"error\": \"Failed to delete contact\"}");
        }
    }

    private void validateContactData(Map<String, Object> contactData) {
        List<Map<String, String>> names = (List<Map<String, String>>) contactData.get("names");
        List<Map<String, String>> phoneNumbers = (List<Map<String, String>>) contactData.get("phoneNumbers");
        List<Map<String, String>> emailAddresses = (List<Map<String, String>>) contactData.get("emailAddresses");

        if (names == null || names.isEmpty() || names.get(0).get("familyName").trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (phoneNumbers == null || phoneNumbers.isEmpty() || phoneNumbers.stream().allMatch(p -> p.get("value").isBlank())) {
            throw new IllegalArgumentException("At least one phone number is required.");
        }
        if (emailAddresses != null) {
            for (Map<String, String> email : emailAddresses) {
                String value = email.get("value");
                if (!value.isBlank() && !EMAIL_PATTERN.matcher(value).matches()) {
                    throw new IllegalArgumentException("Invalid email format: " + value);
                }
            }
        }
        if (phoneNumbers != null) {
            for (Map<String, String> phone : phoneNumbers) {
                String value = phone.get("value");
                if (!value.isBlank() && !value.matches("\\+?[0-9]+")) {
                    throw new IllegalArgumentException("Phone number must contain only digits and an optional '+': " + value);
                }
            }
        }
    }
}