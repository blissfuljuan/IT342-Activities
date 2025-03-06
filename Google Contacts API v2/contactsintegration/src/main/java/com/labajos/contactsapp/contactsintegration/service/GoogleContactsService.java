package com.labajos.contactsapp.contactsintegration.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.PeopleService.Builder;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.labajos.contactsapp.contactsintegration.model.Contact;

@Service
public class GoogleContactsService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(GoogleContactsService.class);

    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService, RestTemplate restTemplate) {
        this.authorizedClientService = authorizedClientService;
        this.restTemplate = restTemplate;
    }

    private String getAccessToken(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        if (client == null || client.getAccessToken() == null) {
            throw new IllegalStateException("OAuth2AuthorizedClient or AccessToken is null");
        }

        return client.getAccessToken().getTokenValue();
    }

    public String getContacts(OAuth2AuthenticationToken authentication) {
        try {
            String accessToken = getAccessToken(authentication);
            String url = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses,phoneNumbers";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                logger.error("Failed to fetch contacts: {}", response.getStatusCode());
                return "Error fetching contacts. Status: " + response.getStatusCode();
            }
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching contacts", e);
            return "Error fetching contacts: " + e.getMessage();
        }
    }

    public String createContact(OAuth2AuthenticationToken authentication, String fullName, String email, String phoneNumber) {
        try {
            String accessToken = getAccessToken(authentication);
            String url = "https://people.googleapis.com/v1/people:createContact";

            String jsonBody = "{"
                    + "\"names\": [{\"givenName\": \"" + fullName + "\"}],"
                    + "\"emailAddresses\": [{\"value\": \"" + email + "\"}],"
                    + "\"phoneNumbers\": [{\"value\": \"" + phoneNumber + "\"}]"
                    + "}";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                logger.error("Failed to create contact: {}", response.getStatusCode());
                return "Error creating contact. Status: " + response.getStatusCode();
            }

            return response.getBody();
        } catch (Exception e) {
            logger.error("Error creating contact", e);
            return "Error creating contact: " + e.getMessage();
        }
    }

    public Contact updateContact(OAuth2AuthorizedClient client, String resourceName, Contact contact) 
    throws IOException, GeneralSecurityException {
    PeopleService service = createPeopleService(client);

    try {
        // Delete the old contact
        try {
            service.people().deleteContact(resourceName).execute();
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                // Try with and without "people/" prefix
                String altResourceName = resourceName.startsWith("people/") ? 
                    resourceName.substring(7) : "people/" + resourceName;
                service.people().deleteContact(altResourceName).execute();
            } else {
                throw e;
            }
        }
        
        // Split full name into first and last name
        String[] nameParts = contact.getName().split(" ", 2);
        String givenName = nameParts[0]; // First word as given name
        String familyName = nameParts.length > 1 ? nameParts[1] : ""; // Rest as last name
        
        // Create a new contact with the updated information
        Person person = new Person()
                .setNames(Collections.singletonList(new Name()
                        .setGivenName(givenName)
                        .setFamilyName(familyName)))
                .setEmailAddresses(contact.getEmail().stream()
                        .map(email -> new EmailAddress().setValue(email))
                        .collect(Collectors.toList()))
                .setPhoneNumbers(contact.getPhone().stream()
                        .map(phone -> new PhoneNumber().setValue(phone))
                        .collect(Collectors.toList()));

        // Create new contact
        Person created = Optional.ofNullable(service.people().createContact(person).execute())
                .orElseThrow(() -> new IOException("Failed to create contact"));
        
        // Fetch the contact again to ensure names field is included
        Person retrievedContact = service.people().get(created.getResourceName())
                .setPersonFields("names,emailAddresses,phoneNumbers") // Explicitly request fields
                .execute();

        return new Contact(
                (retrievedContact.getNames() != null && !retrievedContact.getNames().isEmpty()) ?
                        retrievedContact.getNames().get(0).getGivenName() + 
                        (retrievedContact.getNames().get(0).getFamilyName() != null ? 
                        " " + retrievedContact.getNames().get(0).getFamilyName() : "")
                        : contact.getName(), // Fallback to input name
                retrievedContact.getEmailAddresses() != null ?
                        retrievedContact.getEmailAddresses().stream()
                                .map(EmailAddress::getValue)
                                .collect(Collectors.toList()) :
                        new ArrayList<>(),
                retrievedContact.getPhoneNumbers() != null ?
                        retrievedContact.getPhoneNumbers().stream()
                                .map(PhoneNumber::getValue)
                                .collect(Collectors.toList()) :
                        new ArrayList<>(),
                retrievedContact.getResourceName()
        );
    } catch (Exception e) {
        System.err.println("Error updating contact: " + e.getMessage());
        throw e;
    }
}

    public void deleteContact(OAuth2AuthorizedClient client, String resourceName) throws IOException, GeneralSecurityException {
        PeopleService service = createPeopleService(client);
        try {
            service.people().deleteContact(resourceName).execute();
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                // Try with and without "people/" prefix
                String altResourceName = resourceName.startsWith("people/") ? 
                    resourceName.substring(7) : "people/" + resourceName;
                service.people().deleteContact(altResourceName).execute();
            } else {
                throw e;
            }
        }
    }

    private PeopleService createPeopleService(OAuth2AuthorizedClient client) throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        HttpRequestInitializer requestInitializer = request -> {
            com.google.api.client.auth.oauth2.Credential credential = new com.google.api.client.auth.oauth2.Credential(com.google.api.client.auth.oauth2.BearerToken.authorizationHeaderAccessMethod());
            credential.setAccessToken(client.getAccessToken().getTokenValue());
            credential.initialize(request);
        };

        return new Builder(httpTransport, jsonFactory, requestInitializer)
                .setApplicationName("midterm")
                .build();
    }
}