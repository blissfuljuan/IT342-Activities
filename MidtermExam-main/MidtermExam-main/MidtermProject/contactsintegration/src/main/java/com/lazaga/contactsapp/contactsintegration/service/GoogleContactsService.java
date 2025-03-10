package com.lazaga.contactsapp.contactsintegration.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;


@Service
public class GoogleContactsService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestTemplate restTemplate;

    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
        this.restTemplate = new RestTemplate();
    }

    public String getAccessToken(String userName) {
        try {
            OAuth2AuthorizedClient client = authorizedClientService
                    .loadAuthorizedClient("google", userName);
            
            if (client == null) {
                throw new RuntimeException("OAuth2 client not found. User may need to re-authenticate.");
            }
            
            OAuth2AccessToken accessToken = client.getAccessToken();
            return accessToken.getTokenValue();
        } catch (Exception e) {
            System.err.println("Error getting access token: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get access token: " + e.getMessage(), e);
        }
    }

    public String getContacts(String userName) {
        String url = "https://people.googleapis.com/v1/people/me/connections"
                   + "?personFields=names,emailAddresses,phoneNumbers,metadata"
                   + "&sortOrder=LAST_MODIFIED_DESCENDING";
        
        try {
            System.out.println("Fetching contacts for user: " + userName);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAccessToken(userName));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            System.out.println("Making request to: " + url);
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            System.out.println("Response status: " + response.getStatusCode());
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("REST client error fetching contacts: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error fetching contacts: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Unexpected error fetching contacts: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error fetching contacts: " + e.getMessage(), e);
        }
    }

    private String getAccessToken() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );
            if (client != null && client.getAccessToken() != null) {
                return client.getAccessToken().getTokenValue();
            }
        }
        throw new RuntimeException("OAuth2 authentication failed!");
    }

 
    public String createContact(String userName, Map<String, Object> contactData) {
        String url = "https://people.googleapis.com/v1/people:createContact";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAccessToken(userName));
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(contactData, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody();
    }

    private PeopleService createPeopleService() {
        return new PeopleService.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.gson.GsonFactory(),
                request -> request.getHeaders().setAuthorization("Bearer " + getAccessToken())
        ).setApplicationName("Google Contacts App").build();
    }

    public void updateContact(String resourceName, String familyName, List<String> emails, List<String> phoneNumbers) throws IOException {
        PeopleService peopleService = createPeopleService();
        Person existingContact = peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        List<EmailAddress> emailAddressList = emails != null && !emails.isEmpty()
                ? emails.stream().map(email -> new EmailAddress().setValue(email)).collect(Collectors.toList())
                : null;

        List<PhoneNumber> phoneNumberList = phoneNumbers != null && !phoneNumbers.isEmpty()
                ? phoneNumbers.stream().map(phone -> new PhoneNumber().setValue(phone)).collect(Collectors.toList())
                : null;

        Person updatedContact = new Person()
                .setEtag(existingContact.getEtag())
                .setNames(List.of(new Name().setGivenName(null).setFamilyName(familyName)))
                .setEmailAddresses(emailAddressList)
                .setPhoneNumbers(phoneNumberList);

        peopleService.people().updateContact(resourceName, updatedContact)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();
    }

    public void deleteContact(String resourceName) throws IOException {
        PeopleService peopleService = createPeopleService();
        peopleService.people().deleteContact(resourceName).execute();
    }

}