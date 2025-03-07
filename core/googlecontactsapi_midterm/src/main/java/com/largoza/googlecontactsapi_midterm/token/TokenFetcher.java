package com.largoza.googlecontactsapi_midterm.token;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

public class TokenFetcher {

    private final OAuth2AuthorizedClientService oauthorizedClientService;

    public TokenFetcher(OAuth2AuthorizedClientService oauthorizedClientService) {
        this.oauthorizedClientService = oauthorizedClientService;
    }

    public String fetchToken() {
        OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication(); 
        
        if (authentication == null) {
            throw new IllegalStateException("User is not authorized");
        }

        OAuth2AuthorizedClient authorizedClient = oauthorizedClientService.loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

        return accessToken.getTokenValue();
    }
}
