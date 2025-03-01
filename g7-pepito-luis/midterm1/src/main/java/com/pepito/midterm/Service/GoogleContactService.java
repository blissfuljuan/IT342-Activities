package com.pepito.midterm.Service;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoogleContactService {

    private final OAuth2AuthorizedClientService clientService;

    public GoogleContactService(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    private String getAccessToken(OAuth2User oAuth2User) {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient("google", oAuth2User.getAttribute("sub"));
        if (client == null) {
            throw new IllegalStateException("No authorized client found for Google");
        }
        return client.getAccessToken().getTokenValue();
    }

    public Map<String, Object> getContacts(OAuth2User oAuth2User) {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient("google", oAuth2User.getAttribute("sub"));
        if (client == null) {
            throw new IllegalStateException("No authorized client found for Google");
        }

        String accessToken = client.getAccessToken().getTokenValue();
        String url = "https://people.googleapis.com/v1/people/me/connections"
                + "?personFields=names,emailAddresses,phoneNumbers";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        return response.getBody();
    }


    public List<Map<String, String>> formatContacts(Map<String, Object> contacts) {
        List<Map<String, Object>> connections = (List<Map<String, Object>>) contacts.getOrDefault("connections", List.of());
        return connections.stream()
                .map(contact -> Map.of(
                        "name", getValue(contact, "names"),
                        "email", getValue(contact, "emailAddresses"),
                        "phone", getValue(contact, "phoneNumbers"),
                        "resourceName", (String) contact.getOrDefault("resourceName", "N/A")
                ))
                .collect(Collectors.toList());
    }

    private String getValue(Map<String, Object> contact, String key) {
        List<Map<String, Object>> values = (List<Map<String, Object>>) contact.get(key);
        if (values != null && !values.isEmpty()) {
            // Names, emails, and phones often have 'displayName' or 'value' fields
            if (key.equals("names")) {
                return (String) values.get(0).getOrDefault("displayName", "N/A");
            } else if (key.equals("emailAddresses")) {
                return (String) values.get(0).getOrDefault("value", "N/A");
            } else if (key.equals("phoneNumbers")) {
                return (String) values.get(0).getOrDefault("value", "N/A");
            }
        }
        return "N/A";
    }

    public void addContact(OAuth2User oAuth2User, String name, String email, String phone) {
        String accessToken = getAccessToken(oAuth2User);
        String url = "https://people.googleapis.com/v1/people:createContact";

        Map<String, Object> body = Map.of(
            "names", List.of(Map.of("givenName", name)),
            "emailAddresses", List.of(Map.of("value", email)),
            "phoneNumbers", List.of(Map.of("value", phone))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        new RestTemplate().postForEntity(url, entity, String.class);
    }

    public void updateContact(OAuth2User oAuth2User, String resourceName, String name, String email, String phone) {
        String accessToken = getAccessToken(oAuth2User);
        String url = "https://people.googleapis.com/v1/" + resourceName + "?updatePersonFields=names,emailAddresses,phoneNumbers";

        Map<String, Object> body = Map.of(
            "names", List.of(Map.of("givenName", name)),
            "emailAddresses", List.of(Map.of("value", email)),
            "phoneNumbers", List.of(Map.of("value", phone))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        new RestTemplate().put(url, entity);
    }

    public void deleteContact(OAuth2User oAuth2User, String resourceName) {
        String accessToken = getAccessToken(oAuth2User);
        String url = "https://people.googleapis.com/v1/" + resourceName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        new RestTemplate().exchange(url, HttpMethod.DELETE, entity, String.class);
    }
    
}