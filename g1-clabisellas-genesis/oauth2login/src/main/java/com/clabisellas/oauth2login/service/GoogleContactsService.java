package com.clabisellas.oauth2login.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
@Slf4j
public class GoogleContactsService {

    private static final String APPLICATION_NAME = "Google Contacts Integration";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    private final OAuth2AuthorizedClientService authorizedClientService;

    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    
    private PeopleService getPeopleService(OAuth2AuthenticationToken authentication) throws GeneralSecurityException, IOException {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), 
                authentication.getName());
        
                if (client == null || client.getAccessToken() == null) {
                    throw new IllegalStateException("No authorized client or access token found!");
                }
        
        String accessToken = client.getAccessToken().getTokenValue();
        System.out.println("OAuth2 Access Token: " + accessToken); 
        
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        
        HttpRequestInitializer requestInitializer = request -> {
            request.getHeaders().setAuthorization("Bearer " + accessToken);
        };
        
        return new PeopleService.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<Person> getContacts(OAuth2AuthenticationToken authentication) {
        try {
            PeopleService peopleService = getPeopleService(authentication);
            
            ListConnectionsResponse response = peopleService.people().connections()
                    .list("people/me")
                    .setPersonFields("names,emailAddresses,phoneNumbers,photos")
                    .execute();
            
            return response.getConnections() != null ? response.getConnections() : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error retrieving contacts", e);
            return Collections.emptyList();
        }
    }

    public Person getContact(OAuth2AuthenticationToken authentication, String resourceName) {
        try {
            PeopleService peopleService = getPeopleService(authentication);
            
            return peopleService.people().get(resourceName)
                    .setPersonFields("names,emailAddresses,phoneNumbers,photos")
                    .execute();
                    
        } catch (Exception e) {
            log.error("Error retrieving contact: " + resourceName, e);
            return null;
        }
    }

    public Person createContact(OAuth2AuthenticationToken authentication, Person contact) {
        try {
            PeopleService peopleService = getPeopleService(authentication);
            return peopleService.people().createContact(contact).execute();
        } catch (Exception e) {
            log.error("Error creating contact", e);
            if (e instanceof com.google.api.client.googleapis.json.GoogleJsonResponseException) {
                com.google.api.client.googleapis.json.GoogleJsonResponseException googleException = 
                    (com.google.api.client.googleapis.json.GoogleJsonResponseException) e;
                log.error("Google API Error: " + googleException.getDetails());
            }
            return null;
        }
    }

    public Person updateContact(OAuth2AuthenticationToken authentication, String resourceName, Person contact, String updatePersonFields) {
        try {
            PeopleService peopleService = getPeopleService(authentication);
            
            return peopleService.people().updateContact(resourceName, contact)
                    .setUpdatePersonFields(updatePersonFields)
                    .execute();
            
        } catch (Exception e) {
            log.error("Error updating contact: " + resourceName, e);
            return null;
        }
    }

    public void deleteContact(OAuth2AuthenticationToken authentication, String resourceName) {
        try {
            PeopleService peopleService = getPeopleService(authentication);
            
            peopleService.people().deleteContact(resourceName).execute();
            
        } catch (Exception e) {
            log.error("Error deleting contact: " + resourceName, e);
        }
    }

    // Helper method to create a new Person object with basic details
    public Person createPersonObject(String firstName, String lastName, String email, String phoneNumber) {
        Person person = new Person();
        
        // Set name
        Name name = new Name();
        name.setGivenName(firstName);
        name.setFamilyName(lastName);
        person.setNames(Collections.singletonList(name));
        
        // Set email
        if (email != null && !email.isEmpty()) {
            EmailAddress emailAddress = new EmailAddress();
            emailAddress.setValue(email);
            emailAddress.setType("home");
            person.setEmailAddresses(Collections.singletonList(emailAddress));
        }
        
        // Set phone number
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            PhoneNumber number = new PhoneNumber();
            number.setValue(phoneNumber);
            number.setType("home");
            person.setPhoneNumbers(Collections.singletonList(number));
        }
        
        return person;
    }
}
