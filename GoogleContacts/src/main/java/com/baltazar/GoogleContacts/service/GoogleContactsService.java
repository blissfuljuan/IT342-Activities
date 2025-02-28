package com.baltazar.GoogleContacts.service;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GoogleContactsService {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    public List<String> fetchContacts(String principalName) {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient("google", principalName);

        // Check if user is authorized
        if (authorizedClient == null) {
            System.out.println("User is not authorized or session expired.");
            return List.of("No contacts found (user not authorized)");
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

        // Google People API URL to fetch contacts
        String url = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken.getTokenValue());

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            System.out.println("Google API Response: " + response.getBody()); // Debugging

            List<String> contactsList = new ArrayList<>();
            if (response.getBody() != null && response.getBody().containsKey("connections")) {
                List<Map<String, Object>> connections = (List<Map<String, Object>>) response.getBody().get("connections");

                for (Map<String, Object> connection : connections) {
                    if (connection.containsKey("names")) {
                        List<Map<String, Object>> nameList = (List<Map<String, Object>>) connection.get("names");
                        if (!nameList.isEmpty() && nameList.get(0).containsKey("displayName")) {
                            contactsList.add((String) nameList.get(0).get("displayName"));
                        }
                    }
                }
            }

            return contactsList.isEmpty() ? List.of("No contacts found") : contactsList;
        } catch (Exception e) {
            System.out.println("Error fetching contacts: " + e.getMessage());
            e.printStackTrace();
            return List.of("Error fetching contacts. Check logs.");
        }
    }
}
