package com.miparanummidterm.Midterm.Miparanum.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import com.google.api.services.people.v1.model.ListConnectionsResponse;

import com.google.api.services.people.v1.model.Name;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Service
public class ContactsService {
    private final OAuth2AuthorizedClientService authorizedClientService;

    public ContactsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    public List<Person> getGoogleContacts(Authentication authentication) throws IOException, GeneralSecurityException {
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2AuthorizedClient client = this.authorizedClientService.loadAuthorizedClient("google", authentication.getName());
            if (client == null) {
                throw new IllegalStateException("OAuth2AuthorizedClient is null. User might not be authenticated properly.");
            } else {
                String accessToken = client.getAccessToken().getTokenValue();
                GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, (Date)null));
                HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
                PeopleService peopleService = (new PeopleService.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))).setApplicationName("Google Contacts App").build();
                ListConnectionsResponse response = (ListConnectionsResponse)peopleService.people().connections().list("people/me").setPageSize(10).setPersonFields("names,emailAddresses,phoneNumbers").execute();
                return response.getConnections();
            }
        } else {
            throw new IllegalStateException("User is not authenticated with OAuth2.");
        }
    }

    public Person addGoogleContact(Authentication authentication, Person newContact) throws IOException, GeneralSecurityException {
        OAuth2AuthorizedClient client = this.authorizedClientService.loadAuthorizedClient("google", authentication.getName());
        if (client == null) {
            throw new IllegalStateException("OAuth2AuthorizedClient is null. User might not be authenticated properly.");
        } else {
            String accessToken = client.getAccessToken().getTokenValue();
            GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, (Date)null));
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            PeopleService peopleService = (new PeopleService.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))).setApplicationName("Google Contacts App").build();
            String givenName = "";
            String email = "";
            String phoneNumber = "";
            Object phoneObj;
            if (newContact.getNames() != null && !newContact.getNames().isEmpty()) {
                phoneObj = newContact.getNames().get(0);
                if (phoneObj instanceof Name) {
                    givenName = ((Name)phoneObj).getGivenName();
                } else if (phoneObj instanceof Map) {
                    givenName = (String)((Map)phoneObj).get("givenName");
                }
            }

            if (newContact.getEmailAddresses() != null && !newContact.getEmailAddresses().isEmpty()) {
                phoneObj = newContact.getEmailAddresses().get(0);
                if (phoneObj instanceof EmailAddress) {
                    email = ((EmailAddress)phoneObj).getValue();
                } else if (phoneObj instanceof Map) {
                    email = (String)((Map)phoneObj).get("value");
                }
            }

            if (newContact.getPhoneNumbers() != null && !newContact.getPhoneNumbers().isEmpty()) {
                phoneObj = newContact.getPhoneNumbers().get(0);
                if (phoneObj instanceof PhoneNumber) {
                    phoneNumber = ((PhoneNumber)phoneObj).getValue();
                } else if (phoneObj instanceof Map) {
                    phoneNumber = (String)((Map)phoneObj).get("value");
                }
            }

            Person contactToCreate = (new Person()).setNames(Collections.singletonList((new Name()).setGivenName(givenName))).setEmailAddresses(Collections.singletonList((new EmailAddress()).setValue(email))).setPhoneNumbers(Collections.singletonList((new PhoneNumber()).setValue(phoneNumber)));
            return (Person)peopleService.people().createContact(contactToCreate).execute();
        }
    }

    public Person updateGoogleContact(Authentication authentication, String resourceName, Person updatedContact) throws IOException, GeneralSecurityException {
        if (updatedContact == null) {
            throw new IllegalArgumentException("Updated contact data cannot be null");
        } else {
            OAuth2AuthorizedClient client = this.authorizedClientService.loadAuthorizedClient("google", authentication.getName());
            if (client == null) {
                throw new IllegalStateException("OAuth2AuthorizedClient is null. User might not be authenticated properly.");
            } else {
                String accessToken = client.getAccessToken().getTokenValue();
                GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, (Date)null));
                PeopleService peopleService = (new PeopleService.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))).setApplicationName("Google Contacts App").build();
                Person existingContact = (Person)peopleService.people().get(resourceName).setPersonFields("names,emailAddresses,phoneNumbers,metadata").execute();
                String etag = existingContact.getEtag();
                updatedContact.setEtag(etag);
                return (Person)peopleService.people().updateContact(resourceName, updatedContact).setUpdatePersonFields("names,emailAddresses,phoneNumbers").execute();
            }
        }
    }

    public void deleteGoogleContact(Authentication authentication, String resourceName) throws IOException, GeneralSecurityException {
        OAuth2AuthorizedClient client = this.authorizedClientService.loadAuthorizedClient("google", authentication.getName());
        if (client == null) {
            throw new IllegalStateException("OAuth2AuthorizedClient is null. User might not be authenticated properly.");
        } else {
            String accessToken = client.getAccessToken().getTokenValue();
            GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, (Date)null));
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            PeopleService peopleService = (new PeopleService.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))).setApplicationName("Google Contacts App").build();
            System.out.println("Calling Google API to delete: " + resourceName);

            try {
                peopleService.people().deleteContact(resourceName).execute();
            } catch (Exception e) {
                System.err.println("Error from Google API: " + e.getMessage());
                e.printStackTrace(); // Log the full stack trace for debugging
                throw new RuntimeException("Failed to delete contact", e); // Wrap it in a RuntimeException
            }

        }
    }
}