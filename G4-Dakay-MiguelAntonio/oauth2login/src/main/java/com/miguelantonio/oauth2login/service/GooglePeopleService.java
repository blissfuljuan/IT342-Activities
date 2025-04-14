package com.miguelantonio.oauth2login.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GooglePeopleService {

    // Method to get contacts
    public List<Person> getContacts(OAuth2AccessToken accessToken) throws IOException, GeneralSecurityException {
        Credential credential = new GoogleCredential().setAccessToken(accessToken.getTokenValue());

        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Google People API").build();

        ListConnectionsResponse response = peopleService.people().connections()
                .list("people/me")
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        return response.getConnections();
    }

    public void updateContact(String resourceName, String givenName, String email, String mobile, OAuth2AccessToken accessToken) 
        throws IOException, GeneralSecurityException {

        System.out.println("Inside Service: updateContact");
        System.out.println("resourceName: " + resourceName);

        // Authenticate using OAuth2 access token
        Credential credential = new GoogleCredential().setAccessToken(accessToken.getTokenValue());

        // Initialize People API service
        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Google People API").build();

        // 🔹 Step 1: Fetch Existing Contact to Get `etag`
        Person existingContact = peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers,metadata") // Include metadata for `etag`
                .execute();

        if (existingContact == null || existingContact.getEtag() == null) {
            throw new IllegalStateException("Unable to fetch existing contact or etag is missing.");
        }

        String etag = existingContact.getEtag(); // Extract etag
        System.out.println("Fetched etag: " + etag);

        // 🔹 Step 2: Create Updated Contact with `etag`
        Person updatedPerson = new Person()
                .setEtag(etag) // Set etag to prevent overwrite conflicts
                .setNames(Collections.singletonList(new Name().setGivenName(givenName)))
                .setEmailAddresses(Collections.singletonList(new EmailAddress().setValue(email)))
                .setPhoneNumbers(Collections.singletonList(new PhoneNumber().setValue(mobile)));

        System.out.println("Updated Person Object: " + updatedPerson);

        // 🔹 Step 3: Update Contact
        peopleService.people().updateContact(resourceName, updatedPerson)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        System.out.println("Contact updated successfully.");
    }

    // Delete a contact
    public void deleteContact(OAuth2AccessToken accessToken, String resourceName) throws IOException, GeneralSecurityException {
        Credential credential = new GoogleCredential().setAccessToken(accessToken.getTokenValue());

        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Google People API").build();

        peopleService.people().deleteContact(resourceName).execute();
    }

    public void createContact(String firstName, String lastName, String email, String phone, OAuth2AccessToken accessToken) 
        throws IOException, GeneralSecurityException {
    
        // Authenticate using OAuth2 access token
        Credential credential = new GoogleCredential().setAccessToken(accessToken.getTokenValue());

        // Initialize People API service
        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Google People API").build();

        // Create a new contact
        Person newContact = new Person()
                .setNames(Collections.singletonList(new Name()
                        .setGivenName(firstName)
                        .setFamilyName(lastName)))
                .setEmailAddresses(Collections.singletonList(new EmailAddress()
                        .setValue(email)))
                .setPhoneNumbers(Collections.singletonList(new com.google.api.services.people.v1.model.PhoneNumber()
                        .setValue(phone)));

        // Call the People API to create the contact
        peopleService.people().createContact(newContact).execute();
    }
}
