package com.rivera.googlecontacts.service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class GoogleContactsService {
    private final OAuth2AuthorizedClientService authorizedClientService;

    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    private String getAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2AuthorizedClient client = this.authorizedClientService.loadAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());
            if (client != null) {
                String token = client.getAccessToken().getTokenValue();
                System.out.println("OAuth2 Access Token: " + token);
                return token;
            }
        }
        throw new RuntimeException("OAuth2 authentication failed!");
    }

    private PeopleService createPeopleService() {
        return (new PeopleService.Builder(new NetHttpTransport(), new GsonFactory(), (request) -> {
            request.getHeaders().setAuthorization("Bearer " + this.getAccessToken());
        })).setApplicationName("Google Contacts App").build();
    }

    public List<Person> getContacts() throws IOException {
        try {
            PeopleService peopleService = this.createPeopleService();
            ListConnectionsResponse response = peopleService.people().connections()
                    .list("people/me")
                    .setPersonFields("names,emailAddresses,phoneNumbers")
                    .execute();
            List<Person> contacts = response.getConnections() != null ? response.getConnections() : new ArrayList<>();
            System.out.println("Fetched Contacts Count: " + contacts.size());
            return contacts;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error fetching contacts: " + e.getMessage());
            throw new IOException("Failed to retrieve contacts from Google People API", e);
        }
    }

    public Person createContact(String givenName, String familyName, String[] emails, String[] phoneNumbers) throws IOException {
        try {
            PeopleService peopleService = this.createPeopleService();
            Person newPerson = new Person();


            Name name = new Name();
            name.setGivenName(givenName);
            name.setFamilyName(familyName);
            newPerson.setNames(List.of(name));

            if (emails != null && emails.length > 0) {
                List<EmailAddress> emailAddresses = new ArrayList<>();
                for (String email : emails) {
                    if (email != null && !email.trim().isEmpty()) {
                        emailAddresses.add(new EmailAddress().setValue(email));
                    }
                }
                if (!emailAddresses.isEmpty()) {
                    newPerson.setEmailAddresses(emailAddresses);
                }
            }

            if (phoneNumbers != null && phoneNumbers.length > 0) {
                List<PhoneNumber> phoneNumberList = new ArrayList<>();
                for (String phoneNumber : phoneNumbers) {
                    if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                        phoneNumberList.add(new PhoneNumber().setValue(phoneNumber));
                    }
                }
                if (!phoneNumberList.isEmpty()) {
                    newPerson.setPhoneNumbers(phoneNumberList);
                }
            }

            Person createdPerson = peopleService.people().createContact(newPerson).execute();
            System.out.println("Created Contact ID: " + createdPerson.getResourceName());
            return createdPerson;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error creating contact: " + e.getMessage());
            throw new IOException("Failed to create contact using Google People API", e);
        }
    }

    public void updateContact(String resourceName, String givenName, String familyName, String[] emails, String[] phoneNumbers) throws IOException {
        try {
            PeopleService peopleService = this.createPeopleService();
            Person existingContact = peopleService.people().get(resourceName)
                    .setPersonFields("names,emailAddresses,phoneNumbers")
                    .execute();
            String etag = existingContact.getEtag();

            Person updatedContact = new Person();
            updatedContact.setEtag(etag);

            List<Name> names = new ArrayList<>();
            names.add(new Name().setGivenName(givenName).setFamilyName(familyName));
            updatedContact.setNames(names);

            List<EmailAddress> emailAddresses = new ArrayList<>();
            if (emails != null && emails.length > 0) {
                for (String email : emails) {
                    if (email != null && !email.trim().isEmpty()) {
                        emailAddresses.add(new EmailAddress().setValue(email));
                    }
                }
            }
            updatedContact.setEmailAddresses(emailAddresses);

            List<PhoneNumber> phoneNumberList = new ArrayList<>();
            if (phoneNumbers != null && phoneNumbers.length > 0) {
                for (String phoneNumber : phoneNumbers) {
                    if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                        phoneNumberList.add(new PhoneNumber().setValue(phoneNumber));
                    }
                }
            }
            updatedContact.setPhoneNumbers(phoneNumberList);

            peopleService.people().updateContact(resourceName, updatedContact)
                    .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                    .execute();
            System.out.println("Contact updated successfully: " + resourceName);
        } catch (IOException e) {
            System.err.println("Error updating contact: " + e.getMessage());
            throw new IOException("Failed to update contact in Google People API", e);
        }
    }

    public void deleteContact(String resourceName) throws IOException {
        try {
            PeopleService peopleService = this.createPeopleService();
            peopleService.people().deleteContact(resourceName).execute();
            System.out.println("Contact deleted successfully: " + resourceName);
        } catch (IOException e) {
            System.err.println("Error deleting contact: " + e.getMessage());
            throw new IOException("Failed to delete contact in Google People API", e);
        }
    }
}
