package com.pepito.midterm.Config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.BearerToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GoogleCredentialConfig {

    private final OAuth2AuthorizedClientService clientService;

    public GoogleCredentialConfig(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    public Credential getCredential(OAuth2User oAuth2User) {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient("google", oAuth2User.getName());
        if (client == null || client.getAccessToken() == null) {
            throw new IllegalStateException("No authorized client found for Google.");
        }

        return new Credential(BearerToken.authorizationHeaderAccessMethod())
                .setAccessToken(client.getAccessToken().getTokenValue());
    }
}
