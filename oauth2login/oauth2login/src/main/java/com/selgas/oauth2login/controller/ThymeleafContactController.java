package com.selgas.oauth2login.controller;

import com.selgas.oauth2login.Model.Contact;
import com.selgas.oauth2login.service.GoogleContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/thymeleaf")
public class ThymeleafContactController {

    @Autowired
    private GoogleContactService contactService;

    @GetMapping
    public String getAllContacts(Model model) {
        model.addAttribute("contacts", contactService.getAllContacts());
        model.addAttribute("newContact", new Contact()); // Empty object for form binding
        return "contacts";
    }

    @PostMapping("/add")
    public String addContact(@ModelAttribute Contact contact) {
        contactService.createContact(contact);
        return "redirect:/thymeleaf";
    }

    @PostMapping("/update/{id}")
    public String updateContact(@PathVariable String id, @ModelAttribute Contact updatedContact) {
        contactService.updateContact(id, updatedContact);
        return "redirect:/thymeleaf";
    }

    @GetMapping("/delete/{id}")
    public String deleteContact(@PathVariable String id) {
        contactService.deleteContact(id);
        return "redirect:/thymeleaf";
    }
}