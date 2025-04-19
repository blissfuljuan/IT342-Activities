package com.Sacamay.midterm.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.PeopleService.Builder;
import com.google.api.services.people.v1.model.*;
import com.Sacamay.midterm.model.Contact;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GooglePeopleService {

    public List<Contact> getContacts(OAuth2AuthorizedClient client) throws IOException, GeneralSecurityException {
        PeopleService service = createPeopleService(client);
        List<Person> allConnections = new ArrayList<>();
        String pageToken = null;

        do {
            ListConnectionsResponse response = null;
            try {
                response = service.people().connections()
                        .list("people/me")
                        .setPersonFields("names,emailAddresses,phoneNumbers")
                        .setPageSize(1000)  // Maximum allowed page size
                        .setPageToken(pageToken)
                        .execute();
            } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
                System.err.println("Error fetching contacts: " + e.getDetails().getMessage());
                System.err.println("Full error response: " + e.getDetails());
                if (e.getDetails().getCode() == 401) {
                    System.err.println("Authentication error. Check if the access token is valid and has the correct scope.");
                } else if (e.getDetails().getCode() == 403) {
                    System.err.println("Permission error. Check if the application has the correct scope.");
                } else {
                    System.err.println("An unknown error has occurred while retrieving the contacts.");
                }
                throw e;
            } catch (IOException e) {
                System.err.println("IO error occurred when fetching contacts: " + e.getMessage());
                throw e;
            }

            if (response.getConnections() != null) {
                allConnections.addAll(response.getConnections());
            }
            
            pageToken = response.getNextPageToken();
        } while (pageToken != null);

        if (allConnections.isEmpty()) {
            return Collections.emptyList();
        }

        return allConnections.stream()
                .map(person -> new Contact(
                        person.getNames() != null && !person.getNames().isEmpty() ? person.getNames().get(0).getGivenName() : "",
                        person.getNames() != null && !person.getNames().isEmpty() ? person.getNames().get(0).getFamilyName() : "",
                        person.getEmailAddresses() != null && !person.getEmailAddresses().isEmpty() ? person.getEmailAddresses().get(0).getValue() : "",
                        person.getPhoneNumbers() != null && !person.getPhoneNumbers().isEmpty() ? person.getPhoneNumbers().get(0).getValue() : "",
                        person.getResourceName()
                ))
                .collect(Collectors.toList());
    }

    public Contact createContact(OAuth2AuthorizedClient client, Contact contact) throws IOException, GeneralSecurityException {
        PeopleService service = createPeopleService(client);
        Person person = new Person()
                .setNames(Collections.singletonList(new Name()
                        .setGivenName(contact.getFirstName())
                        .setFamilyName(contact.getLastName())))
                .setEmailAddresses(Collections.singletonList(new EmailAddress().setValue(contact.getEmail())))
                .setPhoneNumbers(Collections.singletonList(new PhoneNumber().setValue(contact.getPhoneNumber())));

        Person created = service.people().createContact(person).execute();
        return new Contact(
                created.getNames().get(0).getGivenName(),
                created.getNames().get(0).getFamilyName(),
                created.getEmailAddresses().get(0).getValue(),
                created.getPhoneNumbers().get(0).getValue(),
                created.getResourceName()
        );
    }

    public Contact updateContact(OAuth2AuthorizedClient client, String resourceName, Contact contact) throws IOException, GeneralSecurityException {
        PeopleService service = createPeopleService(client);

        try {
            // Delete the old contact
            service.people().deleteContact(resourceName).execute();

            // Create a new contact with the updated information
            Person person = new Person()
                    .setNames(Collections.singletonList(new Name()
                            .setGivenName(contact.getFirstName())
                            .setFamilyName(contact.getLastName())))
                    .setEmailAddresses(Collections.singletonList(new EmailAddress().setValue(contact.getEmail())))
                    .setPhoneNumbers(Collections.singletonList(new PhoneNumber().setValue(contact.getPhoneNumber())));

            // Create new contact
            Person created = service.people().createContact(person).execute();

            return new Contact(
                    created.getNames().get(0).getGivenName(),
                    created.getNames().get(0).getFamilyName(),
                    created.getEmailAddresses().get(0).getValue(),
                    created.getPhoneNumbers().get(0).getValue(),
                    created.getResourceName()
            );
        } catch (Exception e) {
            System.err.println("Error updating contact: " + e.getMessage());
            throw e;
        }
    }

    public void deleteContact(OAuth2AuthorizedClient client, String resourceName) throws IOException, GeneralSecurityException {
        PeopleService service = createPeopleService(client);
        service.people().deleteContact(resourceName).execute();
    }

    private PeopleService createPeopleService(OAuth2AuthorizedClient client) throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        HttpRequestInitializer requestInitializer = request -> {
            com.google.api.client.auth.oauth2.Credential credential = new com.google.api.client.auth.oauth2.Credential(com.google.api.client.auth.oauth2.BearerToken.authorizationHeaderAccessMethod());
            credential.setAccessToken(client.getAccessToken().getTokenValue());
            credential.initialize(request);
        };

        return new Builder(httpTransport, jsonFactory, requestInitializer)
                .setApplicationName("midterm")
                .build();
    }
}
