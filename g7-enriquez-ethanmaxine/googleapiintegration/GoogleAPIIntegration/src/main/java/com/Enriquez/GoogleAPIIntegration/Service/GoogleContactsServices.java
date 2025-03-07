package com.Enriquez.GoogleAPIIntegration.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.people.v1.model.*;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;


import java.io.IOException;
import java.security.GeneralSecurityException;

import java.util.List;


@Service
public class GoogleContactsServices {

    
    private static final String APPLICATION_NAME = "GOOGLEAPIINTEGRATION";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final OAuth2AuthorizedClientService authorizedClientService;

    public GoogleContactsServices(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    public List<Person> getUserContacts(OAuth2AuthenticationToken authentication) throws GeneralSecurityException, IOException {
        // Get access token from authenticated user
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        if (authorizedClient == null) {
            throw new IllegalStateException("No OAuth2AuthorizedClient found.");
        }

        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        // Initialize Google People API Service
        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken)
        ).setApplicationName(APPLICATION_NAME).build();

        // Fetch contacts from Google People API
        ListConnectionsResponse response = peopleService.people().connections()
                .list("people/me")
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        List<Person> connections = response.getConnections();
        return connections;
    }

    public Person createContact(OAuth2AuthenticationToken authentication, Person contact) throws GeneralSecurityException, IOException {
        // Get access token from authenticated user
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        if (authorizedClient == null) {
            throw new IllegalStateException("No OAuth2AuthorizedClient found.");
        }

        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        // Initialize Google People API Service
        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken)
        ).setApplicationName(APPLICATION_NAME).build();

        Person createdContact = peopleService.people().createContact(contact).execute();

        return createdContact;
    }

    public Person updateContact(OAuth2AuthenticationToken authentication, String resourceName, Person updatedContact) throws GeneralSecurityException, IOException {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );
        if (authorizedClient == null) {
            throw new IllegalStateException("OAuth2AuthorizedClient not found for user: " + authentication.getName());
        }

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalStateException("Access token is missing or invalid.");
        }

        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken)
        ).setApplicationName(APPLICATION_NAME).build();

        Person existingContact = peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        // Set the fields to be updated from the updatedContact
        existingContact.setNames(updatedContact.getNames());
        existingContact.setEmailAddresses(updatedContact.getEmailAddresses());
        existingContact.setPhoneNumbers(updatedContact.getPhoneNumbers());

        Person result = peopleService.people().updateContact(resourceName, existingContact)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        return result;
    }

    public void deleteContact(OAuth2AuthenticationToken authentication, String resourceName) throws GeneralSecurityException, IOException {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
            authentication.getAuthorizedClientRegistrationId(),
            authentication.getName()
        );
        
        if (authorizedClient == null) {
            throw new IllegalStateException("OAuth2AuthorizedClient not found for user: " + authentication.getName());
        }
        
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalStateException("Access token is missing or invalid.");
        }

                PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken)
        ).setApplicationName(APPLICATION_NAME).build();

        try {
            peopleService.people().deleteContact(resourceName).execute();
        } catch (GoogleJsonResponseException e) {
            System.err.println("Error deleting contact: " + e.getDetails().getMessage());
            throw new RuntimeException("Failed to delete contact: " + e.getDetails().getMessage());
        }

    }

}
