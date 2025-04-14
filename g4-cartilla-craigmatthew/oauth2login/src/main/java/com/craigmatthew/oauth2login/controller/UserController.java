package com.craigmatthew.oauth2login.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final OAuth2AuthorizedClientService authorizedClientService;

    public UserController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal OidcUser principal, OAuth2AuthenticationToken authentication) {
        if (principal == null) {
            logger.error("Unauthorized access attempt - No OAuth2 user found.");
            if (authentication != null) {
                logger.error("OAuth2AuthenticationToken found: {}", authentication.getName());
            } else {
                logger.error("OAuth2AuthenticationToken is also null!");
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication failed.");
        }

        Map<String, Object> response = new HashMap<>(principal.getAttributes());
        response.put("email", principal.getEmail());
        response.put("name", principal.getFullName());
        response.put("picture", principal.getPicture());

        logger.info("User info retrieved successfully for {}", principal.getEmail());
        return response;
    }


    @GetMapping("/token")
    public Map<String, String> getAccessToken(OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            logger.error("Unauthorized access attempt - OAuth2 authentication token is missing.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access.");
        }

        String clientRegistrationId = authentication.getAuthorizedClientRegistrationId();
        String userName = authentication.getName();

        if (clientRegistrationId == null || userName == null) {
            logger.warn("Invalid authentication data: clientRegistrationId={}, userName={}", clientRegistrationId, userName);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid authentication data.");
        }

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(clientRegistrationId, userName);

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            logger.warn("No access token found for user: {}", userName);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No access token found.");
        }

        logger.info("Access token retrieved successfully for user: {}", userName);
        return Map.of("access_token", authorizedClient.getAccessToken().getTokenValue());
    }
}