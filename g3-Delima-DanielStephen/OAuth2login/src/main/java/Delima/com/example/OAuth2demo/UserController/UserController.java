package Delima.com.example.OAuth2demo.UserController;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import org.springframework.security.oauth2.core.user.OAuth2User;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import org.springframework.http.*;

import org.springframework.web.client.RestTemplate;
import Delima.com.example.OAuth2demo.DTO.RegisterRequest;

import java.util.*;

@Controller

public class UserController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public UserController(OAuth2AuthorizedClientService authorizedClientService) {

        this.authorizedClientService = authorizedClientService;

    }

    @GetMapping("/")

    public String index() {

        return "index"; // Serves index.html

    }

    @GetMapping("/user-info")

    public String getUser(@AuthenticationPrincipal OAuth2User oAuth2User, Model model) {

        if (oAuth2User != null) {

            model.addAttribute("name", oAuth2User.getAttribute("name"));

            model.addAttribute("firstName", oAuth2User.getAttribute("given_name"));

            model.addAttribute("lastName", oAuth2User.getAttribute("family_name"));

            model.addAttribute("email", oAuth2User.getAttribute("email"));

        }

        return "user-info";

    }

    @GetMapping("/contacts")

    public String getContacts(Model model, OAuth2AuthenticationToken authentication) {

        String accessToken = getAccessToken(authentication);

        // Call Google People API to get contacts

        String url = "https://people.googleapis.com/v1/people/me/connections"

                + "?personFields=names,emailAddresses,phoneNumbers"

                + "&access_token=" + accessToken;

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        List<Map<String, String>> contactsList = new ArrayList<>();

        if (response != null && response.containsKey("connections")) {

            List<Map<String, Object>> connections = (List<Map<String, Object>>) response.get("connections");

            for (Map<String, Object> person : connections) {

                String name = person.containsKey("names") ?

                        ((List<Map<String, Object>>) person.get("names")).get(0).get("displayName").toString() :

                        "Unknown";

                String email = person.containsKey("emailAddresses") ?

                        ((List<Map<String, Object>>) person.get("emailAddresses")).get(0).get("value").toString() :

                        "N/A";

                String phone = person.containsKey("phoneNumbers") ?

                        ((List<Map<String, Object>>) person.get("phoneNumbers")).get(0).get("value").toString() :

                        "N/A";

                contactsList.add(Map.of("name", name, "email", email, "phone", phone));

            }

        }

        model.addAttribute("contacts", contactsList);

        return "contacts"; // Returns contacts.html

    }

    // 🔹 Helper Method: Get Access Token

    private String getAccessToken(OAuth2AuthenticationToken authentication) {

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(

                authentication.getAuthorizedClientRegistrationId(),

                authentication.getName()

        );

        return client.getAccessToken().getTokenValue();

    }

    // 🔹 Create Contact

    @PostMapping("/contacts/create")

    public String createContact(OAuth2AuthenticationToken authentication) {

        String accessToken = getAccessToken(authentication);

        String url = "https://people.googleapis.com/v1/people:createContact?access_token=" + accessToken;

        Map<String, Object> requestBody = Map.of(

                "names", List.of(Map.of("givenName", "New", "familyName", "Contact")),

                "emailAddresses", List.of(Map.of("value", "new.contact@example.com")),

                "phoneNumbers", List.of(Map.of("value", "+1234567890"))

        );

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        return "redirect:/contacts";

    }

    // 🔹 Update Contact

    @PostMapping("/contacts/update")

    public String updateContact(OAuth2AuthenticationToken authentication, @RequestParam String resourceName) {

        String accessToken = getAccessToken(authentication);

        String url = "https://people.googleapis.com/v1/" + resourceName + "?updatePersonFields=names,emailAddresses,phoneNumbers&access_token=" + accessToken;

        Map<String, Object> requestBody = Map.of(

                "names", List.of(Map.of("givenName", "Updated", "familyName", "Name")),

                "emailAddresses", List.of(Map.of("value", "updated.email@example.com")),

                "phoneNumbers", List.of(Map.of("value", "+9876543210"))

        );

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);

        return "redirect:/contacts";

    }

    // 🔹 Delete Contact

    @PostMapping("/contacts/delete")

    public String deleteContact(OAuth2AuthenticationToken authentication, @RequestParam String resourceName) {

        String accessToken = getAccessToken(authentication);

        String url = "https://people.googleapis.com/v1/" + resourceName + "?access_token=" + accessToken;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

        return "redirect:/contacts";

    }


}

