package com.donaire.OAth2Login.Controller;

import com.donaire.OAth2Login.Service.ContactService;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    // Retrieve all contacts
    @GetMapping
    public List<String> getAllContacts() throws IOException {
        return contactService.getContacts();
    }

    // Add a new contact
    @PostMapping
    public String addContact(@RequestParam String name, @RequestParam String email) throws IOException {
        return contactService.addContact(name, email);
    }

    // Modify an existing contact
    @PutMapping("/{contactId}")
    public String updateContact(@PathVariable String contactId, @RequestParam String name) throws IOException {
        return contactService.updateContact(contactId, name);
    }

    // Delete a contact
    @DeleteMapping("/{contactId}")
    public String deleteContact(@PathVariable String contactId) throws IOException {
        return contactService.deleteContact(contactId);
    }
}
