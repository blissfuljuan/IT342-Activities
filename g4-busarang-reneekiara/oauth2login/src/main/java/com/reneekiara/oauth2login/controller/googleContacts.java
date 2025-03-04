package com.reneekiara.oauth2login.controller;

import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.reneekiara.oauth2login.service.googleContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class googleContacts {

    @Autowired
    googleContactsService serv;


    @GetMapping("/contacts")
    public String getContacts(@AuthenticationPrincipal OAuth2User principal, Model model) throws IOException, GeneralSecurityException {

        model.addAttribute("contacts", serv.getContacts(principal.getName()));
        return "contacts";
    }

    @PostMapping("/newContact")
    public String newContact(@RequestParam("givenName") String givenName,
                             @RequestParam("familyName") String familyName,
                             @RequestParam("email") String email,
                             @RequestParam("phoneNumber") String phoneNumber,
                             @AuthenticationPrincipal OAuth2User principal, Model model) throws IOException, GeneralSecurityException {
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

        serv.newContact(person,principal.getName());

        return "redirect:contacts";
    }

    @PostMapping("/deleteContact")
    public String deleteContact(@RequestParam("resourceName") String resourceName, @AuthenticationPrincipal OAuth2User principal) throws IOException, GeneralSecurityException {

        serv.deleteContact(resourceName,principal.getName());

        return "redirect:contacts";
    }

    @PostMapping("/updateContact")
    public String updateContact(@RequestParam("givenName") String givenName,
                             @RequestParam("familyName") String familyName,
                             @RequestParam("email") String email,
                             @RequestParam("phoneNumber") String phoneNumber, @RequestParam("resourceName") String resourceName,
                             @AuthenticationPrincipal OAuth2User principal, Model model) throws IOException, GeneralSecurityException {
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

        serv.updateContact(person,resourceName,principal.getName());

        return "redirect:contacts";
    }

}

