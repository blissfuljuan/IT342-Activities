package com.rickshel.oauth2login.Controller;

import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Controller
public class ContactsController {

    @GetMapping("/contacts")
    public String getGoogleContacts(
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
            OAuth2AuthenticationToken authentication,
            Model model) {

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String apiUrl = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses,phoneNumbers";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response;

        try {
            response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                model.addAttribute("error", "Failed to fetch contacts. Please try again.");
                return "error-page";
            }
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while fetching contacts: " + e.getMessage());
            return "error-page";
        }

        List<Map<String, String>> contactsList = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            if (rootNode.has("connections")) {
                for (JsonNode contactNode : rootNode.get("connections")) {
                    Map<String, String> contact = new HashMap<>();

                    if (contactNode.has("names") && contactNode.get("names").size() > 0) {
                        JsonNode nameNode = contactNode.get("names").get(0);
                        contact.put("name", nameNode.has("displayName") ? nameNode.get("displayName").asText() : "Unknown");
                    } else {
                        contact.put("name", "Unknown");
                    }

                    if (contactNode.has("emailAddresses") && contactNode.get("emailAddresses").size() > 0) {
                        JsonNode emailNode = contactNode.get("emailAddresses").get(0);
                        contact.put("email", emailNode.has("value") ? emailNode.get("value").asText() : "No Email");
                    } else {
                        contact.put("email", "No Email");
                    }

                    if (contactNode.has("phoneNumbers") && contactNode.get("phoneNumbers").size() > 0) {
                        JsonNode phoneNode = contactNode.get("phoneNumbers").get(0);
                        contact.put("phone", phoneNode.has("value") ? phoneNode.get("value").asText() : "No Phone");
                    } else {
                        contact.put("phone", "No Phone");
                    }

                    contactsList.add(contact);
                }
            } else {
                model.addAttribute("error", "No contacts found.");
                return "error-page";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error parsing contacts data: " + e.getMessage());
            return "error-page";
        }

        model.addAttribute("contacts", contactsList);
        return "contacts";
    }

    @PostMapping("/contacts/add")
    public ResponseEntity<String> addGoogleContact(@RequestBody Map<String, String> contact,
                                                   @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String apiUrl = "https://people.googleapis.com/v1/people:createContact";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("names", Collections.singletonList(Collections.singletonMap("givenName", contact.get("name"))));
        requestBody.put("emailAddresses", Collections.singletonList(Collections.singletonMap("value", contact.get("email"))));
        requestBody.put("phoneNumbers", Collections.singletonList(Collections.singletonMap("value", contact.get("phone"))));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PatchMapping("/contacts/update/{resourceName}")
    public ResponseEntity<String> updateGoogleContact(@PathVariable String resourceName,
                                                      @RequestBody Map<String, String> contact,
                                                      @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String apiUrl = "https://people.googleapis.com/v1/" + resourceName + "?updatePersonFields=names,emailAddresses,phoneNumbers";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("names", Collections.singletonList(Collections.singletonMap("givenName", contact.get("name"))));
        requestBody.put("emailAddresses", Collections.singletonList(Collections.singletonMap("value", contact.get("email"))));
        requestBody.put("phoneNumbers", Collections.singletonList(Collections.singletonMap("value", contact.get("phone"))));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.PATCH, entity, String.class);

        return ResponseEntity.status(response.getStatusCode()).body("Contact updated successfully");
    }

    @DeleteMapping("/contacts/delete/{resourceName}")
    public ResponseEntity<String> deleteGoogleContact(@PathVariable String resourceName,
                                                      @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String apiUrl = "https://people.googleapis.com/v1/" + resourceName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.DELETE, entity, String.class);

        return ResponseEntity.status(response.getStatusCode()).body("Contact deleted successfully");
    }
}