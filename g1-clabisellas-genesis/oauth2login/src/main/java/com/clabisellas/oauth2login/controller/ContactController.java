package com.clabisellas.oauth2login.controller;


import com.clabisellas.oauth2login.service.GoogleContactsService;
import com.clabisellas.oauth2login.utilities.ContactUtil;
import com.google.api.services.people.v1.model.Person;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ContactController {

    private final GoogleContactsService googlePeopleService;

    public ContactController(GoogleContactsService googlePeopleService) {
        this.googlePeopleService = googlePeopleService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/contacts")
    public String listContacts(OAuth2AuthenticationToken authentication, Model model) {
        List<Person> contacts = googlePeopleService.getContacts(authentication);
        model.addAttribute("contacts", contacts);
        model.addAttribute("utils", new ContactUtil());
        return "contacts/list";
    }

    @GetMapping("/contacts/new")
    public String newContactForm(Model model) {
        model.addAttribute("contact", new Person());
        return "contacts/form";
    }

    @PostMapping("/contacts")
    public String createContact(
            OAuth2AuthenticationToken authentication,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("emails") List<String> emails,
            @RequestParam("phoneNumbers") List<String> phoneNumbers,
            RedirectAttributes redirectAttributes) {
    
        Person contact = googlePeopleService.createPersonObject(firstName, lastName, emails, phoneNumbers);
    
        try {
            Person createdContact = googlePeopleService.createContact(authentication, contact);
    
            if (createdContact != null) {
                redirectAttributes.addFlashAttribute("message", "Contact successfully created!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to create contact.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error creating contact: " + e.getMessage());
        }
    
        return "redirect:/contacts";
    }
    


    @GetMapping("/contacts/{resourceName}/edit")
    public String editContactForm(
        @PathVariable String resourceName,
        OAuth2AuthenticationToken authentication,
        Model model) {
    
    // Ensure the resourceName is properly formatted for the API request
    String formattedResourceName = resourceName.startsWith("people/") ? resourceName : "people/" + resourceName;

    try {
        // Fetch the contact using the Google People API
        Person contact = googlePeopleService.getContact(authentication, formattedResourceName);

        if (contact == null) {
            System.out.println("Contact not found for resource: " + formattedResourceName);
            return "redirect:/contacts?error=contact_not_found"; // Redirect if the contact doesn't exist
        }

        // Encode resource name for Thymeleaf processing
        String encodedResourceName = resourceName.replace("/", "_");

        // Add attributes to the model for Thymeleaf rendering
        model.addAttribute("contact", contact);
        model.addAttribute("resourceName", encodedResourceName);
        model.addAttribute("utils", new ContactUtil());

        return "contacts/edit";

    } catch (Exception e) {
        System.err.println("Error fetching contact: " + e.getMessage());
        e.printStackTrace(); // Print full error details in logs

        return "redirect:/contacts?error=server_error"; // Redirect if an unexpected error occurs
    }
}


    @PostMapping("/contacts/{resourceName}")
public String updateContact(
        @PathVariable String resourceName,
        OAuth2AuthenticationToken authentication,
        @RequestParam("firstName") String firstName,
        @RequestParam(value = "lastName", required = false) String lastName,
        @RequestParam(value = "emails", required = false) List<String> emails,
        @RequestParam(value = "phoneNumbers", required = false) List<String> phoneNumbers,
        RedirectAttributes redirectAttributes) {

    // Decode resource name
    String decodedResourceName = "people/" + resourceName.replace("_", "/");

    // Retrieve existing contact to get its etag
    Person existingContact = googlePeopleService.getContact(authentication, decodedResourceName);
    if (existingContact == null) {
        redirectAttributes.addFlashAttribute("error", "Contact not found.");
        return "redirect:/contacts";
    }

    // Ensure lastName is not null (prevent NullPointerException)
    if (lastName == null) {
        lastName = "";  // Set to empty string if not provided
    }

    // Ensure emails and phoneNumbers are not null
    if (emails == null) {
        emails = new ArrayList<>();  // Set to empty list if none provided
    }
    if (phoneNumbers == null) {
        phoneNumbers = new ArrayList<>();  // Set to empty list if none provided
    }

    // Create new contact object with updated fields
    Person contact = googlePeopleService.createPersonObject(firstName, lastName, emails, phoneNumbers);
    contact.setEtag(existingContact.getEtag());  // Ensure etag is included

    // Update the contact
    Person updatedContact = googlePeopleService.updateContact(authentication, decodedResourceName, contact, "names,emailAddresses,phoneNumbers");

    if (updatedContact != null) {
        redirectAttributes.addFlashAttribute("message", "Contact successfully updated!");
    } else {
        redirectAttributes.addFlashAttribute("error", "Failed to update contact.");
    }

    return "redirect:/contacts";
}


    @GetMapping("/contacts/{resourceName}/delete")
    public String deleteContact(
            @PathVariable String resourceName,
            OAuth2AuthenticationToken authentication,
            RedirectAttributes redirectAttributes) {
        
        // Decode resource name
        String decodedResourceName = "people/" + resourceName.replace("_", "/");
        
        googlePeopleService.deleteContact(authentication, decodedResourceName);
        redirectAttributes.addFlashAttribute("message", "Contact successfully deleted!");
        
        return "redirect:/contacts";
    }
}