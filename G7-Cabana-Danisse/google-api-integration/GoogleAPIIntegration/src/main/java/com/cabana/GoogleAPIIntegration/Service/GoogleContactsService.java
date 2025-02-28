package com.cabana.GoogleAPIIntegration.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

@Service
public class GoogleContactsService {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    private static final String GOOGLE_CONTACTS_API = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses,phoneNumbers";

    public List<Map<String, Object>> getGoogleContacts(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService
                .loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());

        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(GOOGLE_CONTACTS_API, HttpMethod.GET, entity, Map.class);

        if (response.getBody() != null && response.getBody().containsKey("connections")) {
            return (List<Map<String, Object>>) response.getBody().get("connections");
        }
        return List.of();
    }
}
