package com.largoza.googlecontactsapi_midterm.config;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.context.WebApplicationContext;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.largoza.googlecontactsapi_midterm.util.TokenFetcher;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;

@Configuration
public class PeopleServiceConfig {
    
    @Autowired
    private OAuth2AuthorizedClientService oAuthorizedClientService;

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public PeopleService peopleService() throws GeneralSecurityException, IOException {
        TokenFetcher tokenFetcher = new TokenFetcher(oAuthorizedClientService);
        String accessToken = tokenFetcher.fetchToken();

        HttpRequestInitializer requestInitializer = request -> request.getHeaders().setAuthorization("Bearer " + accessToken);

        return new PeopleService.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), 
            GsonFactory.getDefaultInstance(),
            requestInitializer)
        .setApplicationName("googlecontactsapi_midterm").build();
    }
}
