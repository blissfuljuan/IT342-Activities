package com.Vitorillo.midterm.Controller;

import com.Vitorillo.midterm.Sevices.GoogleContactsService;
import com.google.api.services.people.v1.model.Person;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/contacts")
public class ContactsController {
    private static final Logger logger = LoggerFactory.getLogger(ContactsController.class);
    private final GoogleContactsService contactsService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public ContactsController(GoogleContactsService contactsService, OAuth2AuthorizedClientService authorizedClientService) {
        this.contactsService = contactsService;
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping
    public String listContacts(
        Model model, 
        OAuth2AuthenticationToken authentication
    ) {
        try {
            if (authentication == null) {
                logger.error("Authentication is null. User is not authenticated.");
                model.addAttribute("error", "Authentication is null. Please log in again.");
                return "error";
            }
            
            logger.info("Authentication token present: " + authentication.getName());
            logger.info("Authorized client registration ID: " + authentication.getAuthorizedClientRegistrationId());
            
            try {
                OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    authentication.getAuthorizedClientRegistrationId(), 
                    authentication.getName()
                );
                
                if (client == null) {
                    logger.error("OAuth2AuthorizedClient is null. Token might be invalid or expired.");
                    model.addAttribute("error", "OAuth token is invalid or expired. Please log in again.");
                    return "error";
                }
                
                logger.info("Access token: " + client.getAccessToken().getTokenValue().substring(0, 10) + "...");
                logger.info("Token expires at: " + client.getAccessToken().getExpiresAt());
            } catch (Exception e) {
                logger.error("Error loading authorized client: " + e.getMessage(), e);
                model.addAttribute("error", "Error with OAuth token: " + e.getMessage());
                return "error";
            }
            
            List<Person> contacts = contactsService.listContacts(authentication);
            logger.info("Successfully retrieved " + (contacts != null ? contacts.size() : 0) + " contacts");
            model.addAttribute("contacts", contacts != null ? contacts : Collections.emptyList());
            return "contacts";
        } catch (Exception e) {
            logger.error("Error in listContacts: " + e.getMessage(), e);
            e.printStackTrace();
            model.addAttribute("error", "Error fetching contacts: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/add")
    public String showAddContactForm() {
        return "add-contact";
    }

    @PostMapping("/add")
    public String addContact(
        OAuth2AuthenticationToken authentication,
        @RequestParam String firstName,
        @RequestParam String lastName,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String phoneNumber
    ) throws IOException {
        contactsService.createContact(
            authentication, 
            firstName, 
            lastName, 
            email, 
            phoneNumber
        );
        return "redirect:/contacts";
    }

    @GetMapping("/edit/{resourceName}")
    public String showEditContactForm(
        OAuth2AuthenticationToken authentication,
        @PathVariable String resourceName, 
        Model model
    ) {
        try {
            logger.info("Showing edit form for resourceName: " + resourceName);
            // Fetch the specific contact details
            try {
                Person contact = contactsService.getContact(authentication, resourceName);
                if (contact != null) {
                    model.addAttribute("contact", contact);
                    logger.info("Successfully retrieved contact details for editing");
                }
            } catch (Exception e) {
                logger.warn("Could not fetch contact details: " + e.getMessage());
                // Continue without contact details - form will be empty
            }
            model.addAttribute("resourceName", resourceName);
            return "edit-contact";
        } catch (Exception e) {
            logger.error("Error showing edit form: " + e.getMessage(), e);
            model.addAttribute("error", "Error loading contact: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/edit/people/{resourceName}")
    public String showEditContactFormWithPeople(
        OAuth2AuthenticationToken authentication,
        @PathVariable String resourceName, 
        Model model
    ) {
        try {
            logger.info("Showing edit form with people/ prefix. ResourceName: " + resourceName);
            // Create the full resource name for the People API
            String fullResourceName = "people/" + resourceName;
            logger.info("Full resource name: " + fullResourceName);
            
            // Fetch the specific contact details
            try {
                Person contact = contactsService.getContact(authentication, fullResourceName);
                if (contact != null) {
                    model.addAttribute("contact", contact);
                    logger.info("Successfully retrieved contact details for editing");
                }
            } catch (Exception e) {
                logger.warn("Could not fetch contact details: " + e.getMessage());
                // Continue without contact details - form will be empty
            }
            
            model.addAttribute("resourceName", fullResourceName);
            return "edit-contact";
        } catch (Exception e) {
            logger.error("Error showing edit form with people/ prefix: " + e.getMessage(), e);
            model.addAttribute("error", "Error loading contact: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/edit/{resourceName}")
    public String updateContact(
        OAuth2AuthenticationToken authentication,
        @PathVariable String resourceName,
        @RequestParam String firstName,
        @RequestParam String lastName,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String phoneNumber,
        Model model
    ) {
        try {
            logger.info("Updating contact with resourceName: " + resourceName);
            contactsService.updateContact(
                authentication, 
                resourceName, 
                firstName, 
                lastName, 
                email, 
                phoneNumber
            );
            return "redirect:/contacts";
        } catch (Exception e) {
            logger.error("Error updating contact: " + e.getMessage(), e);
            model.addAttribute("error", "Error updating contact: " + e.getMessage());
            return "error";
        }
    }
    
    @PostMapping("/edit/people/{resourceName}")
    public String updateContactWithPeople(
        OAuth2AuthenticationToken authentication,
        @PathVariable String resourceName,
        @RequestParam String firstName,
        @RequestParam String lastName,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String phoneNumber,
        Model model
    ) {
        try {
            logger.info("Updating contact with people/ prefix. ResourceName: " + resourceName);
            // Create the full resource name for the People API
            String fullResourceName = "people/" + resourceName;
            logger.info("Full resource name: " + fullResourceName);
            contactsService.updateContact(
                authentication, 
                fullResourceName, 
                firstName, 
                lastName, 
                email, 
                phoneNumber
            );
            return "redirect:/contacts";
        } catch (Exception e) {
            logger.error("Error updating contact with people/ prefix: " + e.getMessage(), e);
            model.addAttribute("error", "Error updating contact: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/delete/{resourceName}")
    public String deleteContact(
        OAuth2AuthenticationToken authentication,
        @PathVariable String resourceName,
        Model model
    ) {
        try {
            logger.info("Deleting contact with resourceName: " + resourceName);
            contactsService.deleteContact(authentication, resourceName);
            return "redirect:/contacts";
        } catch (Exception e) {
            logger.error("Error deleting contact: " + e.getMessage(), e);
            model.addAttribute("error", "Error deleting contact: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/delete/people/{resourceName}")
    public String deleteContactWithPeople(
        OAuth2AuthenticationToken authentication,
        @PathVariable String resourceName,
        Model model
    ) {
        try {
            logger.info("Deleting contact with people/ prefix. ResourceName: " + resourceName);
            // Create the full resource name for the People API
            String fullResourceName = "people/" + resourceName;
            logger.info("Full resource name: " + fullResourceName);
            contactsService.deleteContact(authentication, fullResourceName);
            return "redirect:/contacts";
        } catch (Exception e) {
            logger.error("Error deleting contact with people/ prefix: " + e.getMessage(), e);
            model.addAttribute("error", "Error deleting contact: " + e.getMessage());
            return "error";
        }
    }
}
