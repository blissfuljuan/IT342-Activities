package com.baclayon.oauth2login.Service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GoogleContactsService {

    private static final String GOOGLE_CONTACTS_API_URL =
            "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses";
    private static final String GOOGLE_CONTACTS_CREATE_URL =
            "https://people.googleapis.com/v1/people:createContact";
    private static final String GOOGLE_CONTACTS_UPDATE_URL =
            "https://people.googleapis.com/v1/{resourceName}:updateContact";
    private static final String GOOGLE_CONTACTS_DELETE_URL =
            "https://people.googleapis.com/v1/{resourceName}";

    public List<Map<String, Object>> getContacts(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                GOOGLE_CONTACTS_API_URL,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("connections")) {
            return List.of(); // Return empty list if no contacts found
        }

        return (List<Map<String, Object>>) responseBody.get("connections");
    }

    public void addContact(String accessToken, String firstName, String lastName, String email) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Content-Type", "application/json");

        String requestBody = "{"
                + "\"names\": [{\"givenName\": \"" + firstName + "\", \"familyName\": \"" + lastName + "\"}],"
                + "\"emailAddresses\": [{\"value\": \"" + email + "\"}]"
                + "}";

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        restTemplate.exchange(
                GOOGLE_CONTACTS_CREATE_URL,
                HttpMethod.POST,
                entity,
                String.class
        );
    }

    public void updateContact(String accessToken, String resourceName, String firstName, String lastName, String email) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Content-Type", "application/json");

        String requestBody = "{"
                + "\"names\": [{\"givenName\": \"" + firstName + "\", \"familyName\": \"" + lastName + "\"}],"
                + "\"emailAddresses\": [{\"value\": \"" + email + "\"}]"
                + "}";

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        restTemplate.exchange(
                GOOGLE_CONTACTS_UPDATE_URL.replace("{resourceName}", resourceName),
                HttpMethod.PATCH,
                entity,
                String.class
        );
    }

    public void deleteContact(String accessToken, String resourceName) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        restTemplate.exchange(
                GOOGLE_CONTACTS_DELETE_URL.replace("{resourceName}", resourceName),
                HttpMethod.DELETE,
                entity,
                String.class
        );
    }
}