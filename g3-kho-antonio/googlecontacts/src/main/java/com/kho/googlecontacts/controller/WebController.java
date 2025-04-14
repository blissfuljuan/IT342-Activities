package com.kho.googlecontacts.controller;

import com.google.api.services.people.v1.model.Person;
import com.kho.googlecontacts.service.GoogleContactsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class WebController {

    private final GoogleContactsService googleContactsService;

    public WebController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping("/contacts")
    public String showContacts(
            Model model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String search) {
        try {
            if (search != null && !search.isEmpty()) {
                // Fetch all contacts across pages
                List<Person> allContacts = new ArrayList<>();
                String pageToken = null;
                do {
                    List<Person> contactsPage = googleContactsService.getContacts(size, pageToken);
                    allContacts.addAll(contactsPage);
                    pageToken = googleContactsService.getNextPageToken(size, pageToken);
                } while (pageToken != null);

                // Filter contacts by search term (case-insensitive)
                String searchLower = search.toLowerCase();
                List<Person> filteredContacts = allContacts.stream().filter(person -> {
                    if (person.getNames() != null && !person.getNames().isEmpty()) {
                        String name = person.getNames().get(0).getDisplayName();
                        if (name != null && name.toLowerCase().contains(searchLower))
                            return true;
                    }
                    if (person.getEmailAddresses() != null && !person.getEmailAddresses().isEmpty()) {
                        String email = person.getEmailAddresses().get(0).getValue();
                        if (email != null && email.toLowerCase().contains(searchLower))
                            return true;
                    }
                    if (person.getPhoneNumbers() != null && !person.getPhoneNumbers().isEmpty()) {
                        String phone = person.getPhoneNumbers().get(0).getValue();
                        if (phone != null && phone.toLowerCase().contains(searchLower))
                            return true;
                    }
                    return false;
                }).toList();

                // Custom sort the filtered contacts
                List<Person> sortedContacts = customSortContacts(filteredContacts);

                // Manual pagination of filtered results
                int total = sortedContacts.size();
                int fromIndex = (page - 1) * size;
                int toIndex = Math.min(fromIndex + size, total);
                List<Person> paginatedContacts = new ArrayList<>();
                if (fromIndex < total) {
                    paginatedContacts = sortedContacts.subList(fromIndex, toIndex);
                }

                model.addAttribute("contacts", paginatedContacts);
                model.addAttribute("currentPage", page);
                model.addAttribute("hasPreviousPage", page > 1);
                model.addAttribute("hasNextPage", toIndex < total);
                model.addAttribute("search", search);
            } else {
                // Normal pagination without search (use People API pagination)
                String pageToken = null;
                if (page > 1) {
                    for (int i = 1; i < page; i++) {
                        pageToken = googleContactsService.getNextPageToken(size, pageToken);
                        if (pageToken == null) {
                            break;
                        }
                    }
                }
                List<Person> contacts = googleContactsService.getContacts(size, pageToken);
                List<Person> sortedContacts = customSortContacts(contacts);
                String nextPageToken = googleContactsService.getNextPageToken(size, pageToken);

                model.addAttribute("contacts", sortedContacts);
                model.addAttribute("currentPage", page);
                model.addAttribute("hasPreviousPage", page > 1);
                model.addAttribute("hasNextPage", nextPageToken != null);
                model.addAttribute("search", search);
            }
            return "contacts";
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to fetch contacts.");
            return "error";
        }
    }

    // Helper method to sort contacts (contacts starting with a period first, then alphabetical)
    private List<Person> customSortContacts(List<Person> contacts) {
        List<Person> periodContacts = new ArrayList<>();
        List<Person> normalContacts = new ArrayList<>();

        for (Person person : contacts) {
            if (person.getNames() != null && !person.getNames().isEmpty()) {
                String displayName = person.getNames().get(0).getDisplayName();
                if (displayName != null && displayName.startsWith(".")) {
                    periodContacts.add(person);
                } else {
                    normalContacts.add(person);
                }
            } else {
                normalContacts.add(person);
            }
        }

        Comparator<Person> nameComparator = (p1, p2) -> {
            String name1 = (p1.getNames() != null && !p1.getNames().isEmpty())
                    ? p1.getNames().get(0).getDisplayName() : "";
            String name2 = (p2.getNames() != null && !p2.getNames().isEmpty())
                    ? p2.getNames().get(0).getDisplayName() : "";
            return name1.compareToIgnoreCase(name2);
        };

        periodContacts.sort(nameComparator);
        normalContacts.sort(nameComparator);

        List<Person> result = new ArrayList<>(periodContacts);
        result.addAll(normalContacts);
        return result;
    }

    @PostMapping("/api/contacts/create")
    public ResponseEntity<Void> createContact(
            @RequestParam String givenName,
            @RequestParam String familyName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber) throws IOException {
        Person newContact = googleContactsService.createContact(givenName, familyName, email, phoneNumber);
        System.out.println("Contact created: " + newContact.getResourceName());
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/contacts"))
                .build();
    }

    @PostMapping("/api/contacts/update")
    public String updateContact(
            @RequestParam String resourceName,
            @RequestParam String givenName,
            @RequestParam String familyName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber) {
        try {
            googleContactsService.updateContact(resourceName, givenName, familyName, email, phoneNumber);
            System.out.println("Contact updated: " + resourceName);
            return "redirect:/contacts";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @PostMapping("/api/contacts/delete")
    public String deleteContact(@RequestParam String resourceName) {
        try {
            googleContactsService.deleteContact(resourceName);
            System.out.println("Deleted contact: " + resourceName);
            return "redirect:/contacts";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }
}