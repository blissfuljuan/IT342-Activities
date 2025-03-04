package com.quitco.googlecontactsapi.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

public class TokenFetcher {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public TokenFetcher(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    public String fetchToken() {
        OAuth2AuthenticationToken authentication = getAuthenticationToken();
        if (authentication == null) {
            throw new IllegalStateException("No authenticated user found in security context");
        }
        String clientRegistrationId = authentication.getAuthorizedClientRegistrationId();
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(clientRegistrationId, authentication.getName());
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        return accessToken.getTokenValue();
    }

    private OAuth2AuthenticationToken getAuthenticationToken() {
        return (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    }
}