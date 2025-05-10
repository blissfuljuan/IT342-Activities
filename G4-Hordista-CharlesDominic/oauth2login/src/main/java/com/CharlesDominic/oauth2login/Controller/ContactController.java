package com.CharlesDominic.oauth2login.Controller;

import com.CharlesDominic.oauth2login.Service.CredentialService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.google.api.client.json.jackson2.JacksonFactory;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/contacts")
public class ContactController {

    private final CredentialService credentialService;

    public ContactController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @GetMapping
    public String getContacts(OAuth2AuthenticationToken authentication, Model model) throws Exception {
        Credential credential = credentialService.getGoogleCredential(authentication);

        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Fundnote").build();

        ListConnectionsResponse response = peopleService.people().connections()
                .list("people/me")
                .setPageSize(10)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        List<Person> connections = response.getConnections();

        List<String> contacts = connections != null ? connections.stream()
                .map(person -> {
                    String name = person.getNames() != null && !person.getNames().isEmpty()
                            ? person.getNames().get(0).getDisplayName()
                            : "No Name";
                    String email = person.getEmailAddresses() != null && !person.getEmailAddresses().isEmpty()
                            ? person.getEmailAddresses().get(0).getValue()
                            : "No Email";
                    String phone = person.getPhoneNumbers() != null && !person.getPhoneNumbers().isEmpty()
                            ? person.getPhoneNumbers().get(0).getValue()
                            : "No Number";
                    String resourceName = person.getResourceName();
                    return name + " - " + email + " - " + phone + " - " + resourceName;
                })
                .collect(Collectors.toList()) : List.of();

        model.addAttribute("contacts", contacts);
        return "contacts";
    }

    @GetMapping("/new")
    public String showCreateContactForm(Model model) {
        model.addAttribute("contact", new Person());
        return "create-contact";
    }

    @PostMapping("/newContact")
    public String createContact(@RequestParam String name, @RequestParam String email, @RequestParam String phone,
                                OAuth2AuthenticationToken authentication) throws Exception {
        Credential credential = credentialService.getGoogleCredential(authentication);

        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Fundnote").build();

        Person contact = new Person()
                .setNames(List.of(new Name().setGivenName(name)))
                .setEmailAddresses(List.of(new EmailAddress().setValue(email)))
                .setPhoneNumbers(List.of(new PhoneNumber().setValue(phone)));

        peopleService.people().createContact(contact).execute();

        return "redirect:/contacts";
    }

    // DELETE CONTACT FUNCTION
    @PostMapping("/delete")
    public String deleteContact(@RequestParam String resourceName, OAuth2AuthenticationToken authentication) throws Exception {
        Credential credential = credentialService.getGoogleCredential(authentication);

        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Fundnote").build();

        peopleService.people().deleteContact(resourceName).execute();

        return "redirect:/contacts";
    }

    // UPDATE CONTACT - Show Edit Form
    @GetMapping("/edit")
    public String showEditContactForm(@RequestParam String resourceName, @RequestParam String name,
                                      @RequestParam String email, @RequestParam String phone, Model model) {
        model.addAttribute("resourceName", resourceName);
        model.addAttribute("name", name);
        model.addAttribute("email", email);
        model.addAttribute("phone", phone);
        return "edit-contact";
    }

    // UPDATE CONTACT - Process Update
    @PostMapping("/update")
    public String updateContact(@RequestParam String resourceName, @RequestParam String name,
                                @RequestParam String email, @RequestParam String phone,
                                OAuth2AuthenticationToken authentication, Model model) {
        try {
            Credential credential = credentialService.getGoogleCredential(authentication);

            PeopleService peopleService = new PeopleService.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName("Fundnote").build();

            // 🔹 Retrieve existing contact to get etag
            Person existingContact = peopleService.people().get(resourceName)
                    .setPersonFields("names,emailAddresses,phoneNumbers,metadata")
                    .execute();

            if (existingContact == null || existingContact.getEtag() == null) {
                model.addAttribute("error", "Failed to retrieve contact etag.");
                return "error";
            }

            // 🔹 Create updated contact with etag
            Person updatedContact = new Person()
                    .setEtag(existingContact.getEtag())  // Required for updates
                    .setNames(List.of(new Name().setGivenName(name)))
                    .setEmailAddresses(List.of(new EmailAddress().setValue(email)))
                    .setPhoneNumbers(List.of(new PhoneNumber().setValue(phone)));

            // 🔹 Execute update request
            peopleService.people().updateContact(resourceName, updatedContact)
                    .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                    .execute();

            return "redirect:/contacts";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error updating contact: " + e.getMessage());
            return "error";
        }
    }


}
