package com.pepitoJP.midterm.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.pepitoJP.midterm.Model.Contact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoogleContactsService {

    @Autowired
    OAuth2AuthorizedClientService clientService;

    public GoogleContactsService(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    // ------------------ Existing Methods ------------------

    public Map<String, Object> getContacts(OAuth2User oAuth2User) {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient("google", oAuth2User.getAttribute("sub"));
        if (client == null) {
            throw new IllegalStateException("No authorized client found for Google");
        }
        String accessToken = client.getAccessToken().getTokenValue();
        String url = "https://people.googleapis.com/v1/people/me/connections"
                + "?personFields=names,emailAddresses,phoneNumbers";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        return response.getBody();
    }

    public List<Map<String, String>> formatContacts(Map<String, Object> contacts) {
        List<Map<String, Object>> connections = (List<Map<String, Object>>) contacts.getOrDefault("connections", List.of());
        return connections.stream()
                .map(contact -> Map.of(
                        "name", getValue(contact, "names"),
                        "email", getValue(contact, "emailAddresses"),
                        "phone", getValue(contact, "phoneNumbers"),
                        "resourceName", (String) contact.get("resourceName")
                ))
                .collect(Collectors.toList());
    }

    /**
     * Updated getValue method: for phoneNumbers, join all numbers with a comma.
     */
    private String getValue(Map<String, Object> contact, String key) {
        List<Map<String, Object>> values = (List<Map<String, Object>>) contact.get(key);
        if (values != null && !values.isEmpty()) {
            if ("names".equals(key)) {
                return (String) values.get(0).getOrDefault("displayName", "N/A");
            } else if ("emailAddresses".equals(key)) {
                return (String) values.get(0).getOrDefault("value", "N/A");
            } else if ("phoneNumbers".equals(key)) {
                // Join all phone numbers with a comma
                return values.stream()
                        .map(map -> (String) map.getOrDefault("value", "N/A"))
                        .collect(Collectors.joining(", "));
            }
        }
        return "N/A";
    }

    /**
     * Helper to build PeopleService using GsonFactory.
     */
    private PeopleService buildPeopleService(String accessToken) {
        JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        return new PeopleService.Builder(
                new NetHttpTransport(),
                JSON_FACTORY,
                credential
        )
                .setApplicationName("midterm")
                .build();
    }

    // ------------------ New CRUD Methods ------------------

    // Create (Add) Contact – supports multiple phone numbers (comma-separated)
    public Person addContactWithPeopleService(OAuth2User oAuth2User, Contact contact) throws IOException {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient("google", oAuth2User.getAttribute("sub"));
        if (client == null) {
            throw new IllegalStateException("No authorized client found for Google");
        }
        String accessToken = client.getAccessToken().getTokenValue();
        PeopleService peopleService = buildPeopleService(accessToken);

        Person newContact = new Person();

        // Set names
        List<Name> names = new ArrayList<>();
        Name name = new Name();
        name.setGivenName(contact.getFirstName());
        name.setFamilyName(contact.getLastName());
        names.add(name);
        newContact.setNames(names);

        // Set email addresses
        List<EmailAddress> emails = new ArrayList<>();
        EmailAddress email = new EmailAddress();
        email.setValue(contact.getEmail());
        emails.add(email);
        newContact.setEmailAddresses(emails);

        // Set phone numbers (split comma-separated values)
        if (contact.getPhone() != null && !contact.getPhone().trim().isEmpty()) {
            String[] phoneArr = contact.getPhone().split(",");
            List<PhoneNumber> phoneNumbers = new ArrayList<>();
            for (String phone : phoneArr) {
                phoneNumbers.add(new PhoneNumber().setValue(phone.trim()));
            }
            newContact.setPhoneNumbers(phoneNumbers);
        }

        return peopleService.people().createContact(newContact).execute();
    }

    // Update Contact – supports updating multiple phone numbers.
    public Person updateContactWithPeopleService(OAuth2User oAuth2User, String resourceName, Contact contact)
            throws IOException {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient("google", oAuth2User.getAttribute("sub"));
        if (client == null) {
            throw new IllegalStateException("No authorized client found for Google");
        }
        String accessToken = client.getAccessToken().getTokenValue();
        PeopleService peopleService = buildPeopleService(accessToken);

        // Retrieve existing contact with metadata (includes etag)
        Person contactToUpdate = peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers,metadata")
                .execute();

        // Update email addresses
        List<EmailAddress> emailAddresses = new ArrayList<>();
        emailAddresses.add(new EmailAddress().setValue(contact.getEmail()));
        contactToUpdate.setEmailAddresses(emailAddresses);

        // Update phone numbers: split input by newline instead of comma
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        if (contact.getPhone() != null && !contact.getPhone().trim().isEmpty()) {
            String[] phoneArr = contact.getPhone().split("\\r?\\n");
            for (String phone : phoneArr) {
                if (!phone.trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber().setValue(phone.trim()));
                }
            }
        }
        contactToUpdate.setPhoneNumbers(phoneNumbers);

        return peopleService.people()
                .updateContact(contactToUpdate.getResourceName(), contactToUpdate)
                .setUpdatePersonFields("emailAddresses,phoneNumbers")
                .execute();
    }

    // Delete Contact remains unchanged.
    public void deleteContactWithPeopleService(OAuth2User oAuth2User, String resourceName) throws IOException {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient("google", oAuth2User.getAttribute("sub"));
        if (client == null) {
            throw new IllegalStateException("No authorized client found for Google");
        }
        String accessToken = client.getAccessToken().getTokenValue();
        PeopleService peopleService = buildPeopleService(accessToken);
        peopleService.people().deleteContact(resourceName).execute();
    }
}
