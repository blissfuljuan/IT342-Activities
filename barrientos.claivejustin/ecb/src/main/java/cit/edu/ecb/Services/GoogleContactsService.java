package cit.edu.ecb.Services;


import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoogleContactsService {
    @SuppressWarnings("unused")
    private final OAuth2AuthorizedClientService authorizedClientService;

    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    /*public List<String> getContacts(OAuth2AccessToken accessToken) throws IOException {
        PeopleService peopleService = new PeopleService.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.gson.GsonFactory(),
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken.getTokenValue())
        ).setApplicationName("Google Contacts API Integration").build();

        ListConnectionsResponse response = peopleService.people().connections().list("people/me")
                .setPersonFields("names,emailAddresses")
                .execute();

        return response.getConnections().stream()
                .map(person -> person.getNames() != null && !person.getNames().isEmpty()
                        ? person.getNames().get(0).getDisplayName() : "Unknown Contact")
                .collect(Collectors.toList());
    }*/
}

@RestController
@RequestMapping("/contacts")
class GoogleContactsController {
    private final GoogleContactsService googleContactsService;

    public GoogleContactsController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    /*@GetMapping
    public List<String> getContacts(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) throws IOException {
        return googleContactsService.getContacts(authorizedClient.getAccessToken());
    }*/
}
