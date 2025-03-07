package com.example.gadiane.johnkarl.demolition.controller;

import com.example.gadiane.johnkarl.demolition.model.ContactForm;
import com.example.gadiane.johnkarl.demolition.service.GoogleContactsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/contacts")
public class ContactController {
    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);
    private final GoogleContactsService contactsService;

    public ContactController(GoogleContactsService contactsService) {
        this.contactsService = contactsService;
    }

    @GetMapping
    public String listContacts(Model model) {
        try {
            List<Map<String, Object>> contacts = contactsService.listContacts();
            List<ContactForm> contactForms = new ArrayList<>();
            
            for (Map<String, Object> contact : contacts) {
                contactForms.add(contactsService.mapToContactForm(contact));
            }
            
            model.addAttribute("contacts", contactForms);
            return "list";
        } catch (IOException e) {
            logger.error("Error listing contacts", e);
            model.addAttribute("errorMessage", "Failed to retrieve contacts: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("contactForm", new ContactForm());
        return "form";
    }

    @PostMapping("/new")
    public String createContact(@ModelAttribute ContactForm contactForm, RedirectAttributes redirectAttributes) {
        try {
            Map<String, Object> result = contactsService.createContact(contactForm);
            redirectAttributes.addFlashAttribute("successMessage", "Contact created successfully!");
            return "redirect:/contacts";
        } catch (IOException e) {
            logger.error("Error creating contact", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create contact: " + e.getMessage());
            redirectAttributes.addFlashAttribute("contactForm", contactForm);
            return "redirect:/contacts/new";
        }
    }

    @GetMapping("/{resourceName}")
    public String viewContact(@PathVariable String resourceName, Model model) {
        try {
            Map<String, Object> contact = contactsService.getContact(resourceName);
            ContactForm contactForm = contactsService.mapToContactForm(contact);
            model.addAttribute("contact", contactForm);
            return "view";
        } catch (IOException e) {
            logger.error("Error viewing contact", e);
            model.addAttribute("errorMessage", "Failed to retrieve contact: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/edit/{resourceName}")
    public String showEditForm(@PathVariable String resourceName, Model model) {
        logger.info("Showing edit form for contact with resource name: {}", resourceName);
        
        try {
            // Decode the resource name if it's URL-encoded
            resourceName = URLDecoder.decode(resourceName, StandardCharsets.UTF_8.toString());
            
            // Remove 'people/' prefix if it exists for the path variable
            String cleanResourceName = resourceName;
            if (resourceName.startsWith("people/")) {
                cleanResourceName = resourceName.substring(7);
            }
            
            logger.info("Fetching contact data for resource name: {}", cleanResourceName);
            Map<String, Object> contact = contactsService.getContact(cleanResourceName);
            
            if (contact == null) {
                logger.error("Contact not found for resource name: {}", cleanResourceName);
                return "redirect:/contacts";
            }
            
            ContactForm contactForm = contactsService.mapToContactForm(contact);
            model.addAttribute("contactForm", contactForm);
            
            return "form";
        } catch (Exception e) {
            logger.error("Error showing edit form", e);
            return "redirect:/contacts";
        }
    }

    @PutMapping("/edit/{resourceId}")
    @ResponseBody
    public ResponseEntity<ContactForm> updateContact(
            @PathVariable String resourceId,
            @RequestBody ContactForm contactForm) {
        try {
            logger.info("Attempting to update contact with resource name: {}", resourceId);
            logger.info("Contact form data: firstName={}, lastName={}, email={}, phone={}", 
                      contactForm.getFirstName(), contactForm.getLastName(), 
                      contactForm.getEmail(), contactForm.getPhoneNumber());
            
            // Ensure resourceName is properly formatted
            String decodedResourceId = URLDecoder.decode(resourceId, StandardCharsets.UTF_8.toString());
            
            // Fix potential duplicate "people/" prefix
            decodedResourceId = decodedResourceId.replace("people/people/", "people/");
            
            if (!decodedResourceId.startsWith("people/")) {
                decodedResourceId = "people/" + decodedResourceId;
            }
            
            logger.info("Formatted resource ID for update: {}", decodedResourceId);
            
            // Set the resource name in the contactForm
            contactForm.setResourceName(decodedResourceId);
            
            // Update contact
            Map<String, Object> result = contactsService.updateContact(contactForm);
            
            // Log successful update
            logger.info("Contact updated successfully: {}", result);
            
            return ResponseEntity.ok(contactForm);
        } catch (IOException e) {
            logger.error("Error updating contact: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/delete/{resourceName}")
    public String deleteContact(@PathVariable String resourceName, RedirectAttributes redirectAttributes) {
        try {
            contactsService.deleteContact(resourceName);
            redirectAttributes.addFlashAttribute("successMessage", "Contact deleted successfully!");
            return "redirect:/contacts";
        } catch (IOException e) {
            logger.error("Error deleting contact", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete contact: " + e.getMessage());
            return "redirect:/contacts";
        }
    }
}
