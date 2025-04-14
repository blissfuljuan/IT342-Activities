package com.miparanummidterm.Midterm.Miparanum.controller;

import com.miparanummidterm.Midterm.Miparanum.service.ContactsService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/contacts"})

public class ContactsRestController {
    private final ContactsService contactsService;

    public ContactsRestController(ContactsService contactsService) {
        this.contactsService = contactsService;
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<Person>> getAllContacts(Authentication authentication) {
        try {
            List<Person> contacts = this.contactsService.getGoogleContacts(authentication);
            return ResponseEntity.ok(contacts);
        } catch (Exception var3) {
            return ResponseEntity.internalServerError().body((List<Person>) null);
        }
    }

    @PostMapping
    public ResponseEntity<?> addContact(Authentication authentication, @RequestBody Person newContact) {
        try {
            Person createdContact = this.contactsService.addGoogleContact(authentication, newContact);
            return ResponseEntity.ok(createdContact);
        } catch (Exception var4) {
            Exception e = var4;
            return ResponseEntity.internalServerError().body("Error adding contact: " + e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> updateContact(Authentication authentication, @RequestParam String resourceName, @RequestBody Map<String, Object> updatedContactData) {
        try {
            System.out.println("Update Request Received for: " + resourceName);
            System.out.println("Received Data: " + String.valueOf(updatedContactData));
            String givenName = "";
            if (updatedContactData.containsKey("names")) {
                List<Map<String, Object>> namesList = (List)updatedContactData.get("names");
                if (namesList != null && !namesList.isEmpty()) {
                    givenName = (String)((Map)namesList.get(0)).get("givenName");
                }
            }

            String email = "";
            if (updatedContactData.containsKey("emailAddresses")) {
                List<Map<String, Object>> emailList = (List)updatedContactData.get("emailAddresses");
                if (emailList != null && !emailList.isEmpty()) {
                    email = (String)((Map)emailList.get(0)).get("value");
                }
            }

            String phoneNumber = "";
            if (updatedContactData.containsKey("phoneNumbers")) {
                List<Map<String, Object>> phoneList = (List)updatedContactData.get("phoneNumbers");
                if (phoneList != null && !phoneList.isEmpty()) {
                    phoneNumber = (String)((Map)phoneList.get(0)).get("value");
                }
            }

            Person updatedContact = (new Person()).setNames(List.of((new Name()).setGivenName(givenName))).setEmailAddresses(Collections.singletonList((new EmailAddress()).setValue(email))).setPhoneNumbers(Collections.singletonList((new PhoneNumber()).setValue(phoneNumber)));
            Person updated = this.contactsService.updateGoogleContact(authentication, resourceName, updatedContact);
            return ResponseEntity.ok(updated);
        } catch (Exception var9) {
            Exception e = var9;
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating contact: " + e.getMessage());
        }
    }

    @DeleteMapping({"/**"})
    public ResponseEntity<?> deleteContact(Authentication authentication, HttpServletRequest request) {
        try {
            String fullPath = request.getRequestURI();
            String resourceName = fullPath.substring("/api/contacts/".length());
            System.out.println("Attempting to delete resource: " + resourceName);
            this.contactsService.deleteGoogleContact(authentication, resourceName);
            return ResponseEntity.ok("Contact deleted successfully.");
        } catch (Exception var5) {
            Exception e = var5;
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting contact: " + e.getMessage());
        }
    }
}

