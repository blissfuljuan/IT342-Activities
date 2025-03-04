package com.googlecontactsapi.googlecontactsapi.controller;

import com.google.api.services.people.v1.model.Person;
import com.googlecontactsapi.googlecontactsapi.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @GetMapping
    public String listContacts(Model model, OAuth2AuthenticationToken authentication) throws IOException {
        List<Person> contacts = contactService.getAllContacts(authentication);
        model.addAttribute("contacts", contacts);
        return "contacts"; // Thymeleaf template name
    }

    @PostMapping("/add")
    public String addContact(@ModelAttribute Person person, OAuth2AuthenticationToken authentication, Model model) throws IOException {
        List<Person> contacts = contactService.addContact(authentication, person);
        model.addAttribute("contacts", contacts);
        return "redirect:/contacts";
    }

    @PostMapping("/update/{resourceName}")
    public String updateContact(@PathVariable String resourceName, @ModelAttribute Person person, OAuth2AuthenticationToken authentication, Model model) throws IOException {
        List<Person> contacts = contactService.updateContact(authentication, resourceName, person);
        model.addAttribute("contacts", contacts);
        return "contacts";
    }

    @PostMapping("/delete/{resourceName}")
    public String deleteContact(@PathVariable String resourceName, OAuth2AuthenticationToken authentication, Model model) throws IOException {
        List<Person> contacts = contactService.deleteContact(authentication, resourceName);
        model.addAttribute("contacts", contacts);
        return "contacts";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Model model, Exception ex) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }
}