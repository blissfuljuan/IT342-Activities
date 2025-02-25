package com.quitco.googlecontactsapi.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.people.v1.PeopleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class PeopleServiceConfig {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Bean
    public PeopleService peopleService() throws GeneralSecurityException, IOException {
        OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String clientRegistrationId = authentication.getAuthorizedClientRegistrationId();
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(clientRegistrationId, authentication.getName());
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

        HttpRequestInitializer requestInitializer = request -> {
            request.getHeaders().setAuthorization("Bearer " + accessToken.getTokenValue());
        };

        return new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                requestInitializer
        ).setApplicationName("googlecontactsapi").build();
    }
}