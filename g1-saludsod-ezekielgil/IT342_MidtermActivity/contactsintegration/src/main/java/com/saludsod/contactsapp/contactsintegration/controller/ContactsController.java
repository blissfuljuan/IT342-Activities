package com.saludsod.contactsapp.contactsintegration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludsod.contactsapp.contactsintegration.service.GoogleContactsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/contacts")
public class ContactsController {

    private final GoogleContactsService googleContactsService;

    public ContactsController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping
    public String getContacts(Model model, OAuth2AuthenticationToken authentication) {
        try {
            String jsonResponse = googleContactsService.getContacts(authentication);
            String username = authentication.getPrincipal().getAttribute("name");
            if (username == null) {
                username = "User";
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);
            List<Map<String, Object>> contacts = new ArrayList<>();

            if (root.has("connections")) {
                for (JsonNode connection : root.get("connections")) {
                    String firstName = connection.has("names") ? connection.get("names").get(0).get("givenName").asText() : "";
                    String lastName = connection.has("names") && connection.get("names").get(0).has("familyName") ? connection.get("names").get(0).get("familyName").asText() : "";
                    
                    List<String> emails = new ArrayList<>();
                    if (connection.has("emailAddresses")) {
                        for (JsonNode emailNode : connection.get("emailAddresses")) {
                            emails.add(emailNode.get("value").asText());
                        }
                    }
                    
                    List<String> phones = new ArrayList<>();
                    if (connection.has("phoneNumbers")) {
                        for (JsonNode phoneNode : connection.get("phoneNumbers")) {
                            phones.add(phoneNode.get("value").asText());
                        }
                    }

                    String profilePicture = connection.has("photos") ? connection.get("photos").get(0).get("url").asText() : "/default-profile.png";
                    String resourceName = connection.has("resourceName") ? connection.get("resourceName").asText() : "";

                    // Determine display name
                    String displayName;
                    if (!firstName.isEmpty() || !lastName.isEmpty()) {
                        displayName = (firstName + " " + lastName).trim();
                    } else if (!emails.isEmpty()) {
                        displayName = emails.get(0); // Use email if available
                    } else if (!phones.isEmpty()) {
                        displayName = phones.get(0); // Use phone if no email
                    } else {
                        displayName = "Unknown"; // Fallback if no name, email, or phone
                    }

                    // Debug logging
                    System.out.println("Contact: " + resourceName);
                    System.out.println("First Name: " + firstName);
                    System.out.println("Last Name: " + lastName);
                    System.out.println("Emails: " + emails);
                    System.out.println("Phones: " + phones);
                    System.out.println("Display Name: " + displayName);

                    contacts.add(Map.of(
                        "firstName", firstName,
                        "lastName", lastName,
                        "displayName", displayName,
                        "emails", emails,
                        "phones", phones,
                        "profilePicture", profilePicture,
                        "resourceName", resourceName
                    ));
                }
            }

            model.addAttribute("username", username);
            model.addAttribute("contacts", contacts);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch contacts: " + e.getMessage());
        }

        return "contacts";
    }

    @PostMapping("/add")
    public String createContact(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam(required = false) List<String> emails,
                                @RequestParam(required = false) List<String> phones,
                                OAuth2AuthenticationToken authentication,
                                Model model) {
        try {
            String response = googleContactsService.createContact(authentication, firstName, lastName, emails != null ? emails : new ArrayList<>(), phones != null ? phones : new ArrayList<>());
            model.addAttribute("message", "Contact added successfully!");
        } catch (Exception e) {
            model.addAttribute("message", "Failed to add contact: " + e.getMessage());
        }
        return "redirect:/contacts";
    }

    @PostMapping("/edit")
    public String updateContact(@RequestParam String resourceName,
                                @RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam(required = false) List<String> emails,
                                @RequestParam(required = false) List<String> phones,
                                OAuth2AuthenticationToken authentication,
                                Model model) {
        try {
            googleContactsService.deleteContact(authentication, resourceName);
            String response = googleContactsService.createContact(authentication, firstName, lastName, emails != null ? emails : new ArrayList<>(), phones != null ? phones : new ArrayList<>());
            model.addAttribute("message", "Contact updated successfully!");
        } catch (Exception e) {
            model.addAttribute("message", "Failed to update contact: " + e.getMessage());
        }
        return "redirect:/contacts";
    }

    @PostMapping("/delete")
    public String deleteContact(@RequestParam String resourceName,
                                OAuth2AuthenticationToken authentication,
                                Model model) {
        try {
            String response = googleContactsService.deleteContact(authentication, resourceName);
            System.out.println("Delete response: " + response);
        } catch (Exception e) {
            System.out.println("Error deleting contact: " + e.getMessage());
            model.addAttribute("error", "Failed to delete contact: " + e.getMessage());
        }
        return "redirect:/contacts";
    }
}