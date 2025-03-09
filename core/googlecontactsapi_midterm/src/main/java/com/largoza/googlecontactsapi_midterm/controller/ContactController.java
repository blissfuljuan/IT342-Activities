package com.largoza.googlecontactsapi_midterm.controller;

// import java.io.IOException;
// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.*;

// import com.google.api.services.people.v1.model.EmailAddress;
// import com.google.api.services.people.v1.model.Name;
// import com.google.api.services.people.v1.model.Person;
// import com.google.api.services.people.v1.model.PhoneNumber;
// import com.largoza.googlecontactsapi_midterm.service.ContactService;

// @Controller
// @RequestMapping("/contacts")
// public class ContactController {

//     @Autowired
//     private ContactService contactService;

//     @GetMapping("/getContacts")
//     public String listContacts(Model model) throws IOException {
//         List<Person> contacts = contactService.listContacts();
//         model.addAttribute("contacts", contacts);

//         return "contacts";
//     }

//     @GetMapping("/getContact/{resourceName}")
//     public Person getContact(@PathVariable String resourceName) throws IOException {
//         return contactService.getContact(resourceName);
//     }

//     @PostMapping("/createContact")
//     public String createContact(@RequestParam String firstName,
//                                 @RequestParam String lastName,
//                                 @RequestParam String email,
//                                 @RequestParam String phone) throws IOException {
//         Person contact = new Person();
//         contact.setNames(List.of(new Name().setGivenName(firstName).setFamilyName(lastName)));
//         contact.setEmailAddresses(List.of(new EmailAddress().setValue(email)));
//         contact.setPhoneNumbers(List.of(new PhoneNumber().setValue(phone)));

//         contactService.createContact(contact);
//         return "redirect:/contacts/getContacts";
//     }

//     @PostMapping("/updateContact/{resourceName}")
//     public String updateContact(@PathVariable String resourceName,
//                                 @RequestParam String firstName,
//                                 @RequestParam String lastName,
//                                 @RequestParam String email,
//                                 @RequestParam String phone) throws IOException {
//         Person contact = new Person();
//         contact.setNames(List.of(new Name().setGivenName(firstName).setFamilyName(lastName)));
//         contact.setEmailAddresses(List.of(new EmailAddress().setValue(email)));
//         contact.setPhoneNumbers(List.of(new PhoneNumber().setValue(phone)));

//         contactService.updateContact(resourceName, contact);
//         return "redirect:/contacts/getContacts";
//     }

//     @PostMapping("/deleteContact/{resourceName}")
//     public String deleteContact(@PathVariable String resourceName) throws IOException {
//         contactService.deleteContact(resourceName);
//         return "redirect:/contacts/getContacts";
//     }
// }

import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.largoza.googlecontactsapi_midterm.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @GetMapping("/getContacts")
    public String listContacts(Model model) throws IOException {
        List<Person> contacts = contactService.listContacts();
        model.addAttribute("contacts", contacts);

        return "contacts";
    }

    @GetMapping("/testGetContacts")
    @ResponseBody
    public List<Person> getContactsTest() throws IOException {
        return contactService.listContacts();
    }

    @GetMapping("/getContact/{resourceName}")
    @ResponseBody
    public Person getContact(@PathVariable String resourceName) throws IOException {
        return contactService.getContact(resourceName);
    }


    @PostMapping("/createContact")
    public String createContact(@RequestParam String firstName, @RequestParam String lastName, @RequestParam String email, @RequestParam String phone) throws IOException {
        Person contact = new Person();

        contact.setNames(List.of(new Name().setGivenName(firstName).setFamilyName(lastName)));
        contact.setEmailAddresses(List.of(new EmailAddress().setValue(email)));
        contact.setPhoneNumbers(List.of(new PhoneNumber().setValue(phone)));

        contactService.createContact(contact);

        return "redirect:/contacts/getContacts";
    }

    @PostMapping("/updateContact/people/{resourceName}")
    public String updateContact(@PathVariable String resourceName, @RequestParam String firstName, @RequestParam String lastName, @RequestParam String email, @RequestParam String phone) throws IOException {
        Person contact = new Person();

        contact.setNames(List.of(new Name().setGivenName(firstName).setFamilyName(lastName)));
        contact.setEmailAddresses(List.of(new EmailAddress().setValue(email)));
        contact.setPhoneNumbers(List.of(new PhoneNumber().setValue(phone)));

        contactService.updateContact("people/" + resourceName, contact);

        return "redirect:/contacts/getContacts";
    }

    @RequestMapping(value="/deleteContact/people/{resourceName}", method= RequestMethod.POST)
    public String deleteContact(@PathVariable String resourceName) throws IOException {
        contactService.deleteContact("people/" + resourceName);
        return "redirect:/contacts/getContacts";
    }
}