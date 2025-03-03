package Agramon.com.example.project.Controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;  
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.api.services.people.v1.model.Person;

import Agramon.com.example.project.Service.ContactService;

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

    @GetMapping("/Post")
    public String Create() {
        return "Hello secure";  
    }

   
    @GetMapping("/contact")
    public String getGoogleContacts(@AuthenticationPrincipal OAuth2User user, Model model) throws IOException {
        System.out.println("Fetching contacts..."); // ✅ Debug log

        if (user == null) {
            System.out.println("User is null, redirecting to login...");
            return "redirect:/login";
        }

        List<Person> contacts = contactService.getContacts(user);
        System.out.println("Contacts fetched: " + contacts.size()); // ✅ Debug log

        model.addAttribute("contacts", contacts);
        return "contacts"; // ✅ Must match contacts.html in templates
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
