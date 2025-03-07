package com.largoza.googlecontactsapi_midterm.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.largoza.googlecontactsapi_midterm.service.ContactService;
// import com.google.api.services.people.v1.PeopleService;

@Controller
@RequestMapping
public class ContactController {

    @Autowired
    private ContactService contactService;

    @GetMapping("/getContacts")
    public String listContacts (Model model) throws IOException {
        List<Person> contacts = contactService.listContacts();
        model.addAttribute("contacts", contacts);

        return "contacts";
    }

    @GetMapping("/getContacts/{resourceName}")
    public Person getContacts(@PathVariable String resourceName) throws IOException {
        return contactService.getContact(resourceName);
    }

    @PostMapping("/createContacts")
    public String createContacts(@PathVariable String resourceName, @RequestParam String firstName, @RequestParam String lastName, @RequestParam String email, @RequestParam String phone) throws IOException {
        Person contact = new Person();

        // Full Name
        contact.setNames(List.of(new Name().setGivenName(firstName).setFamilyName(lastName)));

        // Email Address
        contact.setEmailAddresses(List.of(new EmailAddress().setValue(email)));

        // Phone Number
        contact.setPhoneNumbers(List.of(new PhoneNumber().setValue(phone)));

        contactService.updateContact("people/" + resourceName, contact);

        return "redirect:/contacts/getContacts";
    }

    @RequestMapping(value="/deleteContact/people/{resourceName}", method = RequestMethod.POST)
    public String deleteContact(@PathVariable String resourceName) throws IOException {
        contactService.deleteContact("people/" + resourceName);
        return "redirect:/contacts/getContacts";
    }
}
