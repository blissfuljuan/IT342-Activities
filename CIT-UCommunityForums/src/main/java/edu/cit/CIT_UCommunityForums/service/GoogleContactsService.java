package edu.cit.CIT_UCommunityForums.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import edu.cit.CIT_UCommunityForums.model.Contact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleContactsService {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    public List<Contact> getContacts(OAuth2AuthenticationToken authentication) throws Exception {
        // Load the authorized client to get the access token.
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );
        String accessToken = client.getAccessToken().getTokenValue();

        // Build the PeopleService with the access token.
        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                new GoogleCredential().setAccessToken(accessToken))
                .setApplicationName("CIT-UCommunityForums")
                .build();

        // Retrieve the user's contacts.
        ListConnectionsResponse response = peopleService.people().connections()
                .list("people/me")
                .setPageSize(100) // adjust as needed
                .setPersonFields("names,emailAddresses")
                .execute();

        List<Person> people = response.getConnections();
        List<Contact> contacts = new ArrayList<>();

        if (people != null) {
            for (Person person : people) {
                String name = "Unknown";
                String email = "Not available";

                if (person.getNames() != null && !person.getNames().isEmpty()) {
                    name = person.getNames().get(0).getDisplayName();
                }
                if (person.getEmailAddresses() != null && !person.getEmailAddresses().isEmpty()) {
                    email = person.getEmailAddresses().get(0).getValue();
                }

                contacts.add(new Contact(name, email));
            }
        }
        return contacts;
    }
}
