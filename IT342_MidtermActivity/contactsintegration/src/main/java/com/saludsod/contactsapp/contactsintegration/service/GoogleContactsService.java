package com.saludsod.contactsapp.contactsintegration.service;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class GoogleContactsService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(GoogleContactsService.class);

    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService, RestTemplate restTemplate) {
        this.authorizedClientService = authorizedClientService;
        this.restTemplate = restTemplate;
    }

    private String getAccessToken(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        if (client == null || client.getAccessToken() == null) {
            throw new IllegalStateException("OAuth2AuthorizedClient or AccessToken is null");
        }

        return client.getAccessToken().getTokenValue();
    }

    public String getContacts(OAuth2AuthenticationToken authentication) {
        try {
            String accessToken = getAccessToken(authentication);
            String url = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses,phoneNumbers,photos";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                logger.error("Failed to fetch contacts: {}", response.getStatusCode());
                return "Error fetching contacts. Status: " + response.getStatusCode();
            }
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching contacts", e);
            return "Error fetching contacts: " + e.getMessage();
        }
    }

    public String createContact(OAuth2AuthenticationToken authentication, String firstName, String lastName, List<String> emails, List<String> phoneNumbers) {
        try {
            String accessToken = getAccessToken(authentication);
            String url = "https://people.googleapis.com/v1/people:createContact";

            StringBuilder jsonBody = new StringBuilder("{");
            jsonBody.append("\"names\": [{\"givenName\": \"").append(firstName).append("\", \"familyName\": \"").append(lastName).append("\"}],");

            // Add emails
            jsonBody.append("\"emailAddresses\": [");
            for (int i = 0; i < emails.size(); i++) {
                jsonBody.append("{\"value\": \"").append(emails.get(i)).append("\"}");
                if (i < emails.size() - 1) jsonBody.append(",");
            }
            jsonBody.append("],");

            // Add phone numbers
            jsonBody.append("\"phoneNumbers\": [");
            for (int i = 0; i < phoneNumbers.size(); i++) {
                jsonBody.append("{\"value\": \"").append(phoneNumbers.get(i)).append("\"}");
                if (i < phoneNumbers.size() - 1) jsonBody.append(",");
            }
            jsonBody.append("]}");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                logger.error("Failed to create contact: {}", response.getStatusCode());
                return "Error creating contact. Status: " + response.getStatusCode();
            }
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error creating contact", e);
            return "Error creating contact: " + e.getMessage();
        }
    }

    public String deleteContact(OAuth2AuthenticationToken authentication, String resourceName) {
        try {
            if (resourceName == null || resourceName.isEmpty()) {
                return "Error: Invalid resourceName.";
            }

            String accessToken = getAccessToken(authentication);
            String url = "https://people.googleapis.com/v1/" + resourceName + ":deleteContact";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return "Contact deleted successfully";
            } else {
                return "Failed to delete contact. Status: " + response.getStatusCode();
            }
        } catch (Exception e) {
            return "Error deleting contact: " + e.getMessage();
        }
    }
}