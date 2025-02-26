package com.revilleza.oauth2login.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import com.revilleza.oauth2login.model.Contact;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GoogleContactsService {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final OAuth2AuthorizedClientService authorizedClientService;

    private static PeopleService peopleService;

    @Value("${google.contacts.api.url}")
    private String contactsApiUrl;

    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    public List getContacts(String principalName) {
        ListConnectionsResponse response = null;
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            OAuth2AuthorizedClient client = authorizedClientService
                    .loadAuthorizedClient("google", principalName);

            if (client == null) {
                throw new RuntimeException("OAuth2 client not found");
            }

            OAuth2AccessToken accessToken = client.getAccessToken();
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken.getTokenValue());


            peopleService = new PeopleService.Builder(HTTP_TRANSPORT,JSON_FACTORY,credential).build();
            response = peopleService.people().connections().list("people/me")
                    .setPersonFields("names,emailAddresses,phoneNumbers")
                    .execute();

            return parseContact(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Contact> parseContact(ListConnectionsResponse response){
        List<Contact> contactsList = new ArrayList<>();
        List<Person> persons = response.getConnections();

        if (response != null) {
            for (Map<String, Object> person : persons) {
                System.out.println("Person: " + person.toString());
                String name = extractName(person);
                List<String> emails = extractEmails(person);
                List<String> phones = extractPhones(person);

                Contact contact = new Contact(name);
                contact.setPhones(phones);
                contact.setEmails(emails);
                contactsList.add(contact);
            }
        }
        return contactsList;
    }

    private String extractName(Map<String, Object> person) {
        List<Map<String, Object>> names = (List<Map<String, Object>>) person.get("names");
        return (names != null && !names.isEmpty()) ? (String) names.get(0).get("displayName") : "No Name";
    }

    private List<String> extractEmails(Map<String, Object> person) {
        List<String> emailsList = new ArrayList<>();
        List<Map<String, Object>> emails = (List<Map<String, Object>>) person.get("emailAddresses");
        if (emails != null) {
            System.out.println("Emails: " + emails.size());
            for (Map<String, Object> email : emails) {
                System.out.println("Email: " + email);
                emailsList.add((String) email.get("value"));
            }
        }
        return emailsList;
    }

    private List<String> extractPhones(Map<String, Object> person) {
        List<String> emailsList = new ArrayList<>();
        List<Map<String, Object>> phones = (List<Map<String, Object>>) person.get("phoneNumbers");
        if (phones != null) {
            System.out.println("Phones: " + phones.size());
            for (Map<String, Object> phone : phones) {
                System.out.println("Phone: " + phone);
                emailsList.add((String) phone.get("value"));
            }
        }
        return emailsList;
    }
}
