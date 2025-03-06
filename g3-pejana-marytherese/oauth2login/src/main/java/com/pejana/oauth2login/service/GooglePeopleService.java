package com.pejana.oauth2login.service;

import com.pejana.oauth2login.model.Contact;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
public class GooglePeopleService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestTemplate restTemplate;

    public GooglePeopleService(OAuth2AuthorizedClientService authorizedClientService, RestTemplate restTemplate) {
        this.authorizedClientService = authorizedClientService;
        this.restTemplate = restTemplate;
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


            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);


            String url = UriComponentsBuilder.fromHttpUrl("https://people.googleapis.com/v1/people/me/connections")
                    .queryParam("personFields", "names,phoneNumbers,emailAddresses,metadata")
                    .queryParam("sortOrder", "LAST_MODIFIED_DESCENDING")
                    .queryParam("pageSize", "1000") // Increase page size to get more contacts
                    .build()
                    .toUriString();

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
                        String etag = (String) connection.get("etag");


                        if (connection.containsKey("names")) {
                            List<LinkedHashMap<String, Object>> names =
                                    (List<LinkedHashMap<String, Object>>) connection.get("names");
                            if (names != null && !names.isEmpty() && names.get(0).containsKey("displayName")) {
                                name = (String) names.get(0).get("displayName");
                            }
                        }


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

                        // Get metadata for last updated time
                        Map<String, Object> metadata = (Map<String, Object>) connection.get("metadata");
                        if (metadata != null && metadata.containsKey("sources")) {
                            List<Map<String, Object>> sources = (List<Map<String, Object>>) metadata.get("sources");
                            if (sources != null && !sources.isEmpty()) {
                                // Use the most recent update time
                                String updateTime = (String) metadata.get("updateTime");
                                System.out.println("Contact " + name + " was last updated at: " + updateTime);
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

    @SuppressWarnings("unchecked")
    public Contact updateContact(OAuth2AuthenticationToken authentication, String resourceId, Contact updatedContact) {
        try {
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

            final String formattedResourceId = !resourceId.startsWith("people/") ? "people/" + resourceId : resourceId;

            String getUrl = String.format("https://people.googleapis.com/v1/%s?personFields=names,phoneNumbers,emailAddresses", formattedResourceId);
            ResponseEntity<Map> getResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            Map<String, Object> existingContact = getResponse.getBody();
            String etag = (String) existingContact.get("etag");

            String updateUrl = String.format("https://people.googleapis.com/v1/%s:updateContact", formattedResourceId);
            updateUrl = UriComponentsBuilder.fromUriString(updateUrl)
                    .queryParam("updatePersonFields", "names,phoneNumbers,emailAddresses")
                    .build()
                    .toUriString();

            Map<String, Object> updateBody = new HashMap<>();

            updateBody.put("etag", etag);
            updateBody.put("resourceName", formattedResourceId);

            List<Map<String, Object>> names = new ArrayList<>();
            Map<String, Object> name = new HashMap<>();
            name.put("givenName", updatedContact.getName());
            name.put("displayName", updatedContact.getName());
            name.put("unstructuredName", updatedContact.getName());
            names.add(name);
            updateBody.put("names", names);

            List<Map<String, Object>> phoneNumbers = new ArrayList<>();
            Map<String, Object> phone = new HashMap<>();
            phone.put("value", updatedContact.getPhoneNumber());
            phone.put("type", "mobile");
            phoneNumbers.add(phone);
            updateBody.put("phoneNumbers", phoneNumbers);


            List<Map<String, Object>> emailAddresses = new ArrayList<>();
            Map<String, Object> email = new HashMap<>();
            email.put("value", updatedContact.getEmail());
            email.put("type", "home");
            emailAddresses.add(email);
            updateBody.put("emailAddresses", emailAddresses);


            System.out.println("Update Request Body: " + updateBody);


            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(updateBody, headers);


            ResponseEntity<Map> updateResponse = restTemplate.exchange(
                    updateUrl,
                    HttpMethod.PATCH,
                    requestEntity,
                    Map.class
            );

            if (updateResponse.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> updatedBody = updateResponse.getBody();
                System.out.println("Update Response Body: " + updatedBody);


                return new Contact(
                        formattedResourceId,
                        updatedContact.getName(),
                        updatedContact.getEmail(),
                        updatedContact.getPhoneNumber()
                );
            }

            throw new RuntimeException("Update failed with status: " + updateResponse.getStatusCode());

        } catch (Exception e) {
            System.err.println("Error updating contact: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating contact: " + e.getMessage());
        }
    }


    @SuppressWarnings("unchecked")
    private String extractName(Map<String, Object> body) {
        if (body != null && body.containsKey("names")) {
            List<Map<String, Object>> names = (List<Map<String, Object>>) body.get("names");
            if (names != null && !names.isEmpty()) {
                Map<String, Object> name = names.get(0);
                String displayName = (String) name.get("displayName");
                return displayName != null ? displayName : "";
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private String extractPhone(Map<String, Object> body) {
        if (body != null && body.containsKey("phoneNumbers")) {
            List<Map<String, Object>> phones = (List<Map<String, Object>>) body.get("phoneNumbers");
            if (phones != null && !phones.isEmpty()) {
                Map<String, Object> phone = phones.get(0);
                String value = (String) phone.get("value");
                return value != null ? value : "";
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private String extractEmail(Map<String, Object> body) {
        if (body != null && body.containsKey("emailAddresses")) {
            List<Map<String, Object>> emails = (List<Map<String, Object>>) body.get("emailAddresses");
            if (emails != null && !emails.isEmpty()) {
                Map<String, Object> email = emails.get(0);
                String value = (String) email.get("value");
                return value != null ? value : "";
            }
        }
        return "";
    }


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
            headers.setContentType(MediaType.APPLICATION_JSON);

            String contactId = resourceId;
            if (resourceId.contains("/")) {
                contactId = resourceId.substring(resourceId.lastIndexOf('/') + 1);
            }

            String url = String.format("https://people.googleapis.com/v1/people/%s:deleteContact", contactId);
            System.out.println("Attempting to delete contact with URL: " + url);

            try {
                ResponseEntity<Void> response = restTemplate.exchange(
                        url,
                        HttpMethod.DELETE,
                        new HttpEntity<>(headers),
                        Void.class
                );

                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Contact deleted successfully with status: " + response.getStatusCode());
                } else {
                    throw new RuntimeException("Failed to delete contact, unexpected status: " + response.getStatusCode());
                }

            } catch (Exception e) {
                System.err.println("Error deleting contact: " + e.getMessage());
                throw e;
            }

        } catch (Exception e) {
            System.err.println("Error in deleteContact: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting contact: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public Contact createContact(OAuth2AuthenticationToken authentication, Contact newContact) {
        try {
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


            Map<String, Object> createBody = new HashMap<>();


            List<Map<String, Object>> names = new ArrayList<>();
            Map<String, Object> name = new HashMap<>();
            name.put("givenName", newContact.getName());
            name.put("displayName", newContact.getName());
            name.put("unstructuredName", newContact.getName());
            names.add(name);
            createBody.put("names", names);


            if (newContact.getPhoneNumber() != null && !newContact.getPhoneNumber().isEmpty()) {
                List<Map<String, Object>> phoneNumbers = new ArrayList<>();
                Map<String, Object> phone = new HashMap<>();
                phone.put("value", newContact.getPhoneNumber());
                phone.put("type", "mobile");
                phoneNumbers.add(phone);
                createBody.put("phoneNumbers", phoneNumbers);
            }


            if (newContact.getEmail() != null && !newContact.getEmail().isEmpty()) {
                List<Map<String, Object>> emailAddresses = new ArrayList<>();
                Map<String, Object> email = new HashMap<>();
                email.put("value", newContact.getEmail());
                email.put("type", "home");
                emailAddresses.add(email);
                createBody.put("emailAddresses", emailAddresses);
            }


            String createUrl = "https://people.googleapis.com/v1/people:createContact";
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(createBody, headers);

            ResponseEntity<Map> createResponse = restTemplate.exchange(
                    createUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (createResponse.getStatusCode().is2xxSuccessful() && createResponse.getBody() != null) {
                Map<String, Object> responseBody = createResponse.getBody();
                String resourceId = (String) responseBody.get("resourceName");

                return new Contact(
                        resourceId,
                        newContact.getName(),
                        newContact.getEmail(),
                        newContact.getPhoneNumber()
                );
            }

            throw new RuntimeException("Failed to create contact");
        } catch (Exception e) {
            System.err.println("Error creating contact: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating contact: " + e.getMessage());
        }
    }
}