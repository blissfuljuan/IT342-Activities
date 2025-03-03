package com.example.gadiane.johnkarl.demolition.controller;

import com.example.gadiane.johnkarl.demolition.model.ContactForm;
import com.example.gadiane.johnkarl.demolition.service.GoogleContactsService;
import com.example.gadiane.johnkarl.demolition.service.GoogleCredentialService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;

@Controller
@RequestMapping("/contacts")
public class ContactsViewController {

    private final GoogleContactsService contactsService;
    private final GoogleCredentialService credentialService;

    public ContactsViewController(GoogleContactsService contactsService, GoogleCredentialService credentialService) {
        this.contactsService = contactsService;
        this.credentialService = credentialService;
    }

    @GetMapping
    public String listContacts(Model model) {
        try {
            Credential credential = credentialService.getCredential();
            model.addAttribute("contacts", contactsService.listContacts(credential));
            return "contacts/list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/{resourceName}")
    public String viewContact(@PathVariable String resourceName, Model model) {
        try {
            Credential credential = credentialService.getCredential();
            model.addAttribute("contact", contactsService.getContact(credential, resourceName));
            return "contacts/view";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/new")
    public String newContactForm(Model model) {
        model.addAttribute("contactForm", new ContactForm());
        return "contacts/form";
    }

    @GetMapping("/edit/{resourceName}")
    public String editContactForm(@PathVariable String resourceName, Model model) {
        try {
            Credential credential = credentialService.getCredential();
            Person person = contactsService.getContact(credential, resourceName);

            ContactForm form = new ContactForm();
            if (person.getNames() != null && !person.getNames().isEmpty()) {
                form.setFirstName(person.getNames().get(0).getGivenName());
                form.setLastName(person.getNames().get(0).getFamilyName());
            }

            if (person.getEmailAddresses() != null && !person.getEmailAddresses().isEmpty()) {
                form.setEmail(person.getEmailAddresses().get(0).getValue());
            }

            model.addAttribute("contactForm", form);
            model.addAttribute("resourceName", resourceName);
            return "contacts/form";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping
    public String saveContact(@ModelAttribute ContactForm contactForm, RedirectAttributes redirectAttributes) {
        try {
            Credential credential = credentialService.getCredential();

            Person person = new Person();

            Name name = new Name()
                    .setGivenName(contactForm.getFirstName())
                    .setFamilyName(contactForm.getLastName());
            person.setNames(Collections.singletonList(name));

            EmailAddress email = new EmailAddress()
                    .setValue(contactForm.getEmail());
            person.setEmailAddresses(Collections.singletonList(email));

            contactsService.createContact(credential, person);
            redirectAttributes.addFlashAttribute("message", "Contact created successfully");
            return "redirect:/contacts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/contacts";
        }
    }

    @PostMapping("/edit/{resourceName}")
    public String updateContact(@PathVariable String resourceName, @ModelAttribute ContactForm contactForm, RedirectAttributes redirectAttributes) {
        try {
            Credential credential = credentialService.getCredential();

            Person person = new Person();

            Name name = new Name()
                    .setGivenName(contactForm.getFirstName())
                    .setFamilyName(contactForm.getLastName());
            person.setNames(Collections.singletonList(name));

            EmailAddress email = new EmailAddress()
                    .setValue(contactForm.getEmail());
            person.setEmailAddresses(Collections.singletonList(email));

            contactsService.updateContact(credential, resourceName, person);
            redirectAttributes.addFlashAttribute("message", "Contact updated successfully");
            return "redirect:/contacts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/contacts";
        }
    }

    @GetMapping("/delete/{resourceName}")
    public String deleteContact(@PathVariable String resourceName, RedirectAttributes redirectAttributes) {
        try {
            Credential credential = credentialService.getCredential();
            contactsService.deleteContact(credential, resourceName);
            redirectAttributes.addFlashAttribute("message", "Contact deleted successfully");
            return "redirect:/contacts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/contacts";
        }
    }
}