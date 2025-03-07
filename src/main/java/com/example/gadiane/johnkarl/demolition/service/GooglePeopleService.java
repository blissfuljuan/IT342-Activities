package com.example.gadiane.johnkarl.demolition.service;

import com.example.gadiane.johnkarl.demolition.model.ContactForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final Logger logger = LoggerFactory.getLogger(GooglePeopleService.class);
    private static final String PEOPLE_API_BASE_URL = "https://people.googleapis.com/v1";
    
    private final RestTemplate restTemplate;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    public GooglePeopleService(RestTemplate restTemplate, OAuth2AuthorizedClientService authorizedClientService) {
        this.restTemplate = restTemplate;
        this.authorizedClientService = authorizedClientService;
    }

    public Map<String, Object> getContact(OAuth2AuthenticationToken authentication, String resourceId) {
        try {
            if (!authentication.getAuthorizedClientRegistrationId().equals("google")) {
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

            // Format resource ID
            final String formattedResourceId = !resourceId.startsWith("people/") ? "people/" + resourceId : resourceId;

            String getUrl = String.format("%s/%s?personFields=names,phoneNumbers,emailAddresses", 
                    PEOPLE_API_BASE_URL, formattedResourceId);
            
            logger.info("Fetching contact data from URL: {}", getUrl);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            return response.getBody();
        } catch (Exception e) {
            logger.error("Error getting contact: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting contact: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> updateContact(OAuth2AuthenticationToken authentication, String resourceId, ContactForm contactForm) {
        try {
            if (!authentication.getAuthorizedClientRegistrationId().equals("google")) {
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

            // Format resource ID
            final String formattedResourceId = !resourceId.startsWith("people/") ? "people/" + resourceId : resourceId;
            // Fix potential duplicate "people/" prefix
            final String cleanResourceId = formattedResourceId.replace("people/people/", "people/");

            // First, get the existing contact to retrieve its etag
            String getUrl = String.format("%s/%s?personFields=names,phoneNumbers,emailAddresses", 
                    PEOPLE_API_BASE_URL, cleanResourceId);
            
            logger.info("Fetching existing contact data from URL: {}", getUrl);
            
            ResponseEntity<Map> getResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            Map<String, Object> existingContact = getResponse.getBody();
            String etag = (String) existingContact.get("etag");
            
            logger.info("Retrieved etag for contact: {}", etag);

            // Build the update URL
            String updateUrl = String.format("%s/%s:updateContact", PEOPLE_API_BASE_URL, cleanResourceId);
            updateUrl = UriComponentsBuilder.fromUriString(updateUrl)
                    .queryParam("updatePersonFields", "names,phoneNumbers,emailAddresses")
                    .build()
                    .toUriString();
            
            logger.info("Update URL: {}", updateUrl);

            // Create the update payload
            Map<String, Object> updateBody = new HashMap<>();
            
            // Add etag and resourceName
            updateBody.put("etag", etag);
            updateBody.put("resourceName", cleanResourceId);

            // Add names
            List<Map<String, Object>> names = new ArrayList<>();
            Map<String, Object> name = new HashMap<>();
            name.put("givenName", contactForm.getFirstName());
            name.put("familyName", contactForm.getLastName());
            name.put("displayName", contactForm.getFirstName() + " " + contactForm.getLastName());
            name.put("unstructuredName", contactForm.getFirstName() + " " + contactForm.getLastName());
            names.add(name);
            updateBody.put("names", names);

            // Add phone numbers
            if (contactForm.getPhoneNumber() != null && !contactForm.getPhoneNumber().isEmpty()) {
                List<Map<String, Object>> phoneNumbers = new ArrayList<>();
                Map<String, Object> phone = new HashMap<>();
                phone.put("value", contactForm.getPhoneNumber());
                phone.put("type", "mobile");
                phoneNumbers.add(phone);
                updateBody.put("phoneNumbers", phoneNumbers);
            }

            // Add email addresses
            if (contactForm.getEmail() != null && !contactForm.getEmail().isEmpty()) {
                List<Map<String, Object>> emailAddresses = new ArrayList<>();
                Map<String, Object> email = new HashMap<>();
                email.put("value", contactForm.getEmail());
                email.put("type", "home");
                emailAddresses.add(email);
                updateBody.put("emailAddresses", emailAddresses);
            }

            logger.info("Update Request Body: {}", updateBody);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(updateBody, headers);

            // Use PATCH method for updates
            ResponseEntity<Map> updateResponse = restTemplate.exchange(
                    updateUrl,
                    HttpMethod.PATCH,
                    requestEntity,
                    Map.class
            );

            if (updateResponse.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> updatedBody = updateResponse.getBody();
                logger.info("Update Response Body: {}", updatedBody);
                return updatedBody;
            }

            throw new RuntimeException("Update failed with status: " + updateResponse.getStatusCode());

        } catch (Exception e) {
            logger.error("Error updating contact: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating contact: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> createContact(OAuth2AuthenticationToken authentication, ContactForm contactForm) {
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

            // Add names
            List<Map<String, Object>> names = new ArrayList<>();
            Map<String, Object> name = new HashMap<>();
            name.put("givenName", contactForm.getFirstName());
            name.put("familyName", contactForm.getLastName());
            name.put("displayName", contactForm.getFirstName() + " " + contactForm.getLastName());
            name.put("unstructuredName", contactForm.getFirstName() + " " + contactForm.getLastName());
            names.add(name);
            createBody.put("names", names);

            // Add phone numbers
            if (contactForm.getPhoneNumber() != null && !contactForm.getPhoneNumber().isEmpty()) {
                List<Map<String, Object>> phoneNumbers = new ArrayList<>();
                Map<String, Object> phone = new HashMap<>();
                phone.put("value", contactForm.getPhoneNumber());
                phone.put("type", "mobile");
                phoneNumbers.add(phone);
                createBody.put("phoneNumbers", phoneNumbers);
            }

            // Add email addresses
            if (contactForm.getEmail() != null && !contactForm.getEmail().isEmpty()) {
                List<Map<String, Object>> emailAddresses = new ArrayList<>();
                Map<String, Object> email = new HashMap<>();
                email.put("value", contactForm.getEmail());
                email.put("type", "home");
                emailAddresses.add(email);
                createBody.put("emailAddresses", emailAddresses);
            }

            String createUrl = PEOPLE_API_BASE_URL + "/people:createContact";
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(createBody, headers);

            ResponseEntity<Map> createResponse = restTemplate.exchange(
                    createUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (createResponse.getStatusCode().is2xxSuccessful() && createResponse.getBody() != null) {
                Map<String, Object> responseBody = createResponse.getBody();
                logger.info("Contact created successfully: {}", responseBody);
                return responseBody;
            }

            throw new RuntimeException("Failed to create contact");
        } catch (Exception e) {
            logger.error("Error creating contact: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating contact: " + e.getMessage(), e);
        }
    }

    public void deleteContact(OAuth2AuthenticationToken authentication, String resourceId) {
        try {
            if (!authentication.getAuthorizedClientRegistrationId().equals("google")) {
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

            // Format resource ID
            String contactId = resourceId;
            if (resourceId.contains("/")) {
                contactId = resourceId.substring(resourceId.lastIndexOf('/') + 1);
            }

            String url = String.format("%s/people/%s:deleteContact", PEOPLE_API_BASE_URL, contactId);
            logger.info("Attempting to delete contact with URL: {}", url);

            try {
                ResponseEntity<Void> response = restTemplate.exchange(
                        url,
                        HttpMethod.DELETE,
                        new HttpEntity<>(headers),
                        Void.class
                );

                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("Contact deleted successfully with status: {}", response.getStatusCode());
                } else {
                    throw new RuntimeException("Failed to delete contact, unexpected status: " + response.getStatusCode());
                }

            } catch (Exception e) {
                logger.error("Error deleting contact: {}", e.getMessage(), e);
                throw e;
            }

        } catch (Exception e) {
            logger.error("Error in deleteContact: {}", e.getMessage(), e);
            throw new RuntimeException("Error deleting contact: " + e.getMessage(), e);
        }
    }
}
