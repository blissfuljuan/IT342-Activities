package com.cabalida.oauth2login.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Service;
import com.google.auth.http.HttpCredentialsAdapter;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleContactsService {

    private static final String APPLICATION_NAME = "IT342-Activities-Cabalida"; // Change this
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CONTACTS_SCOPE = "https://www.googleapis.com/auth/contacts.readonly";

    public List<Person> getContacts(String accessToken) throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null))
                .createScoped(Collections.singleton(CONTACTS_SCOPE));

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials); // Fix here

        PeopleService service = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                requestInitializer // Use fixed request initializer
        ).setApplicationName(APPLICATION_NAME).build();

        // Fetch contacts
        ListConnectionsResponse response = service.people().connections()
                .list("people/me")
                .setPageSize(50)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        return response.getConnections();
    }

    public void addContact(String name, String email, String phone, String accessToken) {
        try {
            GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null))
                    .createScoped(Collections.singleton(CONTACTS_SCOPE));

            PeopleService service = new PeopleService.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            Person newContact = new Person();

            // Add Name
            newContact.setNames(Collections.singletonList(new Name().setGivenName(name)));

            // Add Email
            if (email != null && !email.isEmpty()) {
                newContact.setEmailAddresses(Collections.singletonList(new EmailAddress().setValue(email)));
            }

            // Add Phone
            if (phone != null && !phone.isEmpty()) {
                newContact.setPhoneNumbers(Collections.singletonList(new PhoneNumber().setValue(phone)));
            }

            // Create the contact in Google
            service.people().createContact(newContact).execute();

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public Person getContactById(String contactId, String accessToken) throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null))
                .createScoped(Collections.singleton(CONTACTS_SCOPE));

        PeopleService service = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(APPLICATION_NAME).build();

        return service.people().get(contactId)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();
    }

    public void updateContact(String contactId, String name, String email, String phone, String accessToken) throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null))
                .createScoped(Collections.singleton(CONTACTS_SCOPE));

        PeopleService service = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(APPLICATION_NAME).build();

        Person person = new Person();

        if (!name.isEmpty()) {
            person.setNames(Collections.singletonList(new Name().setGivenName(name)));
        }
        if (email != null && !email.isEmpty()) {
            person.setEmailAddresses(Collections.singletonList(new EmailAddress().setValue(email)));
        }
        if (phone != null && !phone.isEmpty()) {
            person.setPhoneNumbers(Collections.singletonList(new PhoneNumber().setValue(phone)));
        }

        service.people().updateContact(contactId, person)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();
    }

    public void deleteContact(String contactId, String accessToken) throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null))
                .createScoped(Collections.singleton(CONTACTS_SCOPE));

        PeopleService service = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(APPLICATION_NAME).build();

        service.people().deleteContact(contactId).execute();
    }
}

