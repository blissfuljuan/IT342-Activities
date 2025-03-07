package com.orlanes.oauth2login.controller;

import com.orlanes.oauth2login.service.GoogleContactsService;
import com.orlanes.oauth2login.utility.ContactUtil;
import com.google.api.services.people.v1.model.Person;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        @RequestParam("email") String email,
        @RequestParam("phoneNumber") String phoneNumber,
        RedirectAttributes redirectAttributes) {

    Person contact = googlePeopleService.createPersonObject(firstName, lastName, email, phoneNumber);

    try {
        Person createdContact = googlePeopleService.createContact(authentication, contact);

        if (createdContact != null) {
            redirectAttributes.addFlashAttribute("message", "Contact successfully created!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to create contact.");
        }
    } catch (Exception e) {
        e.printStackTrace();  // Print full error stack trace to VSCode terminal
        redirectAttributes.addFlashAttribute("error", "Error creating contact: " + e.getMessage());
    }

    return "redirect:/contacts";
}


    @GetMapping("/contacts/{resourceName}/edit")
    public String editContactForm(
            @PathVariable String resourceName,
            OAuth2AuthenticationToken authentication,
            Model model) {
        
        // The resourceName comes from URL as "people/c1234567" format
        // But we need to replace the slash for Thymeleaf to process it properly
        String encodedResourceName = resourceName.replace("/", "_");
        
        Person contact = googlePeopleService.getContact(authentication, "people/" + resourceName);
        
        if (contact == null) {
            return "redirect:/contacts";
        }
        
        model.addAttribute("contact", contact);
        model.addAttribute("resourceName", encodedResourceName);
        model.addAttribute("utils", new ContactUtil());
        
        return "contacts/edit";
    }

    @PostMapping("/contacts/{resourceName}")
public String updateContact(
        @PathVariable String resourceName,
        OAuth2AuthenticationToken authentication,
        @RequestParam("firstName") String firstName,
        @RequestParam("lastName") String lastName,
        @RequestParam("email") String email,
        @RequestParam("phoneNumber") String phoneNumber,
        RedirectAttributes redirectAttributes) {

    // Decode resource name
    String decodedResourceName = "people/" + resourceName.replace("_", "/");

    // Retrieve existing contact to get its etag
    Person existingContact = googlePeopleService.getContact(authentication, decodedResourceName);
    if (existingContact == null) {
        redirectAttributes.addFlashAttribute("error", "Contact not found.");
        return "redirect:/contacts";
    }

    // Create new contact object with updated fields
    Person contact = googlePeopleService.createPersonObject(firstName, lastName, email, phoneNumber);
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