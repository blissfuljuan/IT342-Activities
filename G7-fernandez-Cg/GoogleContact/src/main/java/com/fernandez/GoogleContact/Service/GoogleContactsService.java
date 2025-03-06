package com.fernandez.GoogleContact.Service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GoogleContactsService {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "Google Contacts Integration";
    private static final List<String> PERSON_FIELDS = Arrays.asList("names", "emailAddresses", "phoneNumbers");

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    private PeopleService getPeopleService(OAuth2User principal) {
        // Get the client registration ID (should be "google" based on your configuration)
        String clientRegistrationId = "google";

        // Get the name/identifier from principal
        String name = principal.getName();

        // Load the authorized client
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                clientRegistrationId,
                name
        );

        if (client == null) {
            throw new RuntimeException("OAuth2 client is null. User may not be properly authenticated.");
        }

        String accessToken = client.getAccessToken().getTokenValue();

        if (accessToken == null) {
            throw new RuntimeException("Access token is null");
        }

        // For debugging
        System.out.println("Access Token: " + accessToken);

        // Build and return the PeopleService
        return new PeopleService.Builder(
                new NetHttpTransport(),
                JSON_FACTORY,
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    public List<Person> getConnectionsAsPeople(OAuth2User principal) {
        if (principal == null) {
            throw new RuntimeException("User is not authenticated");
        }

        try {
            PeopleService peopleService = getPeopleService(principal);
            return getConnections(peopleService);
        } catch (IOException e) {
            throw new RuntimeException("Error fetching contacts: " + e.getMessage(), e);
        }
    }

    private List<Person> getConnections(PeopleService peopleService) throws IOException {
        ListConnectionsResponse response = peopleService.people().connections()
                .list("people/me")
                .setPersonFields(String.join(",", PERSON_FIELDS))
                .execute();

        return response.getConnections() != null ? response.getConnections() : new ArrayList<>();
    }

    public void addContact(OAuth2User principal, String name, String email, String phoneNumber) {
        try {
            PeopleService peopleService = getPeopleService(principal);

            // Create a new person
            Person newPerson = new Person();

            // Add name
            Name personName = new Name();
            personName.setDisplayName(name);
            personName.setGivenName(name);
            newPerson.setNames(Arrays.asList(personName));

            // Add email if provided
            if (email != null && !email.isEmpty()) {
                EmailAddress emailAddress = new EmailAddress();
                emailAddress.setValue(email);
                emailAddress.setType("home");
                newPerson.setEmailAddresses(Arrays.asList(emailAddress));
            }

            // Add phone if provided
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                PhoneNumber personPhone = new PhoneNumber();
                personPhone.setValue(phoneNumber);
                personPhone.setType("mobile");
                newPerson.setPhoneNumbers(Arrays.asList(personPhone));
            }

            // Create the contact
            peopleService.people().createContact(newPerson).execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed to add contact: " + e.getMessage(), e);
        }
    }

    public void updateContact(OAuth2User principal, String resourceName, Person updatePerson, List<String> updatePersonFields) {
        try {
            PeopleService peopleService = getPeopleService(principal);

            // Perform the update
            peopleService.people().updateContact(resourceName, updatePerson)
                    .setUpdatePersonFields(String.join(",", updatePersonFields))
                    .execute();

            System.out.println("Contact updated successfully: {}"+ resourceName);

        } catch (IOException e) {
            System.out.println("Failed to update contact: {}"+ e.getMessage()+ e);
            throw new RuntimeException("Failed to update contact: " + e.getMessage(), e);
        }
    }

    public void deleteContact(OAuth2User principal, String resourceName) {
        try {
            PeopleService peopleService = getPeopleService(principal);
            peopleService.people().deleteContact(resourceName).execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete contact: " + e.getMessage(), e);
        }
    }

    public Person getPersonById(OAuth2User principal, String resourceName) {
        try {
            PeopleService peopleService = getPeopleService(principal);
            return peopleService.people().get(resourceName)
                    .setPersonFields(String.join(",", PERSON_FIELDS))
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch contact: " + e.getMessage(), e);
        }
    }
}