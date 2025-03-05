package Agramon.com.example.project.Service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.google.api.services.people.v1.model.Name;

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

   public void addContact(OAuth2User user, String name, String email, String phone) throws IOException {
        String clientRegistrationId = "google"; 
        OAuth2AuthorizedClient authorizedClient =
                authorizedClientService.loadAuthorizedClient(clientRegistrationId, user.getName());

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new IllegalStateException("No OAuth2 authorized client found for user.");
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        System.out.println("Access Token: " + accessToken.getTokenValue());

        PeopleService peopleService = new PeopleService.Builder(
                new NetHttpTransport(), jsonFactory, null)
                .setApplicationName("Google Contacts App")
                .setHttpRequestInitializer(request -> request.getHeaders()
                        .setAuthorization("Bearer " + accessToken.getTokenValue()))
                .build();

        Person newContact = new Person()
                .setNames(List.of(new Name().setGivenName(name)))
                .setEmailAddresses(List.of(new EmailAddress().setValue(email)))
                .setPhoneNumbers(List.of(new PhoneNumber().setValue(phone)));

        peopleService.people().createContact(newContact).execute();
        System.out.println(" Contact Added: " + name);
    }

    public void updateContact(OAuth2User user, String resourceName, String newName, String newEmail, String newPhone) throws IOException {
        String clientRegistrationId = "google";
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(clientRegistrationId, user.getName());
    
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new IllegalStateException("No OAuth2 authorized client found for user.");
        }
    
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
    
        PeopleService peopleService = new PeopleService.Builder(new NetHttpTransport(), jsonFactory, null)
                .setApplicationName("Google Contacts App")
                .setHttpRequestInitializer(request -> request.getHeaders().setAuthorization("Bearer " + accessToken.getTokenValue()))
                .build();
    
        Person existingContact = peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers,metadata") 
                .execute();
    
        if (existingContact == null || existingContact.getEtag() == null) {
            throw new IllegalStateException("Could not retrieve contact etag.");
        }
    
        Person updatedPerson = new Person();
        updatedPerson.setEtag(existingContact.getEtag()); 
        updatedPerson.setNames(List.of(new Name().setGivenName(newName)));
    
        if (newEmail != null && !newEmail.isEmpty()) {
            updatedPerson.setEmailAddresses(List.of(new EmailAddress().setValue(newEmail)));
        } else if (existingContact.getEmailAddresses() != null) {
            updatedPerson.setEmailAddresses(existingContact.getEmailAddresses()); 
        }
    
        if (newPhone != null && !newPhone.isEmpty()) {
            updatedPerson.setPhoneNumbers(List.of(new PhoneNumber().setValue(newPhone)));
        } else if (existingContact.getPhoneNumbers() != null) {
            updatedPerson.setPhoneNumbers(existingContact.getPhoneNumbers()); 
        }
    
        peopleService.people().updateContact(resourceName, updatedPerson)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();
    }
    
    public void deleteContact(OAuth2User user, String resourceName) throws IOException {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient("google", user.getName());
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new IllegalStateException("No OAuth2 authorized client found for user.");
        }
        
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        
        PeopleService peopleService = new PeopleService.Builder(
                new NetHttpTransport(), jsonFactory, null)
                .setApplicationName("Google Contacts App")
                .setHttpRequestInitializer(request -> request.getHeaders()
                        .setAuthorization("Bearer " + accessToken.getTokenValue()))
                .build();
        
        peopleService.people().deleteContact(resourceName).execute();
        
        System.out.println("Contact Deleted: " + resourceName);
    }
    
    
    public List<Person> getContacts(OAuth2User user) throws IOException {
        String clientRegistrationId = "google"; 
        OAuth2AuthorizedClient authorizedClient =
                authorizedClientService.loadAuthorizedClient(clientRegistrationId, user.getName());

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new IllegalStateException("No OAuth2 authorized client found for user.");
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        System.out.println("Access Token: " + accessToken.getTokenValue()); 

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
            System.out.println("No contacts retrieved from Google API.");
            return List.of();
        }

        System.out.println(" Contacts Retrieved: " + response.getConnections().size());
        return response.getConnections();
    }
}
