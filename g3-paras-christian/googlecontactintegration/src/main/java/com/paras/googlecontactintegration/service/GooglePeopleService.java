package com.paras.googlecontactintegration.service;

import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GooglePeopleService {

    // Service to manage OAuth2 authorized clients
    private final OAuth2AuthorizedClientService authorizedClientService;

    // Constructor to initialize the authorized client service
    public GooglePeopleService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    // Method to get the OAuth2 access token
    private String getAccessToken() {
        // Get the current authentication context
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        // Check if the authentication is an instance of OAuth2AuthenticationToken
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            // Load the authorized client using the registration ID and user name
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );
            // If the client is not null, return the access token
            if (client != null) {
                String token = client.getAccessToken().getTokenValue();
                System.out.println("OAuth2 Access Token: " + token); // DEBUGGING TOKEN
                return token;
            }
        }
        // Throw an exception if OAuth2 authentication fails
        throw new RuntimeException("OAuth2 authentication failed!");
    }

    // Method to create a PeopleService instance
    private PeopleService createPeopleService() {
        return new PeopleService.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.gson.GsonFactory(),
                // Set the authorization header with the access token
                request -> request.getHeaders().setAuthorization("Bearer " + getAccessToken())
        ).setApplicationName("Google Contacts App").build();
    }

    // Method to get the list of contacts
    public List<Person> getContacts() throws IOException {
        try {
            // Create a PeopleService instance
            PeopleService peopleService = createPeopleService();
            // List the connections (contacts) for the authenticated user
            ListConnectionsResponse response = peopleService.people().connections()
                    .list("people/me")
                    .setPersonFields("names,emailAddresses,phoneNumbers")
                    .execute();

            // Get the list of connections or an empty list if null
            List<Person> contacts = response.getConnections() != null ? response.getConnections() : new ArrayList<>();
            System.out.println("Fetched Contacts Count: " + contacts.size()); // DEBUGGING CONTACT COUNT
            return contacts;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error fetching contacts: " + e.getMessage());
            throw new IOException("Failed to retrieve contacts from Google People API", e);
        }
    }

    // Method to add a new contact
    public void addContact(String firstName, String lastName, List<String> emails, List<String> phoneNumbers) throws IOException {
        // Create a new Person object with the provided details
        Person contactToCreate = new Person()
                .setNames(List.of(new Name().setGivenName(firstName).setFamilyName(lastName)))
                .setEmailAddresses(emails.stream().map(email -> new EmailAddress().setValue(email)).collect(Collectors.toList()))
                .setPhoneNumbers(phoneNumbers.stream().map(phone -> new PhoneNumber().setValue(phone)).collect(Collectors.toList()));

        // Create a PeopleService instance
        PeopleService peopleService = createPeopleService();
        // Create the contact using the PeopleService
        peopleService.people().createContact(contactToCreate).execute();
    }

    // Method to get a contact by resource name
    public Person getContact(String resourceName) throws IOException {
        // Create a PeopleService instance
        PeopleService peopleService = createPeopleService();
        // Get the contact details using the resource name
        return peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers,metadata")
                .execute();
    }

    // Method to update an existing contact
    public void updateContact(String resourceName, String firstName, String lastName, List<String> emails, List<String> phoneNumbers) throws IOException {
        // Get the existing contact to retrieve the latest etag
        Person existingContact = getContact(resourceName);

        // Create a new Person object with the updated details
        Person contactToUpdate = new Person()
                .setEtag(existingContact.getEtag())
                .setNames(List.of(new Name().setGivenName(firstName).setFamilyName(lastName)))
                .setEmailAddresses(emails.stream().map(email -> new EmailAddress().setValue(email)).collect(Collectors.toList()))
                .setPhoneNumbers(phoneNumbers.stream().map(phone -> new PhoneNumber().setValue(phone)).collect(Collectors.toList()));

        // Create a PeopleService instance
        PeopleService peopleService = createPeopleService();

        // Update the contact using the PeopleService
        peopleService.people().updateContact(resourceName, contactToUpdate)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();
    }

    // Method to delete a contact by resource name
    public void deleteContact(String resourceName) throws IOException {
        // Create a PeopleService instance
        PeopleService peopleService = createPeopleService();
        // Delete the contact using the PeopleService
        peopleService.people().deleteContact(resourceName).execute();
    }

}
