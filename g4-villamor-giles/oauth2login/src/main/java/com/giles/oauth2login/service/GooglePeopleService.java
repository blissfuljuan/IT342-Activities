package com.giles.oauth2login.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class GooglePeopleService {
    private static final Logger log = LoggerFactory.getLogger(GooglePeopleService.class);

    public List<Person> getContacts(OAuth2AuthorizedClient authorizedClient) throws IOException {
        GoogleCredential credential = new GoogleCredential().setAccessToken(authorizedClient.getAccessToken().getTokenValue());

        PeopleService peopleService = new PeopleService.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("OAuth2Login")
                .build();

        ListConnectionsResponse response = peopleService.people().connections()
                .list("people/me")
                .setPageSize(10)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        List<Person> contacts = response.getConnections();

        // Log the contacts for debugging
        if (contacts != null) {
            for (Person contact : contacts) {
                log.info("Contact: {}", contact);
                log.info("Names: {}", contact.getNames());
                log.info("Emails: {}", contact.getEmailAddresses());

                // Ensure names and emailAddresses are not null
                if (contact.getNames() == null) {
                    contact.setNames(Collections.emptyList());
                }
                if (contact.getEmailAddresses() == null) {
                    contact.setEmailAddresses(Collections.emptyList());
                }
            }
        } else {
            log.warn("No contacts found or contacts list is null.");
            contacts = Collections.emptyList(); // Fallback to an empty list
        }

        return contacts;
    }

    public Person createContact(OAuth2AuthorizedClient authorizedClient, Person contact) throws IOException {
        GoogleCredential credential = new GoogleCredential().setAccessToken(authorizedClient.getAccessToken().getTokenValue());

        PeopleService peopleService = new PeopleService.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("OAuth2Login")
                .build();

        return peopleService.people().createContact(contact).execute();
    }

    public Person updateContact(OAuth2AuthorizedClient authorizedClient, String resourceName, Person contact) throws IOException {
        GoogleCredential credential = new GoogleCredential().setAccessToken(authorizedClient.getAccessToken().getTokenValue());

        PeopleService peopleService = new PeopleService.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("OAuth2Login")
                .build();

        return peopleService.people().updateContact(resourceName, contact).execute();
    }

    public void deleteContact(OAuth2AuthorizedClient authorizedClient, String resourceName) throws IOException {
        GoogleCredential credential = new GoogleCredential().setAccessToken(authorizedClient.getAccessToken().getTokenValue());

        PeopleService peopleService = new PeopleService.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("OAuth2Login")
                .build();

        peopleService.people().deleteContact(resourceName).execute();
    }
}