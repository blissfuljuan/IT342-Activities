package com.googlecontactsapi.googlecontactsapi.service;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import com.googlecontactsapi.googlecontactsapi.helper.TokenStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ContactService {

    private final TokenStore tokenStore;

    @Autowired
    public ContactService(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    private PeopleService getPeopleService(OAuth2AuthenticationToken authentication) throws IOException {
        String accessToken = tokenStore.getToken(authentication);

        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                .setAccessToken(accessToken);

        return new PeopleService.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).build();
    }

    public List<Person> getAllContacts(OAuth2AuthenticationToken authentication) throws IOException {
        PeopleService peopleService = getPeopleService(authentication);

        ListConnectionsResponse response = peopleService.people().connections()
                .list("people/me")
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        return response.getConnections();
    }

    public List<Person> addContact(OAuth2AuthenticationToken authentication, Person person) throws IOException {
        PeopleService peopleService = getPeopleService(authentication);

        peopleService.people().createContact(person).execute();
        return getAllContacts(authentication);
    }

    public List<Person> updateContact(OAuth2AuthenticationToken authentication, String resourceName, Person person) throws IOException {
        PeopleService peopleService = getPeopleService(authentication);

        peopleService.people().updateContact(resourceName, person)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();
        return getAllContacts(authentication);
    }

    public List<Person> deleteContact(OAuth2AuthenticationToken authentication, String resourceName) throws IOException {
        PeopleService peopleService = getPeopleService(authentication);

        peopleService.people().deleteContact(resourceName).execute();
        return getAllContacts(authentication);
    }
}