package com.renato.oauth2login.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContactService {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private PeopleService peopleService(OAuth2AccessToken accessToken) throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new PeopleService.Builder(
                httpTransport,
                JSON_FACTORY,
                new HttpRequestInitializer() {
                    public void initialize(HttpRequest httpRequest) {
                        httpRequest.getHeaders().setAuthorization("Bearer " + accessToken.getTokenValue());
                    }
                }
        ).setApplicationName("Google Contacts API").build();
    }

    private OAuth2AccessToken getAccessToken(String principalName) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("google", principalName);
        if (client == null) {
            throw new RuntimeException("OAuth2 client not found for user: " + principalName);
        }
        return client.getAccessToken();
    }

    // Retrieve all contacts
    public List<Person> getContacts(String principalName) throws GeneralSecurityException, IOException {
        OAuth2AccessToken accessToken = getAccessToken(principalName);
        PeopleService peopleService = peopleService(accessToken);

        try {
            ListConnectionsResponse response = peopleService.people().connections()
                    .list("people/me")
                    .setPageSize(100)
                    .setPersonFields("names,emailAddresses,phoneNumbers")
                    .execute();

            return response.getConnections() != null ? response.getConnections() : new ArrayList<>();
        } catch (IOException e) {
            throw new IOException("Error retrieving contacts: " + e.getMessage(), e);
        }
    }

    // Add a new contact
    public Person addContact(String principalName, String firstName, String lastName) throws GeneralSecurityException, IOException {
        OAuth2AccessToken accessToken = getAccessToken(principalName);
        PeopleService peopleService = peopleService(accessToken);

        try {
            Person contactToCreate = new Person();
            List<Name> names = new ArrayList<>();
            names.add(new Name().setGivenName(firstName).setFamilyName(lastName));
            contactToCreate.setNames(names);

            return peopleService.people().createContact(contactToCreate).execute();
        } catch (IOException e) {
            throw new IOException("Error adding contact: " + e.getMessage(), e);
        }
    }

    // Update an existing contact
    public Person updateContact(String principalName, String resourceName, String firstName, String lastName) throws GeneralSecurityException, IOException {
        OAuth2AccessToken accessToken = getAccessToken(principalName);
        PeopleService peopleService = peopleService(accessToken);

        try {
            Person contactToUpdate = peopleService.people().get(resourceName).execute();
            List<Name> names = new ArrayList<>();
            names.add(new Name().setGivenName(firstName).setFamilyName(lastName));
            contactToUpdate.setNames(names);

            return peopleService.people().updateContact(resourceName, contactToUpdate)
                    .setUpdatePersonFields("names")
                    .execute();
        } catch (IOException e) {
            throw new IOException("Error updating contact: " + e.getMessage(), e);
        }
    }

    // Remove a contact
    public void removeContact(String principalName, String resourceName) throws GeneralSecurityException, IOException {
        OAuth2AccessToken accessToken = getAccessToken(principalName);
        PeopleService peopleService = peopleService(accessToken);

        try {
            peopleService.people().deleteContact(resourceName).execute();
        } catch (IOException e) {
            throw new IOException("Error deleting contact: " + e.getMessage(), e);
        }
    }
}
