package com.labajos.contactsapp.contactsintegration.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.labajos.contactsapp.contactsintegration.model.Contact;
import com.labajos.contactsapp.contactsintegration.service.GoogleContactsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/contacts")
public class ContactsController {

    @Autowired
    private GoogleContactsService googleContactsService;

    public ContactsController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping
    public String getContacts(Model model, OAuth2AuthenticationToken authentication) {
        try {
            String jsonResponse = googleContactsService.getContacts(authentication);
    
            String username = authentication.getPrincipal().getAttribute("name");
            if (username == null) {
                username = "User"; // Fallback if name is not available
            }
    
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);
    
            List<Map<String, Object>> contacts = new ArrayList<>(); // Changed to Object to accommodate List<String>
    
            if (root.has("connections")) {
                for (JsonNode connection : root.get("connections")) {
                    String name = connection.has("names") ? 
                                  connection.get("names").get(0).get("displayName").asText() : 
                                  "Unknown";
    
                    String email = connection.has("emailAddresses") ? 
                                   connection.get("emailAddresses").get(0).get("value").asText() : 
                                   "No email";
    
                    // Collect all phone numbers into a list
                    List<String> phones = new ArrayList<>();
                    if (connection.has("phoneNumbers")) {
                        for (JsonNode phoneNode : connection.get("phoneNumbers")) {
                            phones.add(phoneNode.get("value").asText());
                        }
                    }
                    if (phones.isEmpty()) {
                        phones.add("No phone");
                    }
    
                    String resourceName = connection.has("resourceName") ? 
                                          connection.get("resourceName").asText() : 
                                          "";
    
                    // Generate initials for profile icon
                    String[] nameParts = name.split(" ");
                    String initials = nameParts.length > 1 ? 
                                      nameParts[0].substring(0, 1) + nameParts[1].substring(0, 1) : 
                                      nameParts[0].substring(0, 1);
                    initials = initials.toUpperCase();
    
                    contacts.add(Map.of(
                        "name", name, 
                        "email", email, 
                        "phone", phones, // Now a List<String>
                        "initial", initials,
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
    public String createContact(@RequestParam String name,
            @RequestParam String email,
            @RequestParam(value = "phone") List<String> phones, // Changed to List<String>
            OAuth2AuthenticationToken authentication,
            Model model) {
        String response = googleContactsService.createContact(authentication, name, email, phones);
        model.addAttribute("message", response);
        return "redirect:/contacts";
    }
    
    @PostMapping("/edit")
    public String updateContact(
            @RequestParam String resourceName,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(value = "phone") List<String> phones, // Changed to List<String>
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
            RedirectAttributes redirectAttributes) {
        try {
            Contact contact = new Contact(
                name,
                Arrays.asList(email),
                phones, // Pass the list directly
                resourceName
            );
            googleContactsService.updateContact(authorizedClient, resourceName, contact);
            redirectAttributes.addFlashAttribute("success", "Contact updated successfully");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg.contains("NOT_FOUND")) {
                errorMsg = "Contact not found or was deleted. Please refresh the page and try again.";
            } else if (errorMsg.contains("INVALID_ARGUMENT")) {
                errorMsg = "Invalid contact information provided. Please check all fields and try again.";
            }
            System.err.println("Error in updateContact: " + errorMsg);
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error updating contact: " + errorMsg);
        }
        return "redirect:/contacts";
    }        
    
    // This method handles the deletion of contacts. Make sure the form sends a DELETE request.
    @PostMapping("/delete")
    public String deleteContact(
            @RequestParam String resourceName,
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
            RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Delete Contact - Resource Name: " + resourceName);
            googleContactsService.deleteContact(authorizedClient, resourceName);
            redirectAttributes.addFlashAttribute("success", "Contact deleted successfully");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg.contains("NOT_FOUND")) {
                errorMsg = "Contact not found or was already deleted. Please refresh the page.";
            }
            System.err.println("Error in deleteContact: " + errorMsg);
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error deleting contact: " + errorMsg);
        }
        return "redirect:/contacts";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, 
                SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/"; // Redirects to login page
    }
}