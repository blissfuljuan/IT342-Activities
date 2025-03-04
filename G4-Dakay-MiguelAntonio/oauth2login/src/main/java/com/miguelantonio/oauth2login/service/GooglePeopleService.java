package com.miguelantonio.oauth2login.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GooglePeopleService {

    // Method to get contacts
    public List<Person> getContacts(OAuth2AccessToken accessToken) throws IOException, GeneralSecurityException {
        // Wrap OAuth2AccessToken in a GoogleCredential
        Credential credential = new GoogleCredential().setAccessToken(accessToken.getTokenValue());

        // Create the PeopleService
        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Google People API").build();

        // Fetch the contacts
        ListConnectionsResponse response = peopleService.people().connections()
                .list("people/me")
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        List<Person> connections = response.getConnections();

        return connections;
    }

    public void createContact(OAuth2AccessToken accessToken, Person newContact, String email) throws IOException, GeneralSecurityException {
        // Wrap OAuth2AccessToken in a GoogleCredential
        Credential credential = new GoogleCredential().setAccessToken(accessToken.getTokenValue());

        // Create the PeopleService
        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Google People API").build();

        // Set email for the new contact
        newContact.setEmailAddresses(Collections.singletonList(new EmailAddress().setValue(email)));

        // Create the contact
        peopleService.people().createContact(newContact).execute();
    }

    public void deleteContact(OAuth2AccessToken accessToken, String resourceName) throws IOException, GeneralSecurityException {
        // Wrap OAuth2AccessToken in a GoogleCredential
        Credential credential = new GoogleCredential().setAccessToken(accessToken.getTokenValue());

        // Create the PeopleService
        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Google People API").build();

        // Delete the contact
        peopleService.people().deleteContact(resourceName).execute();
    }
}
