package com.googlecontactsapi.googlecontactsapi.controller;

import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.googlecontactsapi.googlecontactsapi.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @GetMapping
    public String listContacts(Model model, @AuthenticationPrincipal OAuth2User principal) throws IOException, GeneralSecurityException {
        List<Person> contacts = contactService.getAllContactsWithResourceName(principal.getName());
        model.addAttribute("contacts", contacts);
        return "contacts"; // Thymeleaf template name
    }

    @PostMapping("/add")
    public String addContact(@ModelAttribute Person person, @AuthenticationPrincipal OAuth2User principal) throws IOException, GeneralSecurityException {
        contactService.addContact(principal.getName(), person);
        return "redirect:/contacts";
    }

    @PostMapping("/updateContact")
    public String updateContact(@RequestParam("givenName") String givenName,
                                @RequestParam("familyName") String familyName,
                                @RequestParam("email") String email,
                                @RequestParam("phoneNumber") String phoneNumber,
                                @RequestParam("resourceName") String resourceName,
                                @AuthenticationPrincipal OAuth2User principal) throws IOException, GeneralSecurityException {
        Person person = new Person();

        List<Name> names = new ArrayList<>();
        names.add(new Name().setGivenName(givenName).setFamilyName(familyName));
        person.setNames(names);

        List<EmailAddress> emails = new ArrayList<>();
        emails.add(new EmailAddress().setValue(email));
        person.setEmailAddresses(emails);

        List<PhoneNumber> phones = new ArrayList<>();
        phones.add(new PhoneNumber().setValue(phoneNumber));
        person.setPhoneNumbers(phones);

        contactService.updateContact(principal.getName(), resourceName, person);

        return "redirect:/contacts";
    }

    @PostMapping("/deleteContact")
    public String deleteContact(@RequestParam("resourceName") String resourceName, @AuthenticationPrincipal OAuth2User principal) throws IOException, GeneralSecurityException {
        contactService.deleteContact(principal.getName(), resourceName);
        return "redirect:/contacts";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Model model, Exception ex) {
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("exception", ex != null ? ex : new Exception("Unknown error"));
        return "error";
    }
}