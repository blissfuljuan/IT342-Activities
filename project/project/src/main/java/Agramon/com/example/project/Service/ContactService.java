package Agramon.com.example.project.Service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ContactService {

    @SuppressWarnings("unused")
    private final PeopleService peopleService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final JsonFactory jsonFactory;

    public ContactService(PeopleService peopleService, OAuth2AuthorizedClientService authorizedClientService, JsonFactory jsonFactory) {
        this.peopleService = peopleService;
        this.authorizedClientService = authorizedClientService;
        this.jsonFactory = jsonFactory;
    }

    public List<Person> getContacts(OAuth2User user) throws IOException {
        // Get the OAuth2 access token properly
        String clientRegistrationId = "google"; // This should match your application.properties
        OAuth2AuthorizedClient authorizedClient =
                authorizedClientService.loadAuthorizedClient(clientRegistrationId, user.getName());

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new IllegalStateException("No OAuth2 authorized client found for user.");
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        System.out.println("Access Token: " + accessToken.getTokenValue()); // ✅ Debugging

        PeopleService peopleService = new PeopleService.Builder(
                new NetHttpTransport(), jsonFactory, null)
                .setApplicationName("Google Contacts App")
                .setHttpRequestInitializer(request -> request.getHeaders().setAuthorization("Bearer " + accessToken.getTokenValue()))
                .build();

        ListConnectionsResponse response = peopleService.people().connections()
                .list("people/me")
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        if (response == null || response.getConnections() == null) {
            System.out.println("❌ No contacts retrieved from Google API.");
            return List.of(); // Return an empty list instead of null
        }

        System.out.println("✅ Contacts Retrieved: " + response.getConnections().size());
        return response.getConnections();
    }
}
