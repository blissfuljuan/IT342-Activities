package com.derecho.googlecontacts.service;

import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoogleContactsService {

    @Autowired
    private PeopleService peopleService;

    private String syncToken; // Stores the last sync token for incremental updates

    /**
     * Fetch all contacts (full sync).
     */
    public List<Person> getAllContacts() throws IOException {
        List<Person> allContacts = new ArrayList<>();

        if (peopleService == null) {
            throw new IllegalStateException("PeopleService is not initialized");
        }

        ListConnectionsResponse response = peopleService.people().connections()
                .list("people/me")
                .setPersonFields("metadata,names,emailAddresses")
                .setRequestSyncToken(true) // Request a sync token
                .execute();

        if (response.getConnections() != null) {
            allContacts.addAll(response.getConnections());
        }

        // Handle pagination
        while (response.getNextPageToken() != null) {
            response = peopleService.people().connections().list("people/me")
                    .setPersonFields("metadata,names,emailAddresses")
                    .setRequestSyncToken(true)
                    .setPageToken(response.getNextPageToken())
                    .execute();

            if (response.getConnections() != null) {
                allContacts.addAll(response.getConnections());
            }
        }

        // Store sync token for incremental updates
        syncToken = response.getNextSyncToken();
        return allContacts;
    }

    /**
     * Fetch incremental changes using the stored sync token.
     */
    public List<Person> getIncrementalChanges() throws IOException {
        if (syncToken == null) {
            return getAllContacts(); // Perform full sync if no sync token is stored
        }

        List<Person> updatedContacts = new ArrayList<>();

        try {
            ListConnectionsResponse response = peopleService.people().connections()
                    .list("people/me")
                    .setPersonFields("metadata,names,emailAddresses")
                    .setSyncToken(syncToken) // Use stored sync token
                    .execute();

            if (response.getConnections() != null) {
                for (Person person : response.getConnections()) {
                    handlePerson(person);
                    updatedContacts.add(person);
                }
            }

            // Handle pagination
            while (response.getNextPageToken() != null) {
                response = peopleService.people().connections().list("people/me")
                        .setPersonFields("metadata,names,emailAddresses")
                        .setSyncToken(syncToken)
                        .setPageToken(response.getNextPageToken()) // Fetch next page
                        .execute();

                if (response.getConnections() != null) {
                    for (Person person : response.getConnections()) {
                        handlePerson(person);
                        updatedContacts.add(person);
                    }
                }
            }

            // Update sync token for future syncs
            syncToken = response.getNextSyncToken();
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 410) { // Sync token expired
                return getAllContacts(); // Perform full sync
            } else {
                throw e; // Rethrow other errors
            }
        }

        return updatedContacts;
    }

    /**
     * Searches for contacts with caching.
     */
    public List<Person> searchContacts(String query) throws IOException, InterruptedException {
        if (peopleService == null) {
            throw new IllegalStateException("PeopleService is not initialized");
        }

        // Warmup cache
        peopleService.people().searchContacts()
                .setQuery("")
                .setReadMask("names,emailAddresses")
                .execute();

        // Wait a few seconds for caching
        Thread.sleep(5000);

        // Perform actual search query
        SearchResponse response = peopleService.people().searchContacts()
                .setQuery(query)
                .setReadMask("names,emailAddresses")
                .execute();

        return response.getResults() != null
                ? response.getResults().stream()
                .map(SearchResult::getPerson)  // Extract Person from SearchResult
                .collect(Collectors.toList())  // Collect as List<Person>
                : Collections.emptyList();



    }

    /**
     * Creates a new contact.
     */
    public Person createContact(String firstName, String lastName) throws IOException {
        if (peopleService == null) {
            throw new IllegalStateException("PeopleService is not initialized");
        }

        Person contact = new Person();
        List<Name> names = new ArrayList<>();
        names.add(new Name().setGivenName(firstName).setFamilyName(lastName));
        contact.setNames(names);

        return peopleService.people().createContact(contact).execute();
    }

    /**
     * Updates an existing contact.
     */
    public Person updateContact(String resourceName, String email) throws IOException {
        if (peopleService == null) {
            throw new IllegalStateException("PeopleService is not initialized");
        }

        Person contactToUpdate = peopleService.people().get(resourceName).execute();

        List<EmailAddress> emailAddresses = new ArrayList<>();
        emailAddresses.add(new EmailAddress().setValue(email));
        contactToUpdate.setEmailAddresses(emailAddresses);

        return peopleService.people()
                .updateContact(contactToUpdate.getResourceName(), contactToUpdate)
                .setUpdatePersonFields("emailAddresses")
                .execute();
    }

    /**
     * Deletes a contact.
     */
    public void deleteContact(String resourceName) throws IOException {
        if (peopleService == null) {
            throw new IllegalStateException("PeopleService is not initialized");
        }

        peopleService.people().deleteContact(resourceName).execute();
    }

    /**
     * Handles a person record (checks for deletions or updates).
     */
    private void handlePerson(Person person) {
        if (person.getMetadata() != null && Boolean.TRUE.equals(person.getMetadata().getDeleted())) {
            System.out.println("Deleted contact: " + person);
        } else {
            System.out.println("Updated contact: " + person);
        }
    }
}
