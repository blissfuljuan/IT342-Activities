package com.garcia.googlecontacts.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import java.util.List;
import com.garcia.googlecontacts.model.Contact;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    private static final String GOOGLE_PEOPLE_API_URL = "https://people.googleapis.com/v1/people/me/connections";

    @GetMapping
    public List<Contact> getContacts(Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2AuthorizedClient client = authorizedClientService
            .loadAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());
        OAuth2AccessToken accessToken = client.getAccessToken();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken.getTokenValue());

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            GOOGLE_PEOPLE_API_URL + "?personFields=names,emailAddresses",
            HttpMethod.GET,
            entity,
            String.class
        );

        // Parse the response to extract contacts
        List<Contact> contacts = parseContacts(response.getBody());

        return contacts;
    }

    @PostMapping
    public void addContact(@RequestBody Contact contact, Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2AuthorizedClient client = authorizedClientService
            .loadAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());
        OAuth2AccessToken accessToken = client.getAccessToken();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken.getTokenValue());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Contact> entity = new HttpEntity<>(contact, headers);
        restTemplate.postForEntity(GOOGLE_PEOPLE_API_URL + ":createContact", entity, String.class);
    }

    @PutMapping("/{id}")
    public void editContact(@PathVariable String id, @RequestBody Contact contact, Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2AuthorizedClient client = authorizedClientService
            .loadAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());
        OAuth2AccessToken accessToken = client.getAccessToken();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken.getTokenValue());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Contact> entity = new HttpEntity<>(contact, headers);
        restTemplate.exchange(
            GOOGLE_PEOPLE_API_URL + "/" + id + ":updateContact",
            HttpMethod.PUT,
            entity,
            String.class
        );
    }

    @DeleteMapping("/{id}")
    public void removeContact(@PathVariable String id, Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2AuthorizedClient client = authorizedClientService
            .loadAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());
        OAuth2AccessToken accessToken = client.getAccessToken();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken.getTokenValue());

        restTemplate.exchange(
            GOOGLE_PEOPLE_API_URL + "/" + id + ":deleteContact",
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            String.class
        );
    }

    // Parse the response to extract contacts
    private List<Contact> parseContacts(String responseBody) {
        // Implement your parsing logic here to convert the response body to List<Contact>
        return null; // Replace this with actual parsing logic and return the list of contacts
    }
}
