package com.canal.GoogleContact.service;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoogleContactsService {
    private final OAuth2AuthorizedClientService authorizedClientService;

    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    // Fetch all contacts
    public List<Map<String, Object>> getContacts(OAuth2AuthenticationToken authentication) {
        String accessToken = getAccessToken(authentication);
        String url = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses,phoneNumbers";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        List<Map<String, Object>> connections = (List<Map<String, Object>>) response.getBody().get("connections");
        if (connections == null) {
            return List.of();
        }

        return connections.stream().map(person -> Map.of(
                "id", person.get("resourceName"),  // Use resourceName as the ID
                "name", person.containsKey("names") ? ((List<Map<String, Object>>) person.get("names")).get(0).get("displayName") : "Unknown",
                "emails", person.containsKey("emailAddresses") ? ((List<Map<String, Object>>) person.get("emailAddresses")).stream().map(email -> email.get("value")).collect(Collectors.toList()) : List.of(),
                "phones", person.containsKey("phoneNumbers") ? ((List<Map<String, Object>>) person.get("phoneNumbers")).stream().map(phone -> phone.get("value")).collect(Collectors.toList()) : List.of()
        )).collect(Collectors.toList());
    }

    // Fetch a single contact by resourceName
    public Map<String, Object> getContactByResourceName(OAuth2AuthenticationToken authentication, String resourceName) {
        String accessToken = getAccessToken(authentication);
        String url = "https://people.googleapis.com/v1/" + resourceName + "?personFields=names,emailAddresses,phoneNumbers";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to fetch contact: " + response.getBody());
        }

        Map<String, Object> person = response.getBody();
        return Map.of(
                "id", person.get("resourceName"),
                "name", person.containsKey("names") ? ((List<Map<String, Object>>) person.get("names")).get(0).get("displayName") : "Unknown",
                "emails", person.containsKey("emailAddresses") ? ((List<Map<String, Object>>) person.get("emailAddresses")).stream().map(email -> email.get("value")).collect(Collectors.toList()) : List.of(),
                "phones", person.containsKey("phoneNumbers") ? ((List<Map<String, Object>>) person.get("phoneNumbers")).stream().map(phone -> phone.get("value")).collect(Collectors.toList()) : List.of()
        );
    }

    // Add a new contact
    public void addContact(OAuth2AuthenticationToken authentication, String name, String email, String phone) {
        String accessToken = getAccessToken(authentication);
        String url = "https://people.googleapis.com/v1/people:createContact";

        Map<String, Object> contactData = Map.of(
                "names", List.of(Map.of("givenName", name)),
                "emailAddresses", email != null && !email.isEmpty() ? List.of(Map.of("value", email)) : List.of(),
                "phoneNumbers", phone != null && !phone.isEmpty() ? List.of(Map.of("value", phone)) : List.of()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(contactData, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to add contact: " + response.getBody());
        }
    }

    // Update an existing contact
    public void updateContact(OAuth2AuthenticationToken authentication, String resourceName, String name, String email, String phone) {
        String accessToken = getAccessToken(authentication);
        String url = "https://people.googleapis.com/v1/" + resourceName + ":updateContact";

        Map<String, Object> contactData = Map.of(
                "names", List.of(Map.of("givenName", name)),
                "emailAddresses", email != null && !email.isEmpty() ? List.of(Map.of("value", email)) : List.of(),
                "phoneNumbers", phone != null && !phone.isEmpty() ? List.of(Map.of("value", phone)) : List.of()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(contactData, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update contact: " + response.getBody());
        }
    }

    // Delete a contact
    public void deleteContact(OAuth2AuthenticationToken authentication, String resourceName) {
        String accessToken = getAccessToken(authentication);
        String url = "https://people.googleapis.com/v1/" + resourceName + ":deleteContact";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to delete contact: " + response.getBody());
        }
    }

    // Helper method to get the OAuth2 access token
    private String getAccessToken(OAuth2AuthenticationToken authentication) {
        String userName = authentication.getName();
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("google", userName);
        if (client == null) {
            throw new RuntimeException("OAuth2 client not found for user: " + userName);
        }
        return client.getAccessToken().getTokenValue();
    }
}