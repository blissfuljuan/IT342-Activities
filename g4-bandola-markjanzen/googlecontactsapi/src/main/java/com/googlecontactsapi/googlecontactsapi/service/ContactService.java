package com.googlecontactsapi.googlecontactsapi.service;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContactService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private static final HttpTransport HTTP_TRANSPORT;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    @Autowired
    public ContactService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    private PeopleService getPeopleService(String principalName) throws IOException, GeneralSecurityException {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("google", principalName);
        GoogleCredential credentials = new GoogleCredential().setAccessToken(client.getAccessToken().getTokenValue());

        return new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                .setApplicationName("Google Contacts")
                .build();
    }

    public List<Person> getAllContactsWithResourceName(String principalName) throws IOException, GeneralSecurityException {
        PeopleService peopleService = getPeopleService(principalName);

        try {
            ListConnectionsResponse response = peopleService.people().connections()
                    .list("people/me")
                    .setPersonFields("names,emailAddresses,phoneNumbers")
                    .execute();

            return response.getConnections();
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            throw e;
        }
    }

    public void addContact(String principalName, Person person) throws IOException, GeneralSecurityException {
        PeopleService peopleService = getPeopleService(principalName);

        try {
            Person newContact = new Person();

            List<Name> names = new ArrayList<>();
            Name name = new Name();
            name.setGivenName(person.getNames().get(0).getGivenName());
            name.setFamilyName(person.getNames().get(0).getFamilyName());
            names.add(name);
            newContact.setNames(names);

            List<EmailAddress> emailAddresses = new ArrayList<>();
            EmailAddress emailAddress = new EmailAddress();
            emailAddress.setValue(person.getEmailAddresses().get(0).getValue());
            emailAddresses.add(emailAddress);
            newContact.setEmailAddresses(emailAddresses);

            List<PhoneNumber> phoneNumbers = new ArrayList<>();
            PhoneNumber phoneNumber = new PhoneNumber();
            phoneNumber.setValue(person.getPhoneNumbers().get(0).getValue());
            phoneNumbers.add(phoneNumber);
            newContact.setPhoneNumbers(phoneNumbers);

            peopleService.people().createContact(newContact).execute();
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            throw e;
        }
    }

    public void updateContact(String principalName, String resourceName, Person person) throws IOException, GeneralSecurityException {
        PeopleService peopleService = getPeopleService(principalName);

        try {
            Person contactToUpdate = peopleService.people().get(resourceName)
                    .setPersonFields("names,emailAddresses,phoneNumbers")
                    .execute();

            contactToUpdate.setNames(person.getNames());
            contactToUpdate.setEmailAddresses(person.getEmailAddresses());
            contactToUpdate.setPhoneNumbers(person.getPhoneNumbers());

            peopleService.people()
                    .updateContact(contactToUpdate.getResourceName(), contactToUpdate)
                    .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                    .execute();
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            throw e;
        }
    }

    public void deleteContact(String principalName, String resourceName) throws IOException, GeneralSecurityException {
        PeopleService peopleService = getPeopleService(principalName);

        try {
            peopleService.people().deleteContact(resourceName).execute();
            System.out.println("Contact deleted: " + resourceName);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            throw e;
        }
    }
}