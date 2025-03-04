package com.reneekiara.oauth2login.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
public class googleContactsService {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final OAuth2AuthorizedClientService authorizedClientService;

    PeopleService peopleService;

    public googleContactsService(OAuth2AuthorizedClientService authorizedClientService){
        this.authorizedClientService = authorizedClientService;
    }
    public List getContacts(String principalName) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("google",principalName);

        OAuth2AccessToken accessToken = client.getAccessToken();
        GoogleCredential credentials = new GoogleCredential().setAccessToken(accessToken.getTokenValue());

        PeopleService service =
                new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                        .setApplicationName("Google Contacts")
                        .build();

        ListConnectionsResponse response = service.people().connections().list("people/me")
                .setPageSize(10)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        List<Person> connections = response.getConnections();

        return connections;

    }

    public void newContact (Person person, String principalName) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("google",principalName);

        OAuth2AccessToken accessToken = client.getAccessToken();
        GoogleCredential credentials = new GoogleCredential().setAccessToken(accessToken.getTokenValue());

        PeopleService service =
                new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                        .setApplicationName("Google Contacts")
                        .build();

        Person contactToCreate = new Person();
        List<Name> names = new ArrayList<>();
        names.add(new Name().setGivenName("John").setFamilyName("Doe"));
        contactToCreate.setNames(person.getNames());
        contactToCreate.setEmailAddresses(person.getEmailAddresses());
        contactToCreate.setPhoneNumbers(person.getPhoneNumbers());

        Person createdContact = service.people().createContact(contactToCreate).execute();


    }

    public void deleteContact (String name, String principalName) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("google",principalName);

        OAuth2AccessToken accessToken = client.getAccessToken();
        GoogleCredential credentials = new GoogleCredential().setAccessToken(accessToken.getTokenValue());

        PeopleService service =
                new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                        .setApplicationName("Google Contacts")
                        .build();

        service.people().deleteContact(name).execute();
    }

}
