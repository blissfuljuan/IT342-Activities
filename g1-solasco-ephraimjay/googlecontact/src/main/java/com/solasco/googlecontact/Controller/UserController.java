package com.solasco.googlecontact.Controller;

import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.solasco.googlecontact.Service.GoogleContactsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {
    @Autowired
    private GoogleContactsService googleContactsService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @GetMapping("")
    public String index() {
        return "redirect:/contacts";  // Redirect to contacts page instead of non-existent home
    }

    @GetMapping("/user-info")
    public String getUserInfo(@AuthenticationPrincipal Object principal, Model model) {
        if (principal instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) principal;
            model.addAttribute("name", oidcUser.getFullName());
            model.addAttribute("email", oidcUser.getEmail());
            model.addAttribute("pictureUrl", oidcUser.getPicture());
        } else if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            Map<String, Object> attributes = oauth2User.getAttributes();
            model.addAttribute("name", attributes.get("name"));
            model.addAttribute("email", attributes.get("email"));
            model.addAttribute("pictureUrl", attributes.get("picture"));
        } else {
            return "redirect:/";
        }

        try {
            // Get contacts statistics
            List<Person> contacts = googleContactsService.getConnectionsAsPeople((OAuth2User) principal);
            int totalContacts = contacts.size();
            int emailContacts = 0;
            int phoneContacts = 0;

            for (Person contact : contacts) {
                if (contact.getEmailAddresses() != null && !contact.getEmailAddresses().isEmpty()) {
                    emailContacts++;
                }
                if (contact.getPhoneNumbers() != null && !contact.getPhoneNumbers().isEmpty()) {
                    phoneContacts++;
                }
            }

            model.addAttribute("totalContacts", totalContacts);
            model.addAttribute("emailContacts", emailContacts);
            model.addAttribute("phoneContacts", phoneContacts);
        } catch (RuntimeException e) {
            // If we can't get contacts, just set defaults
            model.addAttribute("totalContacts", 0);
            model.addAttribute("emailContacts", 0);
            model.addAttribute("phoneContacts", 0);
        }

        return "user-info";
    }

    @GetMapping("/contacts")
    public String fetchContactsFromGoogle(Model model, @AuthenticationPrincipal OAuth2User principal) throws IOException {
        if (principal == null) {
            return "redirect:/";
        }
        List<Person> connections = googleContactsService.getConnectionsAsPeople(principal);
        model.addAttribute("contacts", connections);
        return "contact";
    }

    @GetMapping("/addContact")
    public String showAddContactForm(Model model) {
        return "addContact";
    }

    @PostMapping("/addContact")
    public String addContact(
            @RequestParam("displayName") String name,
            @RequestParam(required = false) List<String> emails,
            @RequestParam(required = false) List<String> emailTypes,
            @RequestParam(required = false) List<String> phones,
            @RequestParam(required = false) List<String> phoneTypes,
            @AuthenticationPrincipal OAuth2User principal,
            Model model) {
        try {
            Person newPerson = new Person();

            // Add name
            Name personName = new Name();
            personName.setDisplayName(name);
            personName.setGivenName(name);
            newPerson.setNames(Arrays.asList(personName));

            // Add emails
            if (emails != null && !emails.isEmpty()) {
                List<EmailAddress> emailAddresses = new ArrayList<>();
                for (int i = 0; i < emails.size(); i++) {
                    String email = emails.get(i);
                    String type = (emailTypes != null && emailTypes.size() > i) ? emailTypes.get(i) : "other";
                    if (!email.isEmpty()) {
                        emailAddresses.add(new EmailAddress()
                            .setValue(email)
                            .setType(type));
                    }
                }
                if (!emailAddresses.isEmpty()) {
                    newPerson.setEmailAddresses(emailAddresses);
                }
            }

            // Add phones
            if (phones != null && !phones.isEmpty()) {
                List<PhoneNumber> phoneNumbers = new ArrayList<>();
                for (int i = 0; i < phones.size(); i++) {
                    String phone = phones.get(i);
                    String type = (phoneTypes != null && phoneTypes.size() > i) ? phoneTypes.get(i) : "other";
                    if (!phone.isEmpty()) {
                        phoneNumbers.add(new PhoneNumber()
                            .setValue(phone)
                            .setType(type));
                    }
                }
                if (!phoneNumbers.isEmpty()) {
                    newPerson.setPhoneNumbers(phoneNumbers);
                }
            }

            // Create the contact
            googleContactsService.createContact(principal, newPerson);
            return "redirect:/contacts";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add contact: " + e.getMessage());
            return "addContact";
        }
    }

    @GetMapping("/contacts/edit/people/{contactId}")
    public String showEditContactForm(@PathVariable String contactId,
                                    @AuthenticationPrincipal OAuth2User principal,
                                    Model model) {
        try {
            Person contact = googleContactsService.getPersonById(principal, contactId);
            if (contact == null) {
                throw new RuntimeException("Contact not found");
            }
            model.addAttribute("contact", contact);
            return "editContact";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Failed to load contact: " + e.getMessage());
            return "editContact";
        }
    }

    @PostMapping("/contacts/edit/people/{contactId}")
    public String updateContact(
            @PathVariable String contactId,
            @RequestParam String displayName,
            @RequestParam(required = false) List<String> emails,
            @RequestParam(required = false) List<String> emailTypes,
            @RequestParam(required = false) List<String> phones,
            @RequestParam(required = false) List<String> phoneTypes,
            @AuthenticationPrincipal OAuth2User principal,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            // Ensure contactId has "people/" prefix
            String resourceName = contactId.startsWith("people/") ? contactId : "people/" + contactId;
            Person existingContact = googleContactsService.getPersonById(principal, resourceName);
            if (existingContact == null) {
                model.addAttribute("error", "Contact not found");
                return "editContact";
            }

            // Create update person object
            Person updatePerson = new Person();
            updatePerson.setEtag(existingContact.getEtag());
            updatePerson.setResourceName(resourceName);

            // Update name
            Name name = new Name();
            name.setDisplayName(displayName);
            name.setGivenName(displayName);
            updatePerson.setNames(Collections.singletonList(name));

            // Update emails if provided
            if (emails != null && !emails.isEmpty()) {
                List<EmailAddress> emailAddresses = new ArrayList<>();
                for (int i = 0; i < emails.size(); i++) {
                    String email = emails.get(i).trim();
                    if (!email.isEmpty()) {
                        String type = (emailTypes != null && emailTypes.size() > i) ? emailTypes.get(i) : "other";
                        emailAddresses.add(new EmailAddress().setValue(email).setType(type));
                    }
                }
                if (!emailAddresses.isEmpty()) {
                    updatePerson.setEmailAddresses(emailAddresses);
                }
            }

            // Update phones if provided
            if (phones != null && !phones.isEmpty()) {
                List<PhoneNumber> phoneNumbers = new ArrayList<>();
                for (int i = 0; i < phones.size(); i++) {
                    String phone = phones.get(i).trim();
                    if (!phone.isEmpty()) {
                        String type = (phoneTypes != null && phoneTypes.size() > i) ? phoneTypes.get(i) : "other";
                        phoneNumbers.add(new PhoneNumber().setValue(phone).setType(type));
                    }
                }
                if (!phoneNumbers.isEmpty()) {
                    updatePerson.setPhoneNumbers(phoneNumbers);
                }
            }

            // Update the contact
            Person updatedContact = googleContactsService.updateContact(principal, resourceName, updatePerson);
            redirectAttributes.addFlashAttribute("success", "Contact updated successfully");
            return "redirect:/contacts";
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update contact: " + e.getMessage());
            try {
                model.addAttribute("contact", googleContactsService.getPersonById(principal, contactId));
            } catch (Exception ex) {
                // If we can't load the contact, the form will show appropriate error
            }
            return "editContact";
        }
    }

    @PostMapping("/contacts/delete/{contactId}")
    public String deleteContact(
            @PathVariable String contactId,
            @AuthenticationPrincipal OAuth2User principal,
            Model model) {
        try {
            // Ensure contactId has "people/" prefix
            String resourceName = contactId.startsWith("people/") ? contactId : "people/" + contactId;
            googleContactsService.deleteContact(principal, resourceName);
            return "redirect:/contacts";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete contact: " + e.getMessage());
            return "redirect:/contacts";
        }
    }

    @DeleteMapping("/contacts/delete/{contactId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteContactAjax(
            @PathVariable String contactId,
            @AuthenticationPrincipal OAuth2User principal) {
        Map<String, String> response = new HashMap<>();
        try {
            googleContactsService.deleteContact(principal, contactId);
            response.put("status", "success");
            response.put("message", "Contact deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/contacts/delete/people/{contactId}")
    public String deleteContact(
            @PathVariable String contactId,
            @AuthenticationPrincipal OAuth2User principal) {
        try {
            googleContactsService.deleteContact(principal, contactId);
        } catch (Exception e) {
            System.err.println("Error deleting contact: " + e.getMessage());
        }
        return "redirect:/contacts";
    }
}