package com.kho.googlecontacts.service;


import com.kho.googlecontacts.model.Contact;
import org.springframework.http.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;


@Service
public class GoogleContactsService {

    private final RestTemplate restTemplate;

    // Inject RestTemplate for making API calls
    public GoogleContactsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Fetch contacts from Google Contacts API
    public List<Contact> getContacts(OAuth2User principal) {
        String accessToken = (String) principal.getAttributes().get("access_token");
        String apiUrl = "https://people.googleapis.com/v1/people/me/connections?personFields=emailAddresses,phoneNumbers";

        List<Contact> contacts = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);
            Map<String, Object> data = response.getBody();
            List<Map<String, Object>> connections = (List<Map<String, Object>>) data.get("connections");

            for (Map<String, Object> connection : connections) {
                String name = (String) connection.get("names");  // Adjust to actual response key
                List<String> emails = new ArrayList<>();
                List<String> phones = new ArrayList<>();

                if (connection.containsKey("emailAddresses")) {
                    emails = (List<String>) connection.get("emailAddresses");
                }
                if (connection.containsKey("phoneNumbers")) {
                    phones = (List<String>) connection.get("phoneNumbers");
                }

                contacts.add(new Contact(name, emails, phones));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return contacts;
    }

    // Add Contact
    public void addContact(OAuth2User principal, Contact contact) {
        String accessToken = (String) principal.getAttributes().get("access_token");
        String apiUrl = "https://people.googleapis.com/v1/people:createContact"; // Adjust as needed

        // Call the API with the necessary body for creating the contact
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String json = "{ \"names\": [{\"givenName\": \"" + contact.getName() + "\"}], "
                    + "\"emailAddresses\": [{\"value\": \"" + contact.getEmails().get(0) + "\"}], "
                    + "\"phoneNumbers\": [{\"value\": \"" + contact.getPhones().get(0) + "\"}]}";

            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Modify Contact
    public void modifyContact(OAuth2User principal, Contact contact) {
        // Similar logic for modifying contact
    }

    // Remove Contact
    public void removeContact(OAuth2User principal, Long contactId) {
        // Implement the API call for removing contact
    }
}
