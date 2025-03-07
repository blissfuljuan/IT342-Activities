package com.pepitoJP.midterm.Controller;

import com.google.api.services.people.v1.model.Person;
import com.pepitoJP.midterm.Model.Contact;
import com.pepitoJP.midterm.Service.GoogleContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/contact")
public class ContactController {

    @Autowired
    private GoogleContactsService googleContactsService;

    // ---------- Add Contact ----------

    // Show the Add Contact form
    @GetMapping("/add")
    public String showAddContactForm(Model model) {
        model.addAttribute("contact", new Contact());
        return "addContacts"; // Ensure you have addContacts.html in your templates
    }

    // Process Add Contact form submission
    @PostMapping("/add")
    public String addContact(@ModelAttribute Contact contact,
                             @AuthenticationPrincipal OAuth2User oAuth2User,
                             Model model) {
        try {
            Person created = googleContactsService.addContactWithPeopleService(oAuth2User, contact);
            // Optionally, you can add created contact details to model if needed.
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add contact: " + e.getMessage());
        }
        return "redirect:/contacts"; // After adding, redirect to contacts page.
    }

    // ---------- Update Contact ----------

    // Show the Update Contact form (pre-populated)
    @GetMapping("/update/{resourceName}")
    public String showUpdateContactForm(@PathVariable String resourceName,
                                        @AuthenticationPrincipal OAuth2User oAuth2User,
                                        Model model) {
        // For demonstration purposes, we'll assume you already have the contact details.
        // In a real application, you may want to fetch the specific contact's details.
        Contact contact = new Contact();
        contact.setResourceName(resourceName);
        // Pre-populate contact if you have the data (or fetch from service if possible)
        model.addAttribute("contact", contact);
        return "updateContact"; // Ensure you have updateContacts.html in your templates
    }

    // Process Update Contact form submission
    @PostMapping("/update/{resourceName}")
    public String updateContact(@PathVariable String resourceName,
                                @ModelAttribute Contact contact,
                                @AuthenticationPrincipal OAuth2User oAuth2User,
                                Model model) {
        try {
            Person updated = googleContactsService.updateContactWithPeopleService(oAuth2User, resourceName, contact);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update contact: " + e.getMessage());
        }
        return "redirect:/contacts"; // Redirect back to contacts page.
    }

    // ---------- Delete Contact ----------

    // Process Delete Contact
    @PostMapping("/delete/{resourceName}")
    public String deleteContact(@PathVariable String resourceName,
                                @AuthenticationPrincipal OAuth2User oAuth2User,
                                Model model) {
        try {
            googleContactsService.deleteContactWithPeopleService(oAuth2User, resourceName);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete contact: " + e.getMessage());
        }
        return "redirect:/contacts"; // Redirect after deletion.
    }
}
