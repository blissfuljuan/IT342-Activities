package com.calimpong.google.oauth.service;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class GoogleContactsService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleContactsService.class);

    /**
     * Retrieves contacts from Google People API.
     *
     * @param authorizedClient the OAuth2AuthorizedClient with the access token
     * @return a list of contacts or an empty list if retrieval fails
     */
    public List<Map<String, Object>> getContacts(OAuth2AuthorizedClient authorizedClient) {
        if (authorizedClient == null) {
            logger.warn("⚠️ Authorized Client is NULL! Cannot retrieve contacts.");
            return Collections.emptyList();
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        if (accessToken == null) {
            logger.warn("⚠️ Access Token is NULL! Cannot retrieve contacts.");
            return Collections.emptyList();
        }

        logger.info("🔑 Using Access Token: {}", accessToken.getTokenValue());

        // API URL to fetch contacts
        String url = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses,phoneNumbers";

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken.getTokenValue());

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("✅ Successfully retrieved contacts.");
                return (List<Map<String, Object>>) response.getBody().getOrDefault("connections", Collections.emptyList());
            } else {
                logger.warn("⚠️ No contacts found or invalid response.");
                return Collections.emptyList();
            }
        } catch (Exception e) {
            logger.error("❌ Error retrieving contacts: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
