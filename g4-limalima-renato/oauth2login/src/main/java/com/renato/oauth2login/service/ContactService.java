    package com.renato.oauth2login.service;

    import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
    import com.google.api.client.http.javanet.NetHttpTransport;
    import com.google.api.client.json.JsonFactory;
    import com.google.api.client.json.gson.GsonFactory;
    import com.google.api.client.http.HttpRequest;
    import com.google.api.client.http.HttpRequestInitializer;
    import com.google.api.services.people.v1.PeopleService;
    import com.google.api.services.people.v1.model.*;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
    import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
    import org.springframework.security.oauth2.core.OAuth2AccessToken;
    import org.springframework.stereotype.Service;

    import java.io.IOException;
    import java.security.GeneralSecurityException;
    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.List;

    @Service
    public class ContactService {

        @Autowired
        private OAuth2AuthorizedClientService authorizedClientService;

        private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

        private PeopleService peopleService(OAuth2AccessToken accessToken) throws GeneralSecurityException, IOException {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            return new PeopleService.Builder(
                    httpTransport,
                    JSON_FACTORY,
                    httpRequest -> httpRequest.getHeaders().setAuthorization("Bearer " + accessToken.getTokenValue())
            ).setApplicationName("Google Contacts API").build();
        }

        private OAuth2AccessToken getAccessToken(String principalName) {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("google", principalName);
            if (client == null) {
                throw new RuntimeException("OAuth2 client not found for user: " + principalName);
            }
            return client.getAccessToken();
        }

        // Retrieve all contacts
        public List<Person> getContacts(String principalName) throws GeneralSecurityException, IOException {
            OAuth2AccessToken accessToken = getAccessToken(principalName);
            PeopleService peopleService = peopleService(accessToken);

            try {
                ListConnectionsResponse response = peopleService.people().connections()
                        .list("people/me")
                        .setPageSize(100)
                        .setPersonFields("names,emailAddresses,phoneNumbers")
                        .execute();

                return response.getConnections() != null ? response.getConnections() : new ArrayList<>();
            } catch (IOException e) {
                throw new IOException("Error retrieving contacts: " + e.getMessage(), e);
            }
        }

        // Add a new contact
        public void addContact(String principalName, String firstName, String lastName, String phoneNumber)
                throws GeneralSecurityException, IOException {
            OAuth2AccessToken accessToken = getAccessToken(principalName);
            PeopleService peopleService = peopleService(accessToken);

            Person person = new Person()
                    .setNames(Collections.singletonList(new Name()
                            .setGivenName(firstName)
                            .setFamilyName(lastName)));

            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                person.setPhoneNumbers(Collections.singletonList(new PhoneNumber().setValue(phoneNumber)));
            }

            peopleService.people().createContact(person).execute();
        }

        // Update an existing contact
        public void updateContact(String principalName, String resourceName, String firstName, String lastName, String phoneNumber)
            throws GeneralSecurityException, IOException {
        if (resourceName == null || resourceName.isEmpty()) {
            throw new IllegalArgumentException("Resource name cannot be null or empty.");
        }

        OAuth2AccessToken accessToken = getAccessToken(principalName);
        PeopleService peopleService = peopleService(accessToken);

        Person existingPerson = peopleService.people().get(resourceName)
                .setPersonFields("names,phoneNumbers,metadata")
                .execute();

        if (existingPerson.getMetadata() == null || existingPerson.getMetadata().getSources() == null) {
            throw new IOException("Failed to retrieve metadata for contact.");
        }

        String latestEtag = existingPerson.getMetadata().getSources().stream()
                .map(Source::getEtag)
                .filter(etag -> etag != null)
                .findFirst()
                .orElseThrow(() -> new IOException("Failed to retrieve latest etag. Contact might not be updatable."));

        Person updatedPerson = new Person()
                .setEtag(latestEtag)
                .setNames(Collections.singletonList(new Name()
                        .setGivenName(firstName)
                        .setFamilyName(lastName)));

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            updatedPerson.setPhoneNumbers(Collections.singletonList(new PhoneNumber().setValue(phoneNumber)));
        }

        updatedPerson.setMetadata(new PersonMetadata().setSources(existingPerson.getMetadata().getSources()));

        peopleService.people().updateContact(resourceName, updatedPerson)
                .setUpdatePersonFields("names,phoneNumbers")
                .execute();
    }




        // Delete a contact
        public void deleteContact(String principalName, String resourceName) throws GeneralSecurityException, IOException {
            OAuth2AccessToken accessToken = getAccessToken(principalName);
            PeopleService peopleService = peopleService(accessToken);

            peopleService.people().deleteContact(resourceName).execute();
        }
    }
