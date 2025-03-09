package com.example.sugimoto.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class userController {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    //GET USER INFO
    @RequestMapping("/user-info")
    public String getUser(@AuthenticationPrincipal OAuth2User principal, Model model) {
        model.addAttribute("name", principal.getAttribute("name"));
        model.addAttribute("email", principal.getAttribute("email"));
        model.addAttribute("picture", principal.getAttribute("picture"));
        model.addAttribute("phone", principal.getAttribute("phoneNumber"));
        return "user-info";
    }

    
    //CREATE CONTACT
    @PostMapping("/add-contact")
public ResponseEntity<String> addContact(@RequestBody Map<String, String> contactData, OAuth2AuthenticationToken authentication) {
    try {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), authentication.getName());

        if (authorizedClient == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        if (accessToken == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No access token");

        String url = "https://people.googleapis.com/v1/people:createContact";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken.getTokenValue());
        headers.add("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("names", List.of(Map.of("givenName", contactData.get("name"))));
        requestBody.put("emailAddresses", List.of(Map.of("value", contactData.get("email"))));
        requestBody.put("phoneNumbers", List.of(Map.of("value", contactData.get("phoneNumber"))));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForEntity(url, requestEntity, String.class);
        

        return ResponseEntity.ok("Contact added successfully!");
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add contact");
    }
}

//READ CONTACT
@SuppressWarnings("unchecked")
    @RequestMapping("/contacts")
    public String getContacts(OAuth2AuthenticationToken authentication, Model model) {
    try {
        OAuth2User principal = authentication.getPrincipal();
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), authentication.getName());

        if (authorizedClient == null) throw new IllegalStateException("Authorized client not found");

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        if (accessToken == null) throw new IllegalStateException("Access token not found");

        String url = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses,phoneNumbers";
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> response = restTemplate.exchange(
            url + "&access_token=" + accessToken.getTokenValue(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        ).getBody();

        System.out.println("API Response: " + response); // Debugging

        Object connectionsObj = response.get("connections");
        List<Map<String, Object>> connections = new ArrayList<>();

        if (connectionsObj instanceof List<?>) {
            for (Object obj : (List<?>) connectionsObj) {
                if (obj instanceof Map) {
                    connections.add((Map<String, Object>) obj);
                }
            }
        }

        for (Map<String, Object> contact : connections) {
            List<Map<String, Object>> names = (List<Map<String, Object>>) contact.get("names");
            List<Map<String, Object>> emails = (List<Map<String, Object>>) contact.get("emailAddresses");
            List<Map<String, Object>> phoneNumbers = (List<Map<String, Object>>) contact.get("phoneNumbers");

            contact.put("displayName", (names != null && !names.isEmpty()) ? names.get(0).get("displayName") : "Unknown");
            contact.put("email", (emails != null && !emails.isEmpty()) ? emails.get(0).get("value") : "No email");
            contact.put("phoneNumber", (phoneNumbers != null && !phoneNumbers.isEmpty()) ? phoneNumbers.get(0).get("value") : "No contact number");
        }

        System.out.println("Parsed Connections: " + connections); // Debugging

        model.addAttribute("connections", connections);
        model.addAttribute("name", principal.getAttribute("name"));
        model.addAttribute("email", principal.getAttribute("email"));
        model.addAttribute("picture", principal.getAttribute("picture"));

        return "contacts";
    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("error", "An error occurred while fetching contacts: " + e.getMessage());
        return "error";
    }
}

//UPDATE CONTACT

@PutMapping("/edit-contact")
public ResponseEntity<String> editContact(@RequestBody Map<String, String> contactData, OAuth2AuthenticationToken authentication) {
    try {
        System.out.println("Received Contact Data: " + contactData);

        // Validate required fields
        String resourceName = contactData.get("resourceName");
        String etag = contactData.get("etag");

        if (resourceName == null || resourceName.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing required field: resourceName");
        }
        if (etag == null || etag.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing required field: etag");
        }
        if (!contactData.containsKey("name") && !contactData.containsKey("email") && !contactData.containsKey("phoneNumber")) {
            return ResponseEntity.badRequest().body("At least one field (name, email, or phoneNumber) must be provided for update.");
        }

        // Load OAuth2 client
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), authentication.getName());

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Access token missing");
        }

        // API request URL
        String url = "https://people.googleapis.com/v1/" + resourceName + ":updateContact?updatePersonFields=names,emailAddresses,phoneNumbers";

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construct request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("etag", etag);

        contactData.forEach((key, value) -> {
            if (!value.isEmpty()) {
                switch (key) {
                    case "name":
                        requestBody.put("names", List.of(Map.of("givenName", value)));
                        break;
                    case "email":
                        requestBody.put("emailAddresses", List.of(Map.of("value", value, "type", "work")));
                        break;
                    case "phoneNumber":
                        requestBody.put("phoneNumbers", List.of(Map.of("value", value, "type", "mobile")));
                        break;
                }
            }
        });

        // Send PATCH request
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.PATCH, new HttpEntity<>(requestBody, headers), String.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
    }
}



//DELETE CONTACT
@DeleteMapping("/delete-contact")
public ResponseEntity<Map<String, String>> deleteContact(@RequestBody Map<String, String> requestBody, OAuth2AuthenticationToken authentication) {
    try {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), authentication.getName());

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "Unauthorized"));
        }

        String resourceName = requestBody.getOrDefault("resourceName", "").trim();
        if (resourceName.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Missing resourceName"));
        }

        String url = "https://people.googleapis.com/v1/people/" + resourceName.replaceFirst("^people/", "") + ":deleteContact";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue());

        ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

        return ResponseEntity.status(response.getStatusCode()).body(Collections.singletonMap("message", "Contact deleted successfully"));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", "Failed to delete contact: " + e.getMessage()));
    }
}



}
