package com.canal.GoogleContact.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Map;

@Controller
public class ContactsController {

    @GetMapping("/contacts")
    public String showContactsPage(Model model) {
        List<Map<String, String>> contacts = List.of(
                Map.of("name", "John Doe", "email", "john@example.com", "phone", "1234567890"),
                Map.of("name", "Jane Smith", "email", "jane@example.com", "phone", "9876543210")
        );

        model.addAttribute("contacts", contacts);
        return "contacts"; // Refers to contacts.html inside templates/
    }
}
