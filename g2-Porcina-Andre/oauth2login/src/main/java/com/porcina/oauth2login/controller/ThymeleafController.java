package com.porcina.oauth2login.controller;

import com.porcina.oauth2login.model.Contacts;

import com.porcina.oauth2login.service.GoogleContactsService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

@Controller

@RequestMapping("/thymeleaf")

public class ThymeleafController {

    @Autowired

    private GoogleContactsService contactService;

    @GetMapping

    public String getAllContacts(Model model) {

        model.addAttribute("contacts", contactService.getAllContacts());

        model.addAttribute("newContact", new Contacts()); // Empty object for form binding

        return "contacts";

    }

    @PostMapping("/add")

    public String addContact(@ModelAttribute Contacts contact) {

        contactService.createContact(contact);

        return "redirect:/thymeleaf";

    }

    @PostMapping("/update/{id}")

    public String updateContact(@PathVariable String id, @ModelAttribute Contacts updatedContact) {

        contactService.updateContact(id, updatedContact);

        return "redirect:/thymeleaf";

    }

    @GetMapping("/delete/{id}")

    public String deleteContact(@PathVariable String id) {

        contactService.deleteContact(id);

        return "redirect:/thymeleaf";

    }

}


