package com.ligan.googlecontact.peopleintegration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ContactService {

    private final RestTemplate restTemplate;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private static final String PEOPLE_API_BASE_URL = "https://people.googleapis.com/v1/people/me/connections";

    @Autowired
    public ContactService(OAuth2AuthorizedClientService authorizedClientService, RestTemplate restTemplate) {
        this.authorizedClientService = authorizedClientService;
        this.restTemplate = restTemplate;
    }

    public String getAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            log.error("User is not authenticated with OAuth2");
            throw new RuntimeException("User is not authenticated with OAuth2");
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName()
        );

        if (client == null || client.getAccessToken() == null) {
            log.error("No authorized client found or token is missing");
            throw new RuntimeException("No authorized client found or token is missing");
        }

        return client.getAccessToken().getTokenValue();
    }

    public List<Map<String, Object>> getAllContacts() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAccessToken());
            headers.set("Accept", "application/json");

            String url = PEOPLE_API_BASE_URL + "?personFields=names,emailAddresses,phoneNumbers,addresses,organizations,metadata&pageSize=1000";

            log.info("Fetching contacts from Google API: {}", url);

            List<Map<String, Object>> allConnections = new ArrayList<>();
            String nextPageToken = null;

            do {
                String pageUrl = url;
                if (nextPageToken != null) {
                    pageUrl += "&pageToken=" + nextPageToken;
                }

                ResponseEntity<Map> response = restTemplate.exchange(
                        pageUrl,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Map.class
                );

                Map<String, Object> body = response.getBody();
                if (body != null && body.containsKey("connections")) {
                    List<Map<String, Object>> connections = (List<Map<String, Object>>) body.get("connections");
                    allConnections.addAll(connections);
                    nextPageToken = body.containsKey("nextPageToken") ? (String) body.get("nextPageToken") : null;
                } else {
                    break;
                }
            } while (nextPageToken != null);

            return allConnections;

        } catch (Exception e) {
            log.error("Error fetching contacts", e);
            throw new RuntimeException("Failed to fetch contacts", e);
        }
    }

    public Map<String, Object> getContact(String resourceName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAccessToken());
            headers.set("Accept", "application/json");

            String contactId = resourceName;
            if (contactId.contains("/")) {
                contactId = contactId.substring(contactId.lastIndexOf('/') + 1);
            }
            if (contactId.contains("%2F")) {
                contactId = contactId.substring(contactId.lastIndexOf("%2F") + 3);
            }

            String url = "https://people.googleapis.com/v1/people/" + contactId +
                    "?personFields=names,emailAddresses,phoneNumbers,addresses,organizations,metadata";

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching contact: {}", resourceName, e);
            throw new RuntimeException("Failed to fetch contact", e);
        }
    }

    public Map<String, Object> createContact(Map<String, Object> contact) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAccessToken());
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://people.googleapis.com/v1/people:createContact",
                    HttpMethod.POST,
                    new HttpEntity<>(contact, headers),
                    Map.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error creating contact", e);
            throw new RuntimeException("Failed to create contact", e);
        }
    }

    public Map<String, Object> updateContact(String resourceName, Map<String, Object> contact) {
        try {
            // First, get the existing contact to obtain the etag
            Map<String, Object> existingContact = getContact(resourceName);
            String etag = (String) existingContact.get("etag");

            HttpHeaders headers = new HttpHeaders();
            String token = getAccessToken();
            headers.setBearerAuth(token);
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");

            String contactId = resourceName;
            if (contactId.contains("/")) {
                contactId = contactId.substring(contactId.lastIndexOf('/') + 1);
            }
            if (contactId.contains("%2F")) {
                contactId = contactId.substring(contactId.lastIndexOf("%2F") + 3);
            }

            String batchUrl = "https://people.googleapis.com/v1/people:batchUpdateContacts";

            // Prepare the update data with the existing contact's metadata
            Map<String, Object> updateData = new HashMap<>(contact);
            updateData.put("resourceName", "people/" + contactId);
            updateData.put("etag", etag);

            // Preserve existing metadata
            if (existingContact.containsKey("metadata")) {
                updateData.put("metadata", existingContact.get("metadata"));
            }

            Map<String, Map<String, Object>> contacts = new HashMap<>();
            contacts.put("people/" + contactId, updateData);

            Map<String, Object> batchRequest = new HashMap<>();
            batchRequest.put("contacts", contacts);
            batchRequest.put("updateMask", "names,emailAddresses,phoneNumbers,addresses,organizations");

            log.info("Sending batch update request: {}", batchRequest);

            ResponseEntity<Map> response = restTemplate.exchange(
                    batchUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(batchRequest, headers),
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Update failed with status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to update contact: " + response.getStatusCode());
            }

            return getContact("people/" + contactId);
        } catch (Exception e) {
            log.error("Error updating contact: {} - Exception: {}", resourceName, e.getMessage(), e);
            throw new RuntimeException("Failed to update contact: " + e.getMessage(), e);
        }
    }

    public void deleteContact(String resourceName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAccessToken());

            String contactId = resourceName;
            if (contactId.contains("/")) {
                contactId = contactId.substring(contactId.lastIndexOf('/') + 1);
            }
            if (contactId.contains("%2F")) {
                contactId = contactId.substring(contactId.lastIndexOf("%2F") + 3);
            }

            String deleteUrl = "https://people.googleapis.com/v1/people/" + contactId + ":deleteContact";

            restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    new HttpEntity<>(headers),
                    Void.class
            );
        } catch (Exception e) {
            log.error("Error deleting contact: {}", resourceName, e);
            throw new RuntimeException("Failed to delete contact", e);
        }
    }

    public Map<String, Object> testApiConnection() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAccessToken());
            headers.set("Accept", "application/json");

            String url = "https://people.googleapis.com/v1/people/me?personFields=names";

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("statusCode", response.getStatusCode().toString());
            result.put("data", response.getBody());

            return result;
        } catch (Exception e) {
            log.error("API test failed", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return error;
        }
    }
}