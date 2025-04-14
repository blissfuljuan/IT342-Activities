package com.caaway.googlecontact.Service;

import com.caaway.googlecontact.model.Contacts;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.client.json.JsonFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GoogleContactsService {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "Google Contacts Integration";
    private static final List<String> PERSON_FIELDS = Arrays.asList("names", "emailAddresses", "phoneNumbers");

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    private PeopleService getPeopleService(OAuth2User principal) {
        // Get the client registration ID (should be "google" based on your configuration)
        String clientRegistrationId = "google";

        // Get the name/identifier from principal
        String name = principal.getName();

        // Load the authorized client
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                clientRegistrationId,
                name
        );

        if (client == null) {
            throw new RuntimeException("OAuth2 client is null. User may not be properly authenticated.");
        }

        String accessToken = client.getAccessToken().getTokenValue();

        if (accessToken == null) {
            throw new RuntimeException("Access token is null");
        }

        // For debugging
        System.out.println("Access Token: " + accessToken);

        // Build and return the PeopleService
        return new PeopleService.Builder(
                new NetHttpTransport(),
                JSON_FACTORY,
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<Contacts> getContacts(OAuth2User principal) {
        List<Contacts> contactsList = new ArrayList<>();

        if (principal == null) {
            throw new RuntimeException("User is not authenticated");
        }

        try {
            PeopleService peopleService = getPeopleService(principal);
            List<Person> connections = getConnections(peopleService);

            for (Person person : connections) {
                Contacts contact = new Contacts();
                contact.setResourceName(person.getResourceName());
                contact.setName(getPersonName(person));
                contact.setEmail(getPersonEmail(person));
                contact.setPhoneNumber(getPersonPhoneNumber(person));
                contactsList.add(contact);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error fetching contacts: " + e.getMessage(), e);
        }

        return contactsList;
    }

    public List<Person> getConnectionsAsPeople(OAuth2User principal) {
        if (principal == null) {
            throw new RuntimeException("User is not authenticated");
        }

        try {
            PeopleService peopleService = getPeopleService(principal);
            return getConnections(peopleService);
        } catch (IOException e) {
            throw new RuntimeException("Error fetching contacts: " + e.getMessage(), e);
        }
    }

    private List<Person> getConnections(PeopleService peopleService) throws IOException {
        ListConnectionsResponse response = peopleService.people().connections()
                .list("people/me")
                .setPersonFields(String.join(",", PERSON_FIELDS))
                .execute();

        return response.getConnections() != null ? response.getConnections() : new ArrayList<>();
    }

    public void addContact(OAuth2User principal, String name, String email, String phoneNumber) {
        try {
            PeopleService peopleService = getPeopleService(principal);

            // Create a new person
            Person newPerson = new Person();

            // Add name
            Name personName = new Name();
            personName.setDisplayName(name);
            personName.setGivenName(name);
            newPerson.setNames(Arrays.asList(personName));

            // Add email if provided
            if (email != null && !email.isEmpty()) {
                EmailAddress emailAddress = new EmailAddress();
                emailAddress.setValue(email);
                emailAddress.setType("home");
                newPerson.setEmailAddresses(Arrays.asList(emailAddress));
            }

            // Add phone if provided
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                PhoneNumber personPhone = new PhoneNumber();
                personPhone.setValue(phoneNumber);
                personPhone.setType("mobile");
                newPerson.setPhoneNumbers(Arrays.asList(personPhone));
            }

            // Create the contact
            peopleService.people().createContact(newPerson).execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed to add contact: " + e.getMessage(), e);
        }
    }

    public void createContact(OAuth2User principal, Person newPerson) {
        try {
            PeopleService peopleService = getPeopleService(principal);
            peopleService.people().createContact(newPerson).execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create contact: " + e.getMessage(), e);
        }
    }

    public Person updateContact(OAuth2User principal, String resourceName, Person updatePerson) {
        try {
            PeopleService peopleService = getPeopleService(principal);

            // Build update fields based on what's set in updatePerson
            List<String> updatePersonFields = new ArrayList<>();
            if (updatePerson.getNames() != null) updatePersonFields.add("names");
            if (updatePerson.getEmailAddresses() != null) updatePersonFields.add("emailAddresses");
            if (updatePerson.getPhoneNumbers() != null) updatePersonFields.add("phoneNumbers");

            if (updatePersonFields.isEmpty()) {
                throw new RuntimeException("No fields provided for update");
            }

            // Ensure resourceName has the "people/" prefix
            String fullResourceName = resourceName.startsWith("people/") ? resourceName : "people/" + resourceName;

            // Perform the update
            return peopleService.people().updateContact(fullResourceName, updatePerson)
                    .setUpdatePersonFields(String.join(",", updatePersonFields))
                    .setPersonFields(String.join(",", PERSON_FIELDS))  // Add this to get back full contact details
                    .execute();

        } catch (IOException e) {
            throw new RuntimeException("Failed to update contact: " + e.getMessage(), e);
        }
    }

    public void deleteContact(OAuth2User principal, String resourceName) {
        try {
            PeopleService peopleService = getPeopleService(principal);
            peopleService.people().deleteContact(resourceName).execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete contact: " + e.getMessage(), e);
        }
    }

    public Person getPersonById(OAuth2User principal, String resourceName) {
        try {
            PeopleService peopleService = getPeopleService(principal);
            // Ensure resourceName has the "people/" prefix
            String fullResourceName = resourceName.startsWith("people/") ? resourceName : "people/" + resourceName;
            return peopleService.people().get(fullResourceName)
                    .setPersonFields(String.join(",", PERSON_FIELDS))
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch contact: " + e.getMessage(), e);
        }
    }

    public Contacts getContactById(OAuth2User principal, String resourceName) {
        try {
            Person person = getPersonById(principal, resourceName);

            Contacts contact = new Contacts();
            contact.setResourceName(resourceName);
            contact.setName(getPersonName(person));
            contact.setEmail(getPersonEmail(person));
            contact.setPhoneNumber(getPersonPhoneNumber(person));

            return contact;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch contact: " + e.getMessage(), e);
        }
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
}