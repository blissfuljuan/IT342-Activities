package com.example.gadiane.johnkarl.demolition.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GoogleCredentialService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleCredentialService.class);
    private final OAuth2AuthorizedClientService clientService;

    public GoogleCredentialService(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    public Credential getCredential() throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            logger.error("User is not authenticated with OAuth2");
            throw new IOException("User is not authenticated with OAuth2");
        }
        
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName());
        
        if (client == null) {
            logger.error("OAuth2 client not found");
            throw new IOException("OAuth2 client not found");
        }
        
        logger.debug("Creating Google credential with access token: {}", 
                client.getAccessToken().getTokenValue().substring(0, 5) + "...");
        
        return new GoogleCredential.Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .build()
                .setAccessToken(client.getAccessToken().getTokenValue());
    }
}
