package com.CharlesDominic.oauth2login.Service;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class CredentialService {

    private final OAuth2AuthorizedClientService clientService;

    public CredentialService(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    public Credential getGoogleCredential(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        String accessToken = client.getAccessToken().getTokenValue();

        return new Credential(BearerToken.authorizationHeaderAccessMethod())
                .setAccessToken(accessToken);
    }
}