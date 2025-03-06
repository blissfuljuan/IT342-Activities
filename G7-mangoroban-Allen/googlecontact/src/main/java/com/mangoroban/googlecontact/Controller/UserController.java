package com.mangoroban.googlecontact.Controller;

import com.mangoroban.googlecontact.Service.GoogleContactsService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {
    @Autowired
    private GoogleContactsService googleContactsService;

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
            String name = oidcUser.getFullName();
            String email = oidcUser.getEmail();
            String pictureUrl = oidcUser.getPicture();
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("pictureUrl", pictureUrl);
        } else if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            String name = oauth2User.getAttribute("name");
            String email = oauth2User.getAttribute("email");
            String pictureUrl = oauth2User.getAttribute("picture");
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("pictureUrl", pictureUrl);
        } else {
            return "redirect:/";
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

    @GetMapping("/contact/add-form")
    public String showAddContactForm(Model model) {
        return "addContact";
    }

    @PostMapping("/contact/add")
    public String addContact(
            @RequestParam("displayName") String name,
            @RequestParam String email,
            @RequestParam(required = false) String phoneNumber,
            @AuthenticationPrincipal OAuth2User principal,
            Model model) {
        try {
            googleContactsService.addContact(principal, name, email, phoneNumber);
            return "redirect:/contacts";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add contact: " + e.getMessage());
            return "addContact";
        }
    }

    @GetMapping("/contacts/edit/people/{contactId}")
    public String editContactForm(
            @PathVariable String contactId,
            @AuthenticationPrincipal OAuth2User principal,
            Model model) {
        try {
            Person contact = googleContactsService.getPersonById(principal, "people/" + contactId);
            if (contact == null) {
                throw new RuntimeException("Contact not found.");
            }
            model.addAttribute("contact", contact);
            return "editContact";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Failed to load contact: " + e.getMessage());
            return "redirect:/contacts";
        }
    }

    @PostMapping("/contacts/edit/people/{contactId}")
    public String updateContact(
            @PathVariable String contactId,
            @RequestParam String displayName,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @AuthenticationPrincipal OAuth2User principal,
            Model model) {
        try {
            Person existingContact = googleContactsService.getPersonById(principal, "people/" + contactId);
            if (existingContact == null) {
                throw new RuntimeException("Contact not found.");
            }
            Person updatePerson = new Person();
            updatePerson.setEtag(existingContact.getEtag());
            if (!displayName.isEmpty()) updatePerson.setNames(Arrays.asList(new Name().setDisplayName(displayName)));
            if (!email.isEmpty()) updatePerson.setEmailAddresses(Arrays.asList(new EmailAddress().setValue(email)));
            if (!phoneNumber.isEmpty()) updatePerson.setPhoneNumbers(Arrays.asList(new PhoneNumber().setValue(phoneNumber)));
            List<String> updatePersonFields = new ArrayList<>();
            if (!displayName.isEmpty()) updatePersonFields.add("names");
            if (!email.isEmpty()) updatePersonFields.add("emailAddresses");
            if (!phoneNumber.isEmpty()) updatePersonFields.add("phoneNumbers");
            if (updatePersonFields.isEmpty()) {
                throw new RuntimeException("No fields provided for update.");
            }
            googleContactsService.updateContact(principal, "people/" + contactId, updatePerson, updatePersonFields);
            return "redirect:/contacts";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Failed to update contact: " + e.getMessage());
            return "editContact";
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
            @AuthenticationPrincipal OAuth2User principal) {
        try {
            googleContactsService.deleteContact(principal, "people/" + contactId);
        } catch (Exception e) {
            System.err.println("Error deleting contact: " + e.getMessage());
        }
        return "redirect:/contacts";
    }
}
