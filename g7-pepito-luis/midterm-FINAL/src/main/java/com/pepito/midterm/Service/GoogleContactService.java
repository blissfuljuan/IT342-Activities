package com.pepito.midterm.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import com.pepito.midterm.Config.GoogleCredentialConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoogleContactService {

    private static final String APPLICATION_NAME = "Google People API Example";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final GoogleCredentialConfig credentialProvider;

    @Autowired
    public GoogleContactService(GoogleCredentialConfig credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    private PeopleService getPeopleService(OAuth2User oAuth2User) throws GeneralSecurityException, IOException {
        Credential credential = credentialProvider.getCredential(oAuth2User);
        return new PeopleService.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<Map<String, Object>> listContacts(OAuth2User oAuth2User) throws IOException, GeneralSecurityException {
        PeopleService peopleService = getPeopleService(oAuth2User);
        ListConnectionsResponse response = peopleService.people().connections().list("people/me")
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        List<Person> connections = response.getConnections();
        if (connections == null || connections.isEmpty()) {
            return new ArrayList<>();
        }

        return connections.stream().map(contact -> {
            Map<String, Object> contactMap = new HashMap<>();
            contactMap.put("resourceName", Optional.ofNullable(contact.getResourceName()).orElse("N/A"));
            contactMap.put("name", contact.getNames() != null && !contact.getNames().isEmpty() ? contact.getNames().get(0).getDisplayName() : "N/A");
            contactMap.put("email", contact.getEmailAddresses() != null ? contact.getEmailAddresses().stream().map(EmailAddress::getValue).collect(Collectors.joining(", ")) : "N/A");
            contactMap.put("phone", contact.getPhoneNumbers() != null ? contact.getPhoneNumbers().stream().map(PhoneNumber::getValue).collect(Collectors.joining(", ")) : "N/A");
            return contactMap;
        }).collect(Collectors.toList());
    }

    public Person addContact(OAuth2User oAuth2User, String givenName, String familyName, List<String> emails, List<String> phones) throws IOException, GeneralSecurityException {
        PeopleService peopleService = getPeopleService(oAuth2User);
        Person contactToCreate = new Person();
        contactToCreate.setNames(List.of(new Name().setGivenName(givenName).setFamilyName(familyName)));

        contactToCreate.setEmailAddresses(emails.stream()
                .map(email -> new EmailAddress().setValue(email))
                .collect(Collectors.toList()));

        contactToCreate.setPhoneNumbers(phones.stream()
                .map(phone -> new PhoneNumber().setValue(phone))
                .collect(Collectors.toList()));

        return peopleService.people().createContact(contactToCreate).execute();
    }

    public Person updateContact(OAuth2User oAuth2User, String resourceName, String givenName, String familyName, List<String> emails, List<String> phones) throws IOException, GeneralSecurityException {
        PeopleService peopleService = getPeopleService(oAuth2User);
    
        // Fetch the existing contact with personFields
        Person contactToUpdate = peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers,metadata")
                .execute();
    
        // Update names
        contactToUpdate.setNames(List.of(new Name().setGivenName(givenName).setFamilyName(familyName)));
    
        // Update emails
        contactToUpdate.setEmailAddresses(emails.stream()
                .map(email -> new EmailAddress().setValue(email))
                .toList());
    
        // Update phones
        contactToUpdate.setPhoneNumbers(phones.stream()
                .map(phone -> new PhoneNumber().setValue(phone))
                .toList());
    
        // Ensure etag is included to prevent concurrency issues
        return peopleService.people().updateContact(resourceName, contactToUpdate)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();
    }
    

    public void deleteContact(OAuth2User oAuth2User, String resourceName) throws IOException, GeneralSecurityException {
        PeopleService peopleService = getPeopleService(oAuth2User);
        peopleService.people().deleteContact(resourceName).execute();
    }
}
