package com.Lacaba.GoogleIntegration.Controller;

import com.fasterxml.jackson.core.JsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import com.google.api.services.people.v1.model.Person;

@Service
public class GoogleContactsService {
    private final OAuth2AuthorizedClientService clientService;

    @Autowired
    public GoogleContactsService(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    public List<Person> getContacts(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        GoogleCredential credential = new GoogleCredential().setAccessToken(client.getAccessToken().getTokenValue());
        PeopleService peopleService = new PeopleService.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("Google Contacts App")
                .build();

        ListConnectionsResponse response;
        try {
            response = peopleService.people().connections()
                    .list("people/me")
                    .setPersonFields("names,emailAddresses")
                    .execute();
            return response.getConnections();
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
