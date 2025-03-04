package com.CharlesDominic.oauth2login.Config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Configuration
public class GoogleConfig {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();


    public PeopleService peopleService() throws IOException {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(getClass().getResourceAsStream("/client_secret.json")));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                JSON_FACTORY,
                clientSecrets,
                List.of("https://www.googleapis.com/auth/contacts.readonly"))
                .build();

        Credential credential = flow.loadCredential("user");

        return new PeopleService.Builder(new NetHttpTransport(), JSON_FACTORY, credential)
                .setApplicationName("oauth2login")
                .build();
    }
}
