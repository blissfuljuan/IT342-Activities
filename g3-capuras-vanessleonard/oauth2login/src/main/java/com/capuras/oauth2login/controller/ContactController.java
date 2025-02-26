package com.capuras.oauth2login.controller;

import com.capuras.oauth2login.model.Contact;
import com.capuras.oauth2login.service.GooglePeopleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class ContactController {

    private final GooglePeopleService googlePeopleService;

    public ContactController(GooglePeopleService googlePeopleService) {
        this.googlePeopleService = googlePeopleService;
    }


    @GetMapping("/contacts")
    public String showAllContacts(Model model, OAuth2AuthenticationToken authentication) {
        Map<String, Contact> contactsMap = googlePeopleService.getContactsMap(authentication);
        model.addAttribute("contactsMap", contactsMap);
        return "contacts";
    }

    @RestController
    @RequestMapping("/api/contacts")
    public static class ContactApiController {

        private final GooglePeopleService googlePeopleService;

        public ContactApiController(GooglePeopleService googlePeopleService) {
            this.googlePeopleService = googlePeopleService;
        }

        @GetMapping
        public ResponseEntity<Map<String, Contact>> getAllContacts(OAuth2AuthenticationToken authentication) {
            Map<String, Contact> contacts = googlePeopleService.getContactsMap(authentication);
            return ResponseEntity.ok(contacts);
        }

        @PutMapping("/{resourceId}")
        public ResponseEntity<Contact> updateContact(
                OAuth2AuthenticationToken authentication,
                @PathVariable String resourceId,
                @RequestBody Contact contact) {

            Contact updatedContact = googlePeopleService.updateContact(authentication, resourceId, contact);
            return ResponseEntity.ok(updatedContact);
        }

        @DeleteMapping("/{resourceId}")
        public ResponseEntity<Void> deleteContact(
                OAuth2AuthenticationToken authentication,
                @PathVariable String resourceId) {

            googlePeopleService.deleteContact(authentication, resourceId);
            return ResponseEntity.noContent().build();
        }
    }
}