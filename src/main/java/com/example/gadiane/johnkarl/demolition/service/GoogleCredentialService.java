package com.example.gadiane.johnkarl.demolition.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GoogleCredentialService {

    private final OAuth2AuthorizedClientService clientService;
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public GoogleCredentialService(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    public Credential getCredential() throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            throw new IllegalStateException("User not authenticated with OAuth2");
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        if (!"google".equals(oauthToken.getAuthorizedClientRegistrationId())) {
            throw new IllegalStateException("User not authenticated with Google");
        }

        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName());

        if (client == null) {
            throw new IllegalStateException("Authorized client not found");
        }

        OAuth2AccessToken accessToken = client.getAccessToken();

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(accessToken.getTokenValue());
        tokenResponse.setTokenType(accessToken.getTokenType().getValue());
        tokenResponse.setExpiresInSeconds(
                accessToken.getExpiresAt() != null ?
                        (accessToken.getExpiresAt().getEpochSecond() - System.currentTimeMillis() / 1000) :
                        null);

        return new Credential(BearerToken.authorizationHeaderAccessMethod())
                .setFromTokenResponse(tokenResponse);
    }
}