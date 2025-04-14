package com.catulong.oauth2login.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpSession;

@Component
public class OAuth2LoginSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);
    private final OAuth2AuthorizedClientService authorizedClientService;

    public OAuth2LoginSuccessHandler(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    public void handleSuccess(Authentication authentication, HttpSession session) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();

            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    clientRegistrationId,
                    oauthToken.getName()
            );

            if (client != null) {
                OAuth2AccessToken accessToken = client.getAccessToken();
                session.setAttribute("accessToken", accessToken.getTokenValue()); // Store in session
                logger.info("Access Token Stored in Session: {}", accessToken.getTokenValue());
            } else {
                logger.warn("OAuth2AuthorizedClient is null! Token could not be retrieved.");
            }
        } else {
            logger.warn("Authentication is not an instance of OAuth2AuthenticationToken");
        }
    }
}