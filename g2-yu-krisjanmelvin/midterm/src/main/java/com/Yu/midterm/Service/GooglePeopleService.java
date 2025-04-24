package com.Yu.midterm.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import com.Yu.midterm.Model.Contact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GooglePeopleService {
    private static final Logger logger = LoggerFactory.getLogger(GooglePeopleService.class);
    private final OAuth2AuthorizedClientService clientService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    public GooglePeopleService(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    private PeopleService getPeopleService(String userId) throws GeneralSecurityException, IOException {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient("google", userId);
        if (client == null) {
            throw new GeneralSecurityException("No authorized client found for userId: " + userId);
        }
        OAuth2AccessToken accessToken = client.getAccessToken();

        Credential credential = new GoogleCredential().setAccessToken(accessToken.getTokenValue());
        return new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("Google Contacts App")
                .build();
    }

    public List<Contact> getContacts(String userId) throws GeneralSecurityException, IOException {
        PeopleService peopleService = getPeopleService(userId);
        List<Person> allPersons = new ArrayList<>();
        String nextPageToken = null;

        do {
            ListConnectionsResponse response = peopleService.people().connections()
                    .list("people/me")
                    .setPersonFields("names,emailAddresses,phoneNumbers")
                    .setPageSize(1000)
                    .setPageToken(nextPageToken)
                    .execute();

            List<Person> persons = response.getConnections();
            if (persons != null) {
                allPersons.addAll(persons);
            }
            nextPageToken = response.getNextPageToken();
            logger.info("Fetched {} contacts on this page, total so far: {}, nextPageToken: {}",
                    persons != null ? persons.size() : 0, allPersons.size(), nextPageToken);
        } while (nextPageToken != null);

        if (allPersons.isEmpty()) {
            logger.warn("No contacts found for userId: {}", userId);
            return Collections.emptyList();
        }

        List<Contact> contacts = allPersons.stream()
                .map(this::mapPersonToContact)
                .collect(Collectors.toList());
        logger.info("Total contacts fetched for userId: {} - {}", userId, contacts.size());
        return contacts;
    }

    public Contact addContact(String userId, Contact contact) throws GeneralSecurityException, IOException {
        PeopleService peopleService = getPeopleService(userId);
        Person person = mapContactToPerson(contact);
        Person createdPerson = peopleService.people().createContact(person).execute();
        logger.info("Added contact for userId: {} - resourceName: {}", userId, createdPerson.getResourceName());
        return mapPersonToContact(createdPerson);
    }

    public Contact updateContact(String userId, String resourceName, Contact contact) throws GeneralSecurityException, IOException {
        PeopleService peopleService = getPeopleService(userId);
        Person person = mapContactToPerson(contact);

        Person existingPerson = peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();
        if (existingPerson == null) {
            logger.error("Failed to retrieve existing person for resourceName: {}", resourceName);
            throw new IOException("Failed to retrieve existing person for update");
        }
        person.setEtag(existingPerson.getEtag());

        Person updatedPerson = peopleService.people().updateContact(resourceName, person)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();
        logger.info("Updated contact - resourceName: {}", updatedPerson.getResourceName());
        return mapPersonToContact(updatedPerson);
    }

    public void deleteContact(String userId, String resourceName) throws GeneralSecurityException, IOException {
        PeopleService peopleService = getPeopleService(userId);
        peopleService.people().deleteContact(resourceName).execute();
        logger.info("Deleted contact - resourceName: {} for userId: {}", resourceName, userId);
    }

    public Contact getContact(String userId, String resourceName) throws GeneralSecurityException, IOException {
        PeopleService peopleService = getPeopleService(userId);
        try {
            Person person = peopleService.people().get(resourceName)
                    .setPersonFields("names,emailAddresses,phoneNumbers")
                    .execute();
            if (person == null) {
                logger.warn("Google API returned null Person for resourceName: {}", resourceName);
                return new Contact();
            }
            return mapPersonToContact(person);
        } catch (IOException e) {
            logger.error("Error fetching contact with resourceName: {} - {}", resourceName, e.getMessage());
            Contact fallbackContact = new Contact();
            fallbackContact.setResourceName(resourceName);
            return fallbackContact;
        }
    }

    private Contact mapPersonToContact(Person person) {
        Contact contact = new Contact();
        contact.setResourceName(person.getResourceName() != null ? person.getResourceName() : "");

        if (person.getNames() != null && !person.getNames().isEmpty() && person.getNames().get(0) != null) {
            contact.setFirstName(person.getNames().get(0).getGivenName() != null ? person.getNames().get(0).getGivenName() : "");
            contact.setLastName(person.getNames().get(0).getFamilyName() != null ? person.getNames().get(0).getFamilyName() : "");
        }
        if (person.getEmailAddresses() != null && !person.getEmailAddresses().isEmpty()) {
            contact.setEmails(person.getEmailAddresses().stream()
                    .map(EmailAddress::getValue)
                    .filter(value -> value != null && !value.isEmpty())
                    .collect(Collectors.toList()));
        }
        if (person.getPhoneNumbers() != null && !person.getPhoneNumbers().isEmpty()) {
            contact.setPhoneNumbers(person.getPhoneNumbers().stream()
                    .map(PhoneNumber::getValue)
                    .filter(value -> value != null && !value.isEmpty())
                    .collect(Collectors.toList()));
        }
        return contact;
    }

    private Person mapContactToPerson(Contact contact) {
        Person person = new Person();
        if (contact.getFirstName() != null || contact.getLastName() != null) {
            Name name = new Name()
                    .setGivenName(contact.getFirstName() != null && !contact.getFirstName().isEmpty() ? contact.getFirstName() : null)
                    .setFamilyName(contact.getLastName() != null && !contact.getLastName().isEmpty() ? contact.getLastName() : null);
            person.setNames(Collections.singletonList(name));
        } else {
            Name name = new Name().setGivenName("Unknown"); // Minimum requirement
            person.setNames(Collections.singletonList(name));
        }
        if (contact.getEmails() != null && !contact.getEmails().isEmpty()) {
            List<EmailAddress> emails = contact.getEmails().stream()
                    .filter(email -> email != null && !email.isEmpty())
                    .map(email -> new EmailAddress().setValue(email))
                    .collect(Collectors.toList());
            person.setEmailAddresses(emails);
        }
        if (contact.getPhoneNumbers() != null && !contact.getPhoneNumbers().isEmpty()) {
            List<PhoneNumber> phones = contact.getPhoneNumbers().stream()
                    .filter(phone -> phone != null && !phone.isEmpty())
                    .map(phone -> new PhoneNumber().setValue(phone))
                    .collect(Collectors.toList());
            person.setPhoneNumbers(phones);
        }
        return person;
    }
}