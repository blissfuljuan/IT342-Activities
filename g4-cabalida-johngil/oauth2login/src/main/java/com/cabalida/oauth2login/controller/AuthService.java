package com.cabalida.oauth2login.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public String getAccessTokenForCurrentUser() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OidcUser oidcUser = (OidcUser) oauthToken.getPrincipal();
            return oidcUser.getIdToken().getTokenValue(); // Ensure this is an access token, not ID token
        }

        throw new RuntimeException("User is not authenticated with Google");
    }
}
