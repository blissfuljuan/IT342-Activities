package gelera.googlecontacts.demo.demo.controller;

import com.google.api.services.people.v1.model.Person;
import gelera.googlecontacts.demo.demo.service.GoogleContactService;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final GoogleContactService googleContactsService;

    public ContactController(GoogleContactService googleContactsService){
        this.googleContactsService = googleContactsService;
    }

    @GetMapping
    public List<Person> getContacts() throws IOException {
        List<Person> contacts = googleContactsService.getContacts();
        System.out.println("Fetched Contacts: " + contacts);
        return contacts;
    }
}