package com.vijar.oauth2login.controller;

import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vijar.oauth2login.model.Contacts;
import com.vijar.oauth2login.service.GoogleContactsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

@Controller
public class UserController {
    @Autowired
    private GoogleContactsService googleContactsService;

    @SuppressWarnings("unused")
    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @GetMapping("")
    public String index() {
        return "home";  // Return home view
    }

    @GetMapping("/user-info")
    public String getUserInfo(@AuthenticationPrincipal Object principal, Model model) {
        if (principal instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) principal;
            // Extract OIDC user info
            String name = oidcUser.getFullName();
            String email = oidcUser.getEmail();
            String pictureUrl = oidcUser.getPicture();

            // Add attributes to model
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("pictureUrl", pictureUrl);
        } else if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            // Extract OAuth2 user info
            String name = oauth2User.getAttribute("name");
            String email = oauth2User.getAttribute("email");
            String pictureUrl = oauth2User.getAttribute("picture");

            // Add attributes to model
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("pictureUrl", pictureUrl);
        } else {
            return "redirect:/"; // Redirect if not authenticated
        }

        // Fetch contacts and calculate totals
        List<Contacts> contacts = googleContactsService.getContacts((OAuth2User) principal);
        int totalContacts = contacts.size();
        int totalEmails = contacts.stream().mapToInt(contact -> contact.getEmail().size()).sum();
        int totalPhoneNumbers = contacts.stream().mapToInt(contact -> contact.getPhoneNumber().size()).sum();

        // Add totals to model
        model.addAttribute("totalContacts", totalContacts);
        model.addAttribute("totalEmails", totalEmails);
        model.addAttribute("totalPhoneNumbers", totalPhoneNumbers);

        return "user-info"; // Return the view name
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

    @GetMapping("/contact/add-form")
    public String showAddContactForm(Model model) {
        return "addContact";
    }

    @PostMapping("/contact/add")
    public String addContact(
            @RequestParam("displayName") String name,
            @RequestParam List<String> email,
            @RequestParam List<String> phoneNumber,
            @AuthenticationPrincipal OAuth2User principal,
            Model model, RedirectAttributes redirectAttributes) {

        System.out.println("Adding contact: " + name + ", " + email + ", " + phoneNumber);

        if (email == null || email.isEmpty()) {
            model.addAttribute("error", "At least one email is required.");
            return "addContact";
        }

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            model.addAttribute("error", "At least one phone number is required.");
            return "addContact";
        }

        try {
            // Ensure emails are unique
            List<Contacts> existingContacts = googleContactsService.getContacts(principal);
            for (Contacts contact : existingContacts) {
                for (String existingEmail : contact.getEmail()) {
                    if (email.contains(existingEmail)) {
                        model.addAttribute("error", "Email " + "existingEmail already exists in another contact.");
                        return "addContact";
                    }
                }
            }

            googleContactsService.addContact(principal, name, email, phoneNumber);
            redirectAttributes.addFlashAttribute("success", "Contact added successfully.");
            return "redirect:/contacts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add contact: " + e.getMessage());
            return "redirect:/contact/add-form";
        }
    }

    @GetMapping("/contacts/edit/people/{contactId}")
    public String editContactForm(
            @PathVariable String contactId,
            @AuthenticationPrincipal OAuth2User principal,
            Model model) {

        try {
            // Fetch the contact by ID
            Person contact = googleContactsService.getPersonById(principal, "people/" + contactId);

            if (contact == null) {
                throw new RuntimeException("Contact not found.");
            }

            // Add the contact to the model
            model.addAttribute("contact", contact);

            // Return the edit form template
            return "editContact";

        } catch (RuntimeException e) {
            // Log the error and add an error message to the model
            model.addAttribute("error", "Failed to load contact: " + e.getMessage());
            return "redirect:/contacts"; // Redirect to the contacts list with an error message
        }
    }

    // POST: Handle the form submission
    @PostMapping("/contacts/edit/people/{contactId}")
    public String updateContact(
            @PathVariable String contactId,
            @RequestParam String displayName,
            @RequestParam List<String> email,
            @RequestParam List<String> phoneNumber,
            @AuthenticationPrincipal OAuth2User principal,
            Model model, RedirectAttributes redirectAttributes) {

        if (email == null || email.isEmpty()) {
            model.addAttribute("error", "At least one email is required.");
            return "editContact";
        }

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            model.addAttribute("error", "At least one phone number is required.");
            return "editContact";
        }

        try {
            // Ensure emails are unique
            List<Contacts> existingContacts = googleContactsService.getContacts(principal);
            for (Contacts contact : existingContacts) {
                if (!contact.getResourceName().equals("people/" + contactId)) {
                    for (String existingEmail : contact.getEmail()) {
                        if (email.contains(existingEmail)) {
                            model.addAttribute("error", "Email " + "existingEmail already exists in another contact.");
                            return "editContact";
                        }
                    }
                }
            }

            // Fetch the contact to get the current etag
            Person existingContact = googleContactsService.getPersonById(principal, "people/" + contactId);
            if (existingContact == null) {
                throw new RuntimeException("Contact not found.");
            }

            // Create a person for update
            Person updatePerson = new Person();
            updatePerson.setEtag(existingContact.getEtag()); // Set the etag

            // Update name if provided
            if (displayName != null && !displayName.isEmpty()) {
                Name personName = new Name();
                personName.setDisplayName(displayName);
                personName.setGivenName(displayName);
                updatePerson.setNames(Arrays.asList(personName));
            }

            // Update emails if provided
            if (email != null && !email.isEmpty()) {
                List<EmailAddress> emailAddresses = new ArrayList<>();
                for (String emailAddr : email) {
                    EmailAddress emailAddress = new EmailAddress();
                    emailAddress.setValue(emailAddr);
                    emailAddress.setType("home");
                    emailAddresses.add(emailAddress);
                }
                updatePerson.setEmailAddresses(emailAddresses);
            }

            // Update phones if provided
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                List<PhoneNumber> phoneNumbers = new ArrayList<>();
                for (String phone : phoneNumber) {
                    PhoneNumber personPhone = new PhoneNumber();
                    personPhone.setValue(phone);
                    personPhone.setType("mobile");
                    phoneNumbers.add(personPhone);
                }
                updatePerson.setPhoneNumbers(phoneNumbers);
            }

            // Determine which fields to update
            List<String> updatePersonFields = new ArrayList<>();
            if (displayName != null && !displayName.isEmpty()) updatePersonFields.add("names");
            if (email != null && !email.isEmpty()) updatePersonFields.add("emailAddresses");
            if (phoneNumber != null && !phoneNumber.isEmpty()) updatePersonFields.add("phoneNumbers");

            // Validate that at least one field is being updated
            if (updatePersonFields.isEmpty()) {
                throw new RuntimeException("No fields provided for update.");
            }

            // Perform the update
            googleContactsService.updateContact(principal, "people/" + contactId, updatePerson, updatePersonFields);
            redirectAttributes.addFlashAttribute("success", "Contact updated successfully.");
            return "redirect:/contacts"; // Redirect to contacts list after successful update

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update contact: " + e.getMessage());
            return "redirect:/contacts/edit/people/" + contactId;
        }
    }

    @DeleteMapping("/contacts/delete/{contactId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteContactAjax(
            @PathVariable String contactId,
            @AuthenticationPrincipal OAuth2User principal) {
        Map<String, String> response = new HashMap<>();
        try {
            googleContactsService.deleteContact(principal, "people/" + contactId);
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
            @AuthenticationPrincipal OAuth2User principal,
            RedirectAttributes redirectAttributes) {
        try {
            googleContactsService.deleteContact(principal, "people/" + contactId);
            redirectAttributes.addFlashAttribute("success", "Contact deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete contact: " + e.getMessage());
        }
        return "redirect:/contacts";
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Return the login view
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        request.logout();  // Invalidate the session
        return "redirect:/login";  // Redirect to the login page
    }
}