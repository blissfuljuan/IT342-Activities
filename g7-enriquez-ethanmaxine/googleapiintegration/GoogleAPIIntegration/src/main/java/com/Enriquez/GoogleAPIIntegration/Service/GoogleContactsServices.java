package com.Enriquez.GoogleAPIIntegration.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Person;

import java.io.IOException;
import java.util.Collections;
import com.Enriquez.GoogleAPIIntegration.DTO.Contact;

import org.springframework.http.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoogleContactsServices {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    private static final String GOOGLE_CONTACTS_API = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses,phoneNumbers";
    private static final String GOOGLE_CONTACTS_SCOPE = "https://people.googleapis.com/v1/people:createContact";

    @SuppressWarnings({ "unchecked", "null" })
    public List<Map<String, Object>> getGoogleContacts(OAuth2AuthenticationToken authentication) {
        // Retrieve OAuth2 access token
        OAuth2AuthorizedClient authorizedClient = authorizedClientService
                .loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        

        // Call Google People API
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(GOOGLE_CONTACTS_API, HttpMethod.GET, entity, Map.class);

        // Extract contacts
        if (response.getBody() != null && response.getBody().containsKey("connections")) {
            return (List<Map<String, Object>>) response.getBody().get("connections");
        }
        
        return List.of();
    }
    
    public void createGoogleContact(OAuth2AuthenticationToken authentication, Contact contact) {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService
                .loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());

        if (authorizedClient == null) {
            throw new RuntimeException("Client not authorized");
        }

        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        // Prepare the request body for the Google People API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("names", List.of(Map.of("givenName", contact.getName())));
        requestBody.put("emailAddresses", List.of(Map.of("value", contact.getEmail())));
        requestBody.put("phoneNumbers", List.of(Map.of("value", contact.getPhone())));

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(GOOGLE_CONTACTS_SCOPE, HttpMethod.POST, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Contact created successfully!");
            } else {
                throw new RuntimeException("Failed to create contact: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create contact", e);
        }
    }



}
