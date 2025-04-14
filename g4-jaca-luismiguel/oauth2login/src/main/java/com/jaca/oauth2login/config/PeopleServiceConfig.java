package com.jaca.oauth2login.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Configuration
public class PeopleServiceConfig {

    //private static final String APPLICATION_NAME = "oauth2login";

    // @Bean
    // public PeopleService peopleService() throws GeneralSecurityException, IOException {
    //JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    // Replace with your credentials.json path
    //  Credential credential = GoogleCredential.fromStream(getClass().getClassLoader().getResourceAsStream("credentials.json"))
    //   .createScoped(List.of("https://www.googleapis.com/auth/contacts"));
//
    //return new PeopleService.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
    ////    .setApplicationName(APPLICATION_NAME)
    //    .build();
    //}
}
