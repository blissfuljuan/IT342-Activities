package com.lacanglacang.google.oauth2googlecontacts.service;

import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GoogleContactsService {
    private final OAuth2AuthorizedClientService authorizedClientService;

    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    // Retrieve access token from Spring Security context
    private String getAccessToken() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName());
            if (client != null && client.getAccessToken() != null) {
                return client.getAccessToken().getTokenValue();
            }
        }
        throw new RuntimeException("OAuth2 authentication failed!");
    }

    // Create PeopleService instance
    private PeopleService createPeopleService() {
        return new PeopleService.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.gson.GsonFactory(),
                request -> request.getHeaders().setAuthorization("Bearer " + getAccessToken()))
                .setApplicationName("Google Contacts App").build();
    }

    // Fetch contacts
    public List<Person> getContacts() throws IOException {
        PeopleService peopleService = createPeopleService();
        ListConnectionsResponse response = peopleService.people().connections()
                .list("people/me")
                .setPersonFields("names,emailAddresses,phoneNumbers,photos")
                .execute();

        return response.getConnections() != null ? response.getConnections() : new ArrayList<>();
    }

    // Create a new contact
    public Person createContact(String givenName, String familyName, List<String> emails, List<String> phoneNumbers)
            throws IOException {
        PeopleService peopleService = createPeopleService();
        Person newPerson = new Person();

        newPerson.setNames(List.of(new Name().setGivenName(givenName).setFamilyName(familyName)));

        // Handle multiple emails
        if (emails != null && !emails.isEmpty()) {
            List<EmailAddress> emailList = new ArrayList<>();
            for (String email : emails) {
                if (email != null && !email.isEmpty()) {
                    emailList.add(new EmailAddress().setValue(email));
                }
            }
            newPerson.setEmailAddresses(emailList);
        }

        // Handle multiple phone numbers
        if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            List<PhoneNumber> phoneNumberList = new ArrayList<>();
            for (String phone : phoneNumbers) {
                if (phone != null && !phone.isEmpty()) {
                    phoneNumberList.add(new PhoneNumber().setValue(phone));
                }
            }
            newPerson.setPhoneNumbers(phoneNumberList);
        }

        return peopleService.people().createContact(newPerson).execute();
    }

    public void updateContact(String resourceName, String givenName, String familyName, List<String> emails,
            List<String> phoneNumbers) throws IOException {
        PeopleService peopleService = createPeopleService();

        // Fetch existing contact
        Person existingContact = peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        Person updatedContact = new Person().setEtag(existingContact.getEtag());

        // Update names
        updatedContact.setNames(List.of(new Name().setGivenName(givenName).setFamilyName(familyName)));

        // Prevent duplicate emails
        Set<String> emailSet = new HashSet<>();
        List<EmailAddress> updatedEmails = new ArrayList<>();

        if (existingContact.getEmailAddresses() != null) {
            for (EmailAddress existingEmail : existingContact.getEmailAddresses()) {
                if (existingEmail.getValue() != null) {
                    emailSet.add(existingEmail.getValue()); // Add existing emails
                }
            }
        }

        if (emails != null) {
            for (String email : emails) {
                if (email != null && !email.isEmpty()) {
                    emailSet.add(email); // Add new emails
                }
            }
        }

        // Convert unique emails back to a list
        for (String email : emailSet) {
            updatedEmails.add(new EmailAddress().setValue(email));
        }

        updatedContact.setEmailAddresses(updatedEmails);

        // Prevent duplicate phone numbers
        Set<String> phoneSet = new HashSet<>();
        List<PhoneNumber> updatedPhoneNumbers = new ArrayList<>();

        if (existingContact.getPhoneNumbers() != null) {
            for (PhoneNumber existingPhone : existingContact.getPhoneNumbers()) {
                if (existingPhone.getValue() != null) {
                    phoneSet.add(existingPhone.getValue()); // Add existing numbers
                }
            }
        }

        if (phoneNumbers != null) {
            for (String phone : phoneNumbers) {
                if (phone != null && !phone.isEmpty()) {
                    phoneSet.add(phone); // Add new numbers
                }
            }
        }

        // Convert unique phone numbers back to a list
        for (String phone : phoneSet) {
            updatedPhoneNumbers.add(new PhoneNumber().setValue(phone));
        }

        updatedContact.setPhoneNumbers(updatedPhoneNumbers);

        // Perform the update
        peopleService.people().updateContact(resourceName, updatedContact)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();
    }

    // Delete a contact
    public void deleteContact(String resourceName) throws IOException {
        PeopleService peopleService = createPeopleService();
        peopleService.people().deleteContact(resourceName).execute();
    }
}
