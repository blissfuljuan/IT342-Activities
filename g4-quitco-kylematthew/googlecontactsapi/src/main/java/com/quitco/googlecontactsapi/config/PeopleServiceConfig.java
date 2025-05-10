package com.quitco.googlecontactsapi.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.people.v1.PeopleService;
import com.quitco.googlecontactsapi.util.TokenFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class PeopleServiceConfig {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public PeopleService peopleService() throws GeneralSecurityException, IOException {
        TokenFetcher tokenFetcher = new TokenFetcher(authorizedClientService);
        String accessToken = tokenFetcher.fetchToken();

        HttpRequestInitializer requestInitializer = request -> request.getHeaders().setAuthorization("Bearer " + accessToken);

        return new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                requestInitializer
        ).setApplicationName("googlecontactsapi").build();
    }
}