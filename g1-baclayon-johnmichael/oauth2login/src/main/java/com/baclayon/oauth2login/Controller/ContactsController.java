package com.baclayon.oauth2login.Controller;

import com.baclayon.oauth2login.Service.GoogleContactsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/contacts")
public class ContactsController {

    private final GoogleContactsService googleContactsService;

    @Autowired
    public ContactsController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping
    public String getContacts(Model model, HttpSession session) {
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/login";
        }

        List<Map<String, Object>> contacts = googleContactsService.getContacts(accessToken);
        model.addAttribute("contacts", contacts);
        return "contacts";
    }

    @GetMapping("/add-form")
    public String showAddContactForm() {
        return "add-contact";
    }

    @PostMapping("/add")
    public String addContact(@RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam String email,
                             HttpSession session) {
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/login";
        }

        googleContactsService.addContact(accessToken, firstName, lastName, email);
        return "redirect:/contacts";
    }

    @GetMapping("/edit-form")
    public String showEditContactForm(@RequestParam String resourceName, Model model) {
        model.addAttribute("resourceName", resourceName);
        return "edit-contact";
    }

    @PostMapping("/update")
    public String updateContact(@RequestParam String resourceName,
                                @RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String email,
                                HttpSession session) {
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/login";
        }

        googleContactsService.updateContact(accessToken, resourceName, firstName, lastName, email);
        return "redirect:/contacts";
    }

    @PostMapping("/delete")
    public String deleteContact(@RequestParam String resourceName, HttpSession session) {
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/login";
        }

        googleContactsService.deleteContact(accessToken, resourceName);
        return "redirect:/contacts";
    }
}