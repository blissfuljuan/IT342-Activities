package com.bayona.oauth2login.service;

import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Logger;

@Service
public class GooglePeopleService {

    private static final String APPLICATION_NAME = "OAuth2Login";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Logger logger = Logger.getLogger(GooglePeopleService.class.getName());

    private final OAuth2AuthorizedClientService authorizedClientService;

    public GooglePeopleService(OAuth2AuthorizedClientService authorizedClientService) {
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
        if (connections != null) {
            for (Person person : connections) {
                logger.info("Fetched contact: " + person.toPrettyString());
                if (person.getNames() != null && !person.getNames().isEmpty()) {
                    logger.info("Fetched contact name: " + person.getNames().get(0).getDisplayName());
                } else {
                    logger.info("Fetched contact with no name");
                }
            }
        } else {
            logger.info("No contacts found");
        }

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

        // Create a new contact
        logger.info("Creating contact with name: " + contact.getNames().get(0).getDisplayName());
        Person createdContact = peopleService.people().createContact(contact).execute();
        logger.info("Created contact with resource name: " + createdContact.getResourceName());

        return createdContact;
    }

    public Person updateContact(OAuth2AuthenticationToken authentication, String resourceName, Person updatedContact) throws GeneralSecurityException, IOException {
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
    
        // Update the contact
        logger.info("Updating contact with resource name: " + resourceName);
        
        // Get the existing contact first
        Person existingContact = peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();
        
        // Set the fields to be updated from the updatedContact
        existingContact.setNames(updatedContact.getNames());
        existingContact.setEmailAddresses(updatedContact.getEmailAddresses());
        existingContact.setPhoneNumbers(updatedContact.getPhoneNumbers());
        
        // Perform the update
        Person result = peopleService.people().updateContact(resourceName, existingContact)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();
                
        logger.info("Updated contact with resource name: " + result.getResourceName());
        if (result.getNames() != null && !result.getNames().isEmpty()) {
            logger.info("Updated contact name: " + result.getNames().get(0).getDisplayName());
        }
    
        return result;
    }

    public void deleteContact(OAuth2AuthenticationToken authentication, String resourceName) throws GeneralSecurityException, IOException {
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

        // Delete the contact
        logger.info("Deleting contact with resource name: " + resourceName);
        peopleService.people().deleteContact(resourceName).execute();
        logger.info("Deleted contact with resource name: " + resourceName);
    }
    public Person getContact(OAuth2AuthenticationToken authentication, String resourceName) 
        throws GeneralSecurityException, IOException {
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

    // Get the specific contact
    logger.info("Fetching contact with resource name: " + resourceName);
    Person contact = peopleService.people().get(resourceName)
            .setPersonFields("names,emailAddresses,phoneNumbers")
            .execute();
            
    if (contact.getNames() != null && !contact.getNames().isEmpty()) {
        logger.info("Fetched contact name: " + contact.getNames().get(0).getDisplayName());
    }
            
    return contact;
}
}