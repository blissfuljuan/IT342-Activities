package com.CharlesDominic.oauth2login.Controller;

import com.CharlesDominic.oauth2login.Service.CredentialService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

        // Retrieve Google Contacts
        ListConnectionsResponse response = peopleService.people().connections()
                .list("people/me")
                .setPageSize(10)
                .setPersonFields("names,emailAddresses,phoneNumbers") // Include phoneNumbers
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
                    return name + " - " + email + " - " + phone;
                })
                .collect(Collectors.toList()) : List.of();

        model.addAttribute("contacts", contacts);
        return "contacts";
    }

    // Show form to create contact
    @GetMapping("/new")
    public String showCreateContactForm(Model model) {
        model.addAttribute("contact", new Person());
        return "create-contact";
    }

    // Handle form submission for creating a contact
    @PostMapping("/contacts")
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
}
