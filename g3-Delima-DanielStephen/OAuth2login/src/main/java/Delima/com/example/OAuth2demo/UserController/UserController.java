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
        try {
            String accessToken = getAccessToken(authentication);

            // Remove resourceName from personFields
            String url = "https://people.googleapis.com/v1/people/me/connections"
                    + "?personFields=names,emailAddresses,phoneNumbers"
                    + "&access_token=" + accessToken;

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);

            List<Map<String, String>> contactsList = new ArrayList<>();

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                Map<String, Object> response = responseEntity.getBody();
                if (response.containsKey("connections")) {
                    List<Map<String, Object>> connections = (List<Map<String, Object>>) response.get("connections");

                    for (Map<String, Object> person : connections) {
                        String name = extractFirstValue(person, "names", "displayName", "Unknown");
                        String email = extractFirstValue(person, "emailAddresses", "value", "N/A");
                        String phone = extractFirstValue(person, "phoneNumbers", "value", "N/A");

                        // The resourceName is a top-level field, so fetch it separately
                        String resourceName = person.containsKey("resourceName") ? person.get("resourceName").toString() : "N/A";

                        contactsList.add(Map.of(
                                "resourceName", resourceName, // Include resourceName
                                "name", name,
                                "email", email,
                                "phone", phone
                        ));
                    }
                }
            }

            model.addAttribute("contacts", contactsList);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch contacts: " + e.getMessage());
        }

        return "contacts"; // Returns contacts.html
    }


    // Utility method to safely extract first value
    private String extractFirstValue(Map<String, Object> person, String field, String subField, String defaultValue) {
        if (person.containsKey(field)) {
            List<Map<String, Object>> values = (List<Map<String, Object>>) person.get(field);
            if (!values.isEmpty() && values.get(0).containsKey(subField)) {
                return values.get(0).get(subField).toString();
            }
        }
        return defaultValue;
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

    public String createContact(
            OAuth2AuthenticationToken authentication,
            @RequestParam String givenName,
            @RequestParam String familyName,
            @RequestParam String email,
            @RequestParam String phone) {

        String accessToken = getAccessToken(authentication);
        String url = "https://people.googleapis.com/v1/people:createContact?access_token=" + accessToken;

        Map<String, Object> requestBody = Map.of(
                "names", List.of(Map.of("givenName", givenName, "familyName", familyName)),
                "emailAddresses", List.of(Map.of("value", email)),
                "phoneNumbers", List.of(Map.of("value", phone))
        );

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return "redirect:/contacts";
        } else {
            return "error"; // Create an error.html to display errors
        }
    }

    // 🔹 Update Contact

    @PostMapping("/contacts/update")

    public String updateContact(OAuth2AuthenticationToken authentication,
                                @RequestParam String resourceName,
                                @RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String phone,
                                Model model) {

        if (resourceName == null || resourceName.isEmpty()) {
            model.addAttribute("error", "Invalid resource name.");
            return "contacts";
        }

        String accessToken = getAccessToken(authentication);
        String url = "https://people.googleapis.com/v1/" + resourceName + "?updatePersonFields=names,emailAddresses,phoneNumbers";

        Map<String, Object> requestBody = Map.of(
                "names", List.of(Map.of("displayName", name)),
                "emailAddresses", List.of(Map.of("value", email)),
                "phoneNumbers", List.of(Map.of("value", phone))
        );

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken); // Use Bearer token instead of query param

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update contact: " + e.getMessage());
            return "contacts";
        }

        return "redirect:/contacts";
    }
    // 🔹 Delete Contact

    @PostMapping("/contacts/delete")

    public String deleteContact(OAuth2AuthenticationToken authentication,
                                @RequestParam String resourceName, Model model) {
        if (resourceName == null || resourceName.isEmpty()) {
            model.addAttribute("error", "Missing resource name.");
            return "contacts";
        }

        // Ensure resourceName is correctly formatted
        if (!resourceName.startsWith("people/")) {
            resourceName = "people/" + resourceName;
        }

        String accessToken = getAccessToken(authentication);
        String url = "https://people.googleapis.com/v1/" + resourceName;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete contact: " + e.getMessage());
            return "contacts";
        }

        return "redirect:/contacts";
    }


}

