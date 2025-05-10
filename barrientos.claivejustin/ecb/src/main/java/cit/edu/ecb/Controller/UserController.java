package cit.edu.ecb.Controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;  
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.google.api.services.people.v1.model.Person;

import cit.edu.ecb.Services.ContactService;


@Controller
public class UserController {

    @Autowired
    private ContactService contactService;  

 
    @GetMapping("/user-info")
    @ResponseBody  // This ensures JSON response instead of Thymeleaf rendering
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User principal) {
        return principal != null ? principal.getAttributes() : Map.of("error", "User not authenticated");
    }


    @GetMapping("/secured")
    public String secured() {
        return "Hello secure";  
    }

    @GetMapping("/homepage")
public String homepage() {
    return "homepage";      
}

     @GetMapping("/contacts/add")
    public String showAddContactForm(Model model) {
        model.addAttribute("contact", new Person()); // Assuming 'Person' is your contact model
        return "add-contact"; // This should match the HTML file name
    }

    @PostMapping("/add")
    public String addContact(@AuthenticationPrincipal OAuth2User user,
                             @RequestParam String name,
                             @RequestParam String email,
                             @RequestParam(required = false) String phone,
                             Model model) {
        if (user == null) {
            return "redirect:/login";
        }

        try {
            contactService.addContact(user, name, email, phone);
            model.addAttribute("message", "Contact added successfully!");
        } catch (IOException e) {
            System.out.println(" Error adding contact: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to add contact.");
            return "error"; // Ensure you have error.html to display failures
        }

        return "redirect:/contacts"; 
    }
    @PostMapping("/contacts/update")
    public String updateContact(@AuthenticationPrincipal OAuth2User user,
    @RequestParam String resourceName,
    @RequestParam String name,
    @RequestParam(required = false) String email,
    @RequestParam(required = false) String phone) throws IOException {

if (name == null || name.trim().isEmpty()) {
throw new IllegalArgumentException("Name is required.");
}

String emailValue = (email != null && !email.trim().isEmpty()) ? email : null;
String phoneValue = (phone != null && !phone.trim().isEmpty()) ? phone : null;

contactService.updateContact(user, resourceName, name, emailValue, phoneValue);

return "redirect:/contacts"; 
}

@PostMapping("/contacts/delete")
public String deleteContact(@AuthenticationPrincipal OAuth2User user,
                            @RequestParam String resourceName) throws IOException {
    
    contactService.deleteContact(user, resourceName);

    return "redirect:/contacts"; 
}


    

   
    @GetMapping("/contacts")
    public String getGoogleContacts(@AuthenticationPrincipal OAuth2User user, Model model) throws IOException {
        System.out.println("Fetching contacts..."); 

        if (user == null) {
            System.out.println("User is null, redirecting to login...");
            return "redirect:/login";
        }

        List<Person> contacts = contactService.getContacts(user);
        System.out.println("Contacts fetched: " + contacts.size()); 

        model.addAttribute("contacts", contacts);
        return "contacts"; 
    }
    

    @GetMapping("/Put")
    public String Update() {
        return "Hello secure";  
    }

    @GetMapping("/Delete")
    public String Delete() {
        return "Hello secure";  
    }
}
