package com.ravanes.google.oauth.controller;

import com.ravanes.google.oauth.service.GoogleContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContactsUIController {

    @Autowired
    private GoogleContactsService googleContactsService;

    @GetMapping("/contacts")
    public String viewContacts(Model model) {
        model.addAttribute("contacts", googleContactsService.getContacts());
        return "contacts";
    }
}
