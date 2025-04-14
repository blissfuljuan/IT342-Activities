package com.vequiso.googlecontacts.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;

@Service
public class GooglePeopleService {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public GooglePeopleService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    private String getAccessToken() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );
            if (client != null) {
                String token = client.getAccessToken().getTokenValue();
                System.out.println("OAuth2 Access Token: " + token); // DEBUGGING TOKEN
                return token;
            }
        }
        throw new RuntimeException("OAuth2 authentication failed!");
    }

    private PeopleService createPeopleService() {
        return new PeopleService.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.gson.GsonFactory(),
                request -> request.getHeaders().setAuthorization("Bearer " + getAccessToken())
        ).setApplicationName("Google Contacts App").build();
    }

    public List<Person> getContacts() throws IOException {
        try {
            PeopleService peopleService = createPeopleService();
            ListConnectionsResponse response = peopleService.people().connections()
                    .list("people/me")
                    .setPersonFields("names,emailAddresses,phoneNumbers")
                    .execute();
            System.out.println("API Response: " + response);

            List<Person> contacts = response.getConnections() != null ? response.getConnections() : new ArrayList<>();
            System.out.println("Fetched Contacts Count: " + contacts.size()); // DEBUGGING CONTACT COUNT
            return contacts;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error fetching contacts: " + e.getMessage());
            throw new IOException("Failed to retrieve contacts from Google People API", e);
        }
    }

    public Person createContact(String givenName, String familyName, String email, String phoneNumber) throws IOException {
        try {
            PeopleService peopleService = createPeopleService();

            // Create a new Person object
            Person newPerson = new Person();

            // Set the name
            Name name = new Name();
            name.setGivenName(givenName);
            name.setFamilyName(familyName);
            newPerson.setNames(List.of(name));

            // Set the email
            if (email != null && !email.isEmpty()) {
                EmailAddress emailAddress = new EmailAddress();
                emailAddress.setValue(email);
                newPerson.setEmailAddresses(List.of(emailAddress));
            }

            // Set the phone number
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                PhoneNumber phone = new PhoneNumber();
                phone.setValue(phoneNumber);
                newPerson.setPhoneNumbers(List.of(phone));
            }

            // Create the contact
            Person createdPerson = peopleService.people().createContact(newPerson).execute();
            System.out.println("Created Contact ID: " + createdPerson.getResourceName()); // DEBUGGING CREATED CONTACT ID
            return createdPerson;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error creating contact: " + e.getMessage());
            throw new IOException("Failed to create contact using Google People API", e);
        }
    }

    public void updateContact(String resourceName, String givenName, String familyName, String email, String phoneNumber) throws IOException {
        try {
            PeopleService peopleService = createPeopleService();

            // Step 1: Fetch the existing contact to get the etag
            Person existingContact = peopleService.people().get(resourceName)
                    .setPersonFields("names,emailAddresses,phoneNumbers")
                    .execute();

            String etag = existingContact.getEtag(); // Extract etag

            // Step 2: Update the contact details
            List<Name> names = new ArrayList<>();
            names.add(new Name().setGivenName(givenName).setFamilyName(familyName));

            List<EmailAddress> emailAddresses = new ArrayList<>();
            if (email != null && !email.isEmpty()) {
                emailAddresses.add(new EmailAddress().setValue(email));
            }

            List<PhoneNumber> phoneNumbers = new ArrayList<>();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                phoneNumbers.add(new PhoneNumber().setValue(phoneNumber));
            }

            // Step 3: Create a new contact object with the etag
            Person updatedContact = new Person();
            updatedContact.setEtag(etag); // Add etag here
            updatedContact.setNames(names);
            updatedContact.setEmailAddresses(emailAddresses);
            updatedContact.setPhoneNumbers(phoneNumbers);

            // Step 4: Perform the update
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
            PeopleService peopleService = createPeopleService();

            // Call the Google People API to delete the contact
            peopleService.people().deleteContact(resourceName).execute();

            System.out.println("Contact deleted successfully: " + resourceName);
        } catch (IOException e) {
            System.err.println("Error deleting contact: " + e.getMessage());
            throw new IOException("Failed to delete contact in Google People API", e);
        }
    }


}