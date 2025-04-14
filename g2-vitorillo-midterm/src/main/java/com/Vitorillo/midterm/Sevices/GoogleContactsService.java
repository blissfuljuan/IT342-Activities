package com.Vitorillo.midterm.Sevices;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.google.api.services.people.v1.model.EmailAddress;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class GoogleContactsService {
    private final OAuth2AuthorizedClientService authorizedClientService;

    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    private PeopleService getPeopleService(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
            authentication.getAuthorizedClientRegistrationId(), 
            authentication.getName()
        );

        GoogleCredential credential = new GoogleCredential().setAccessToken(
            client.getAccessToken().getTokenValue()
        );

        return new PeopleService.Builder(
            new NetHttpTransport(), 
            new GsonFactory(), 
            credential
        ).setApplicationName("Google Contacts Manager").build();
    }

    public List<Person> listContacts(OAuth2AuthenticationToken authentication) throws IOException {
        PeopleService peopleService = getPeopleService(authentication);

        return peopleService.people().connections()
                .list("people/me")
                .setPersonFields("names,emailAddresses,phoneNumbers,photos") // Add photos here
                .execute()
                .getConnections();
    }

    public Person createContact(
        OAuth2AuthenticationToken authentication, 
        String firstName, 
        String lastName, 
        String email, 
        String phoneNumber
    ) throws IOException {
        PeopleService peopleService = getPeopleService(authentication);
        
        Person person = new Person();
        
        // Add Names
        person.setNames(List.of(new Name()
            .setGivenName(firstName)
            .setFamilyName(lastName)
        ));
        
        // Add Email
        if (email != null) {
            person.setEmailAddresses(List.of(new EmailAddress()
                .setValue(email)
            ));
        }
        
        // Add Phone Number
        if (phoneNumber != null) {
            person.setPhoneNumbers(List.of(new PhoneNumber()
                .setValue(phoneNumber)
            ));
        }
        
        return peopleService.people().createContact(person).execute();
    }

    public Person updateContact(
        OAuth2AuthenticationToken authentication, 
        String resourceName,
        String firstName, 
        String lastName, 
        String email, 
        String phoneNumber
    ) throws IOException {
        try {
            System.out.println("Attempting to update contact with resource name: " + resourceName);
            PeopleService peopleService = getPeopleService(authentication);
            
            // First, get the existing contact to retrieve the etag
            Person existingContact = peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();
                
            String etag = existingContact.getEtag();
            System.out.println("Retrieved etag: " + etag);
            
            // Create the updated person with the same etag
            Person person = new Person();
            person.setEtag(etag);
            
            // Add Names
            person.setNames(List.of(new Name()
                .setGivenName(firstName)
                .setFamilyName(lastName)
            ));
            
            // Add Email
            if (email != null) {
                person.setEmailAddresses(List.of(new EmailAddress()
                    .setValue(email)
                ));
            }
            
            // Add Phone Number
            if (phoneNumber != null) {
                person.setPhoneNumbers(List.of(new PhoneNumber()
                    .setValue(phoneNumber)
                ));
            }
            
            // Update the contact with the etag included
            Person updatedPerson = peopleService.people().updateContact(resourceName, person)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();
                
            System.out.println("Successfully updated contact: " + resourceName);
            return updatedPerson;
        } catch (Exception e) {
            System.err.println("Error updating contact: " + e.getMessage());
            e.printStackTrace();
            throw e; // Rethrow to allow controller to handle it
        }
    }

    public Person getContact(
        OAuth2AuthenticationToken authentication, 
        String resourceName
    ) throws IOException {
        try {
            System.out.println("Attempting to get contact with resource name: " + resourceName);
            PeopleService peopleService = getPeopleService(authentication);
            
            Person person = peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();
                
            System.out.println("Successfully retrieved contact: " + resourceName);
            return person;
        } catch (Exception e) {
            System.err.println("Error getting contact: " + e.getMessage());
            e.printStackTrace();
            throw e; // Rethrow to allow controller to handle it
        }
    }

    public void deleteContact(
        OAuth2AuthenticationToken authentication, 
        String resourceName
    ) throws IOException {
        try {
            System.out.println("Attempting to delete contact with resource name: " + resourceName);
            PeopleService peopleService = getPeopleService(authentication);
            peopleService.people().deleteContact(resourceName).execute();
            System.out.println("Successfully deleted contact: " + resourceName);
        } catch (Exception e) {
            System.err.println("Error deleting contact: " + e.getMessage());
            e.printStackTrace();
            throw e; // Rethrow to allow controller to handle it
        }
    }
}
