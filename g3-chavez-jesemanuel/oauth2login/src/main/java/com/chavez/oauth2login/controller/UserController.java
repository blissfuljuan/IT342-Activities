package com.chavez.oauth2login.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.chavez.oauth2login.model.Contact;
import com.chavez.oauth2login.repository.ContactRepository;

@Controller
public class UserController {

    @Autowired
    private ContactRepository contactRepository;

    @GetMapping
    public String index() {
        return "Hello World";
    }


    @GetMapping("/user-info")
    @ResponseBody
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User oAuth2User) {
        return oAuth2User.getAttributes();
    }

    @GetMapping("/googleuser")
    public String getUserInfo(Model model, OAuth2AuthenticationToken authentication) {
        OAuth2User user = authentication.getPrincipal();
        
        // Create a Contact object with the user's information
        Contact contact = new Contact(
            user.getAttribute("sub"),  // Add userId as first parameter
            user.getAttribute("name"),
            user.getAttribute("email"),
            "Not provided" // Default phone number since Google doesn't provide it
        );

        model.addAttribute("name", contact.getName());
        model.addAttribute("email", contact.getEmail());
        model.addAttribute("phone", contact.getPhone());
        model.addAttribute("picture", user.getAttribute("picture"));

        return "userinfo";
    }

    @GetMapping("/secured")
    public String secured() {
        return "Secured";
    }

    @GetMapping("/contacts")
    @ResponseBody
    public List<Contact> getAllContacts(@AuthenticationPrincipal OAuth2User principal) {
        String userId = principal.getAttribute("sub");
        return contactRepository.findByUserId(userId);
    }

    @PostMapping("/contacts")
    @ResponseBody
    public Contact addContact(@AuthenticationPrincipal OAuth2User principal, @RequestBody Contact contact) {
        contact.setUserId(principal.getAttribute("sub"));
        return contactRepository.save(contact);
    }

    @PutMapping("/contacts/{id}")
    @ResponseBody
    public ResponseEntity<Contact> updateContact(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable Long id,
            @RequestBody Contact contactDetails) {
        
        return contactRepository.findById(id)
            .map(contact -> {
                if (!contact.getUserId().equals(principal.getAttribute("sub"))) {
                    return ResponseEntity.status(403).<Contact>build();
                }
                contact.setName(contactDetails.getName());
                contact.setEmail(contactDetails.getEmail());
                contact.setPhone(contactDetails.getPhone());
                Contact savedContact = contactRepository.save(contact);
                return ResponseEntity.ok(savedContact);
            })
            .orElse(ResponseEntity.notFound().<Contact>build());
    }

    @DeleteMapping("/contacts/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteContact(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable Long id) {
        
        Optional<Contact> contactOpt = contactRepository.findById(id);
        if (contactOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Contact contact = contactOpt.get();
        if (!contact.getUserId().equals(principal.getAttribute("sub"))) {
            return ResponseEntity.status(403).build();
        }
        
        contactRepository.delete(contact);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user-profile")
    @ResponseBody
    public Contact getUserProfile(@AuthenticationPrincipal OAuth2User principal) {
        String userId = principal.getAttribute("sub");
        return contactRepository.findByUserId(userId)
                .stream()
                .findFirst()
                .orElse(new Contact(userId, 
                        principal.getAttribute("name"),
                        principal.getAttribute("email"),
                        "Not provided"));
    }

    @PostMapping("/user-profile")
    @ResponseBody
    public Contact updateUserProfile(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody Contact profileData) {
        String userId = principal.getAttribute("sub");
        Contact existingProfile = contactRepository.findByUserId(userId)
                .stream()
                .findFirst()
                .orElse(new Contact(userId,
                        principal.getAttribute("name"),
                        principal.getAttribute("email"),
                        "Not provided"));
        
        existingProfile.setLocalPhoneNumber(profileData.getLocalPhoneNumber());
        existingProfile.setAlternativeEmail(profileData.getAlternativeEmail());
        existingProfile.setAddress(profileData.getAddress());
        existingProfile.setNotes(profileData.getNotes());
        
        return contactRepository.save(existingProfile);
    }
}