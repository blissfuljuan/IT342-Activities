package com.cabiling.oauth2login.Config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory; // ✅ Use GsonFactory instead of deprecated JacksonFactory
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.people.v1.PeopleService;

import org.springframework.core.io.ClassPathResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.List;

@Configuration
public class GooglePeopleServiceConfig {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance(); // ✅ Replace JacksonFactory

    @Bean
    public PeopleService peopleService() throws GeneralSecurityException, IOException {
        // Initialize HTTP Transport
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Load credentials from classpath
        InputStream credentialsStream = new ClassPathResource("credentials.json").getInputStream();
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(credentialsStream)
                .createScoped(List.of("https://www.googleapis.com/auth/contacts"));

        // Return PeopleService instance
        return new PeopleService.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName("Google Contacts API Integration")
                .build();
    }
}
