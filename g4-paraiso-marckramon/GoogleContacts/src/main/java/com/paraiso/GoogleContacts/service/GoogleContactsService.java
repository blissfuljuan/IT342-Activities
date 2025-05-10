package com.paraiso.GoogleContacts.service;

import com.paraiso.GoogleContacts.model.Contacts;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
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
    
    private final OAuth2AuthorizedClientManager clientManager;
    
    @Autowired
    public GoogleContactsService(OAuth2AuthorizedClientManager clientManager) {
        this.clientManager = clientManager;
    }

    private String getAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            throw new IllegalStateException("Not authenticated with OAuth2");
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(
                oauthToken.getAuthorizedClientRegistrationId())
                .principal(authentication)
                .build();

        var authorizedClient = clientManager.authorize(authorizeRequest);
        if (authorizedClient == null) {
            throw new IllegalStateException("Client not authorized");
        }

        return authorizedClient.getAccessToken().getTokenValue();
    }

    public List<Contacts> getContacts() throws IOException {
        String accessToken = getAccessToken();
        
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        PeopleService peopleService = new PeopleService.Builder(
                new NetHttpTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        PeopleService.People.Connections.List request = peopleService.people().connections()
                .list("people/me")
                .setPersonFields(String.join(",", PERSON_FIELDS));

        ListConnectionsResponse response = request.execute();
        List<Contacts> contactsList = new ArrayList<>();
        
        for (Person person : response.getConnections()) {
            Contacts contact = new Contacts();
            contact.setResourceName(person.getResourceName());
            contact.setName(getPersonName(person));
            contact.setEmail(getPersonEmail(person));
            contact.setPhoneNumber(getPersonPhoneNumber(person));
            contactsList.add(contact);
        }

        return contactsList;
    }
    
    public List<Person> getConnectionsAsPeople() throws IOException {
        return getConnections();
    }
    
    private List<Person> getConnections() throws IOException {
        String accessToken = getAccessToken();
        
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        PeopleService peopleService = new PeopleService.Builder(
                new NetHttpTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        PeopleService.People.Connections.List request = peopleService.people().connections()
                .list("people/me")
                .setPersonFields(String.join(",", PERSON_FIELDS));

        ListConnectionsResponse response = request.execute();
        return response.getConnections() != null ? response.getConnections() : new ArrayList<>();
    }
    
    public void addContact(String name, String email, String phoneNumber) throws IOException {
        String accessToken = getAccessToken();
        
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        PeopleService peopleService = new PeopleService.Builder(
                new NetHttpTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        Person newPerson = new Person();
        
        Name personName = new Name();
        personName.setDisplayName(name);
        personName.setGivenName(name);
        newPerson.setNames(Collections.singletonList(personName));

        if (email != null && !email.isEmpty()) {
            EmailAddress emailAddress = new EmailAddress();
            emailAddress.setValue(email);
            emailAddress.setType("home");
            newPerson.setEmailAddresses(Collections.singletonList(emailAddress));
        }

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            PhoneNumber personPhone = new PhoneNumber();
            personPhone.setValue(phoneNumber);
            personPhone.setType("mobile");
            newPerson.setPhoneNumbers(Collections.singletonList(personPhone));
        }

        peopleService.people().createContact(newPerson).execute();
    }
    
    public void updateContact(String resourceName, String name, String email, String phoneNumber) throws IOException {
        String accessToken = getAccessToken();
        
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        PeopleService peopleService = new PeopleService.Builder(
                new NetHttpTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        // First, get the existing contact to get its etag
        Person existingContact = peopleService.people().get(resourceName)
                .setPersonFields(String.join(",", PERSON_FIELDS))
                .execute();

        // Create a person for update
        Person updatePerson = new Person();
        updatePerson.setEtag(existingContact.getEtag()); // Set the etag

        // Update name if provided
        if (name != null && !name.isEmpty()) {
            Name personName = new Name()
                    .setDisplayName(name)
                    .setGivenName(name);
            updatePerson.setNames(Arrays.asList(personName));
        }

        // Update email if provided
        if (email != null && !email.isEmpty()) {
            EmailAddress emailAddress = new EmailAddress()
                    .setValue(email)
                    .setType("home");
            updatePerson.setEmailAddresses(Arrays.asList(emailAddress));
        }

        // Update phone if provided
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            PhoneNumber phone = new PhoneNumber()
                    .setValue(phoneNumber)
                    .setType("home");
            updatePerson.setPhoneNumbers(Arrays.asList(phone));
        }

        // Determine which fields to update
        List<String> updatePersonFields = new ArrayList<>();
        if (name != null && !name.isEmpty()) updatePersonFields.add("names");
        if (email != null && !email.isEmpty()) updatePersonFields.add("emailAddresses");
        if (phoneNumber != null && !phoneNumber.isEmpty()) updatePersonFields.add("phoneNumbers");

        // Validate that at least one field is being updated
        if (updatePersonFields.isEmpty()) {
            throw new RuntimeException("No fields provided for update.");
        }

        // Perform the update
        peopleService.people().updateContact(resourceName, updatePerson)
                .setUpdatePersonFields(String.join(",", updatePersonFields))
                .execute();
    }

    public void deleteContact(String resourceName) throws IOException {
        String accessToken = getAccessToken();
        
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        PeopleService peopleService = new PeopleService.Builder(
                new NetHttpTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        peopleService.people().deleteContact(resourceName).execute();
    }
    
    public Person getPersonById(String resourceName) throws IOException {
        String accessToken = getAccessToken();
        
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        PeopleService peopleService = new PeopleService.Builder(
                new NetHttpTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        return peopleService.people().get(resourceName)
                .setPersonFields(String.join(",", PERSON_FIELDS))
                .execute();
    }
    
    public Contacts getContactById(String resourceName) throws IOException {
        Person person = getPersonById(resourceName);
        
        Contacts contact = new Contacts();
        contact.setResourceName(resourceName);
        contact.setName(getPersonName(person));
        contact.setEmail(getPersonEmail(person));
        contact.setPhoneNumber(getPersonPhoneNumber(person));
        
        return contact;
    }
    
    private String getPersonName(Person person) {
        List<Name> names = person.getNames();
        if (names != null && !names.isEmpty()) {
            return names.get(0).getDisplayName();
        }
        return "";
    }
    
    private String getPersonEmail(Person person) {
        List<EmailAddress> emailAddresses = person.getEmailAddresses();
        if (emailAddresses != null && !emailAddresses.isEmpty()) {
            return emailAddresses.get(0).getValue();
        }
        return "";
    }
    
    private String getPersonPhoneNumber(Person person) {
        List<PhoneNumber> phoneNumbers = person.getPhoneNumbers();
        if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            return phoneNumbers.get(0).getValue();
        }
        return "";
    }

    public Person createContact(String name, String email, String phoneNumber) throws IOException {
        String accessToken = getAccessToken();
        
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        PeopleService peopleService = new PeopleService.Builder(
                new NetHttpTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        Person contactToCreate = new Person();
        
        // Set name
        Name contactName = new Name()
                .setGivenName(name);
        contactToCreate.setNames(Collections.singletonList(contactName));
        
        // Set email if provided
        if (email != null && !email.isEmpty()) {
            EmailAddress emailAddress = new EmailAddress()
                    .setValue(email)
                    .setType("home");
            contactToCreate.setEmailAddresses(Collections.singletonList(emailAddress));
        }
        
        // Set phone number if provided
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            PhoneNumber phone = new PhoneNumber()
                    .setValue(phoneNumber)
                    .setType("home");
            contactToCreate.setPhoneNumbers(Collections.singletonList(phone));
        }

        // Create the contact
        Person createdContact = peopleService.people()
                .createContact(contactToCreate)
                .execute();

        return createdContact;
    }
} 