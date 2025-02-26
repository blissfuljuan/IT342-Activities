package com.capuras.oauth2login.service;

import com.capuras.oauth2login.model.Contact;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GooglePeopleService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestTemplate restTemplate;

    public GooglePeopleService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get the user's phone number from their Google profile
     */
    @SuppressWarnings("unchecked")
    public String getPhoneNumber(OAuth2AuthenticationToken authentication) {
        try {
            if (!"google".equals(authentication.getAuthorizedClientRegistrationId())) {
                return "Not a Google account";
            }

            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    authentication.getAuthorizedClientRegistrationId(),
                    authentication.getName()
            );

            if (client == null) {
                return "No authorized client found";
            }

            String accessToken = client.getAccessToken().getTokenValue();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            // Try with expanded fields
            String url = "https://people.googleapis.com/v1/people/me?personFields=phoneNumbers";

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("phoneNumbers")) {
                List<LinkedHashMap<String, Object>> phoneNumbers = (List<LinkedHashMap<String, Object>>) body.get("phoneNumbers");

                if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
                    for (LinkedHashMap<String, Object> phoneEntry : phoneNumbers) {
                        if (phoneEntry.containsKey("value")) {
                            return (String) phoneEntry.get("value");
                        } else if (phoneEntry.containsKey("canonicalForm")) {
                            return (String) phoneEntry.get("canonicalForm");
                        }
                    }
                }
            }

            return "No phone number found in your Google profile.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error retrieving phone number: " + e.getMessage();
        }
    }

    /**
     * Get contacts with their phone numbers as a map with resourceId as key
     */
    @SuppressWarnings("unchecked")
    public Map<String, Contact> getContactsMap(OAuth2AuthenticationToken authentication) {
        Map<String, Contact> contactsMap = new HashMap<>();

        try {
            if (!"google".equals(authentication.getAuthorizedClientRegistrationId())) {
                return contactsMap;
            }

            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    authentication.getAuthorizedClientRegistrationId(),
                    authentication.getName()
            );

            if (client == null) {
                return contactsMap;
            }

            String accessToken = client.getAccessToken().getTokenValue();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            // Get contacts with phone numbers
            String url = "https://people.googleapis.com/v1/people/me/connections?personFields=names,phoneNumbers,emailAddresses&pageSize=20";

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("connections")) {
                List<LinkedHashMap<String, Object>> connections = (List<LinkedHashMap<String, Object>>) body.get("connections");
                if (connections != null && !connections.isEmpty()) {
                    for (LinkedHashMap<String, Object> connection : connections) {
                        String resourceId = (String) connection.get("resourceName");
                        String name = "Unknown";
                        String email = "";
                        String phone = "No phone";

                        // Get name
                        if (connection.containsKey("names")) {
                            List<LinkedHashMap<String, Object>> names =
                                    (List<LinkedHashMap<String, Object>>) connection.get("names");
                            if (names != null && !names.isEmpty() && names.get(0).containsKey("displayName")) {
                                name = (String) names.get(0).get("displayName");
                            }
                        }

                        // Get email
                        if (connection.containsKey("emailAddresses")) {
                            List<LinkedHashMap<String, Object>> emails =
                                    (List<LinkedHashMap<String, Object>>) connection.get("emailAddresses");
                            if (emails != null && !emails.isEmpty() && emails.get(0).containsKey("value")) {
                                email = (String) emails.get(0).get("value");
                            }
                        }

                        // Get phone
                        if (connection.containsKey("phoneNumbers")) {
                            List<LinkedHashMap<String, Object>> phoneNumbers =
                                    (List<LinkedHashMap<String, Object>>) connection.get("phoneNumbers");
                            if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
                                LinkedHashMap<String, Object> phoneEntry = phoneNumbers.get(0);
                                if (phoneEntry.containsKey("value")) {
                                    phone = (String) phoneEntry.get("value");
                                }
                            }
                        }

                        Contact contact = new Contact(resourceId, name, email, phone);
                        contactsMap.put(resourceId, contact);
                    }
                }
            }

            return contactsMap;
        } catch (Exception e) {
            e.printStackTrace();
            return contactsMap;
        }
    }

    /**
     * Update a contact in Google Contacts
     */
    @SuppressWarnings("unchecked")
    public Contact updateContact(OAuth2AuthenticationToken authentication, String resourceId, Contact updatedContact) {
        try {
            if (!"google".equals(authentication.getAuthorizedClientRegistrationId())) {
                throw new RuntimeException("Not a Google account");
            }

            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    authentication.getAuthorizedClientRegistrationId(),
                    authentication.getName()
            );

            if (client == null) {
                throw new RuntimeException("No authorized client found");
            }

            String accessToken = client.getAccessToken().getTokenValue();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // First, get the current contact data
            String getUrl = "https://people.googleapis.com/v1/" + resourceId + "?personFields=names,phoneNumbers";
            ResponseEntity<Map> getResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            Map<String, Object> contactData = getResponse.getBody();

            // Prepare update data
            Map<String, Object> updateData = new HashMap<>();

            // Update name if it exists
            if (contactData != null && contactData.containsKey("names")) {
                List<Map<String, Object>> names = (List<Map<String, Object>>) contactData.get("names");
                if (names != null && !names.isEmpty()) {
                    Map<String, Object> nameData = new HashMap<>(names.get(0));
                    nameData.put("displayName", updatedContact.getName());
                    nameData.put("unstructuredName", updatedContact.getName());

                    List<Map<String, Object>> updatedNames = new ArrayList<>();
                    updatedNames.add(nameData);
                    updateData.put("names", updatedNames);
                }
            }

            // Update phone number
            List<Map<String, Object>> updatedPhones = new ArrayList<>();
            Map<String, Object> phoneData = new HashMap<>();
            phoneData.put("value", updatedContact.getPhoneNumber());
            phoneData.put("type", "mobile");
            updatedPhones.add(phoneData);
            updateData.put("phoneNumbers", updatedPhones);

            // Set update mask
            String updateMask = "names,phoneNumbers";

            // Perform update
            String updateUrl = "https://people.googleapis.com/v1/" + resourceId + "?updatePersonFields=" + updateMask;

            ResponseEntity<Map> updateResponse = restTemplate.exchange(
                    updateUrl,
                    HttpMethod.PATCH,
                    new HttpEntity<>(updateData, headers),
                    Map.class
            );

            Map<String, Object> updatedData = updateResponse.getBody();
            if (updatedData != null) {
                return new Contact(resourceId, updatedContact.getName(), updatedContact.getEmail(), updatedContact.getPhoneNumber());
            } else {
                throw new RuntimeException("Failed to update contact");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating contact: " + e.getMessage());
        }
    }

    /**
     * Delete a contact from Google Contacts
     */
    public void deleteContact(OAuth2AuthenticationToken authentication, String resourceId) {
        try {
            if (!"google".equals(authentication.getAuthorizedClientRegistrationId())) {
                throw new RuntimeException("Not a Google account");
            }

            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    authentication.getAuthorizedClientRegistrationId(),
                    authentication.getName()
            );

            if (client == null) {
                throw new RuntimeException("No authorized client found");
            }

            String accessToken = client.getAccessToken().getTokenValue();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            // Delete contact
            String url = "https://people.googleapis.com/v1/" + resourceId + ":deleteContact";

            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    new HttpEntity<>(headers),
                    Void.class
            );

            if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
                throw new RuntimeException("Failed to delete contact, status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting contact: " + e.getMessage());
        }
    }
}