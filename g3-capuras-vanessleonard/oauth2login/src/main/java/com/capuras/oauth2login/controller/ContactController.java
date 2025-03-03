package com.capuras.oauth2login.controller;

import com.capuras.oauth2login.model.Contact;
import com.capuras.oauth2login.service.GooglePeopleService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
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
public class ContactController {

    private final GooglePeopleService googlePeopleService;

    public ContactController(GooglePeopleService googlePeopleService) {
        this.googlePeopleService = googlePeopleService;
    }

    @GetMapping("/view")
    public String showAllContacts(Model model, OAuth2AuthenticationToken authentication, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        Map<String, Contact> contactsMap = googlePeopleService.getContactsMap(authentication);
        model.addAttribute("contactsMap", contactsMap);
        return "contacts";
    }


    @GetMapping
    @ResponseBody
    public ResponseEntity<Map<String, Contact>> getAllContacts(OAuth2AuthenticationToken authentication) {
        Map<String, Contact> contacts = googlePeopleService.getContactsMap(authentication);


        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return ResponseEntity.ok()
                .headers(headers)
                .body(contacts);
    }

    @PutMapping("/{resourceId}")
    @ResponseBody
    public ResponseEntity<Contact> updateContact(
            OAuth2AuthenticationToken authentication,
            @PathVariable String resourceId,
            @RequestBody Contact contact) {
        try {
            String decodedResourceId = URLDecoder.decode(resourceId, StandardCharsets.UTF_8.toString());

            decodedResourceId = decodedResourceId.replace("people/people/", "people/");

            if (!decodedResourceId.startsWith("people/")) {
                decodedResourceId = "people/" + decodedResourceId;
            }

            System.out.println("Attempting to update contact with ID: " + decodedResourceId);

            Contact updatedContact = googlePeopleService.updateContact(authentication, decodedResourceId, contact);
            return ResponseEntity.ok(updatedContact);
        } catch (Exception e) {
            System.err.println("Error in update endpoint: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{resourceId}")
    @ResponseBody
    public ResponseEntity<Void> deleteContact(
            OAuth2AuthenticationToken authentication,
            @PathVariable String resourceId) {
        try {
            resourceId = resourceId.replace("%2F", "/");

            if (resourceId.contains("/")) {
                resourceId = resourceId.substring(resourceId.lastIndexOf('/') + 1);
            }

            System.out.println("Attempting to delete contact with ID: " + resourceId);

            googlePeopleService.deleteContact(authentication, resourceId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("Error in delete endpoint: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<Contact> createContact(
            OAuth2AuthenticationToken authentication,
            @RequestBody Contact contact) {
        try {
            Contact newContact = googlePeopleService.createContact(authentication, contact);
            return ResponseEntity.ok(newContact);
        } catch (Exception e) {
            System.err.println("Error in create contact endpoint: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}