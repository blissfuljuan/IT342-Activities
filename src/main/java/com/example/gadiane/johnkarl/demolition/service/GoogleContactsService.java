package com.example.gadiane.johnkarl.demolition.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PersonResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
public class GoogleContactsService {

    private static final String APPLICATION_NAME = "demolition";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private PeopleService getPeopleService(Credential credential) throws GeneralSecurityException, IOException {
        return new PeopleService.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<Person> listContacts(Credential credential) throws GeneralSecurityException, IOException {
        PeopleService peopleService = getPeopleService(credential);
        return peopleService.people().connections().list("people/me")
                .setPersonFields("names,emailAddresses")
                .execute()
                .getConnections();
    }

    public Person getContact(Credential credential, String resourceName) throws GeneralSecurityException, IOException {
        PeopleService peopleService = getPeopleService(credential);
        return peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses")
                .execute();
    }

    public Person createContact(Credential credential, Person person) throws GeneralSecurityException, IOException {
        PeopleService peopleService = getPeopleService(credential);
        return peopleService.people().createContact(person).execute();
    }

    public Person updateContact(Credential credential, String resourceName, Person person) throws GeneralSecurityException, IOException {
        PeopleService peopleService = getPeopleService(credential);
        return peopleService.people().updateContact(resourceName, person)
                .setUpdatePersonFields("names,emailAddresses")
                .execute();
    }

    public void deleteContact(Credential credential, String resourceName) throws GeneralSecurityException, IOException {
        PeopleService peopleService = getPeopleService(credential);
        peopleService.people().deleteContact(resourceName).execute();
    }
}