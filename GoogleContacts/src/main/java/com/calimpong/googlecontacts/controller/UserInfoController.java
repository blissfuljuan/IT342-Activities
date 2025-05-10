package com.calimpong.googlecontacts.controller;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-info1")
public class UserInfoController {
    private final OAuth2AuthorizedClientService authorizedClientService;

    public UserInfoController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping
    public String getUserInfo(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );
        if (client == null || client.getAccessToken() == null) {
            return "Access token is null.";
        }
        OAuth2AccessToken accessToken = client.getAccessToken();
        String tokenValue = accessToken.getTokenValue();
        return "Access Token: " + tokenValue;
    }
}

