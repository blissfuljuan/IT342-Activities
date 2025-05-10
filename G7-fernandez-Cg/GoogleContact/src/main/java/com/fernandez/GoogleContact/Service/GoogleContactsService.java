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
import java.util.Collections;
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
    public void addContact(OAuth2User principal, String name, List<String> emails, List<String> phoneNumbers) {
        try {
            PeopleService peopleService = getPeopleService(principal);

            // Create a new person
            Person newPerson = new Person();

            // Add name
            Name personName = new Name();
            personName.setDisplayName(name);
            personName.setGivenName(name);
            newPerson.setNames(Collections.singletonList(personName));

            // Add emails if provided
            if (emails != null && !emails.isEmpty()) {
                List<EmailAddress> emailAddresses = new ArrayList<>();
                for (String email : emails) {
                    if (email != null && !email.isEmpty()) {
                        EmailAddress emailAddress = new EmailAddress();
                        emailAddress.setValue(email);
                        emailAddress.setType("home");
                        emailAddresses.add(emailAddress);
                    }
                }
                newPerson.setEmailAddresses(emailAddresses);
            }

            // Add phone numbers if provided
            if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
                List<PhoneNumber> phoneList = new ArrayList<>();
                for (String phone : phoneNumbers) {
                    if (phone != null && !phone.isEmpty()) {
                        PhoneNumber personPhone = new PhoneNumber();
                        personPhone.setValue(phone);
                        personPhone.setType("mobile");
                        phoneList.add(personPhone);
                    }
                }
                newPerson.setPhoneNumbers(phoneList);
            }

            // Create the contact
            peopleService.people().createContact(newPerson).execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed to add contact: " + e.getMessage(), e);
        }
    }

    public void updateContact(OAuth2User principal, String resourceName, Person updatePerson, List<String> updatePersonFields) {
        try {
            if (updatePerson == null) {
                throw new IllegalArgumentException("updatePerson object cannot be null.");
            }

            if (updatePersonFields == null || updatePersonFields.isEmpty()) {
                throw new IllegalArgumentException("No fields specified for update.");
            }

            PeopleService peopleService = getPeopleService(principal);

            // Ensure the fields to be updated are properly formatted
            String fieldsToUpdate = String.join(",", updatePersonFields);

            // Perform the update
            peopleService.people()
                    .updateContact(resourceName, updatePerson)
                    .setUpdatePersonFields(fieldsToUpdate)  // Specify which fields to update
                    .setPersonFields("emailAddresses,phoneNumbers,names")  // Include all necessary fields
                    .execute();

        } catch (IOException e) {
            throw new RuntimeException("Failed to update contact due to I/O error: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Unexpected error while updating contact: " + e.getMessage(), e);
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