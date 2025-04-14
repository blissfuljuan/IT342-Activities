package com.paraiso.GoogleContacts.controller;

import com.paraiso.GoogleContacts.service.GoogleContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/contacts", "/contact"})
public class ContactController {

    @Autowired
    private GoogleContactsService googleContactsService;

    @GetMapping
    public String listContacts(@AuthenticationPrincipal OAuth2User principal, Model model) {
        try {
            model.addAttribute("contacts", googleContactsService.getContacts());
            return "contacts";
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching contacts: " + e.getMessage());
            return "contacts";
        }
    }

    @GetMapping("/new")
    public String showNewContactForm(Model model) {
        return "addContact";
    }

    @PostMapping("/new")
    public String createContact(@RequestParam String name,
                              @RequestParam String email,
                              @RequestParam(required = false) String phoneNumber,
                              RedirectAttributes redirectAttributes) {
        try {
            googleContactsService.createContact(name, email, phoneNumber);
            redirectAttributes.addFlashAttribute("message", "Contact created successfully!");
            return "redirect:/contacts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating contact: " + e.getMessage());
            return "redirect:/contacts/new";
        }
    }

    @GetMapping("/edit/people/{contactId}")
    public String showEditForm(@PathVariable String contactId, Model model) {
        try {
            model.addAttribute("contact", googleContactsService.getContactById("people/" + contactId));
            return "editContact";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading contact: " + e.getMessage());
            return "redirect:/contacts";
        }
    }

    @PostMapping("/edit/people/{contactId}")
    public String updateContact(@PathVariable String contactId,
                              @RequestParam String name,
                              @RequestParam String email,
                              @RequestParam(required = false) String phoneNumber,
                              RedirectAttributes redirectAttributes) {
        try {
            googleContactsService.updateContact("people/" + contactId, name, email, phoneNumber);
            redirectAttributes.addFlashAttribute("message", "Contact updated successfully!");
            return "redirect:/contacts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating contact: " + e.getMessage());
            return "redirect:/contacts/edit/people/" + contactId;
        }
    }

    @GetMapping("/delete/people/{contactId}")
    public String deleteContact(@PathVariable String contactId, RedirectAttributes redirectAttributes) {
        try {
            googleContactsService.deleteContact("people/" + contactId);
            redirectAttributes.addFlashAttribute("message", "Contact deleted successfully!");
            return "redirect:/contacts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting contact: " + e.getMessage());
            return "redirect:/contacts";
        }
    }
} 