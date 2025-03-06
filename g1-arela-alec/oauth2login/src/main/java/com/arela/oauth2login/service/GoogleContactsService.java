package com.arela.oauth2login.service;

// test comment
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;

// test
@Service
public class GoogleContactsService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(GoogleContactsService.class);

    @Autowired
    public GoogleContactsService(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
    }

    // Fetch contacts
    public String getContacts(String accessToken) {
        String url = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses,phoneNumbers,birthdays";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException.Unauthorized e) {
            logger.error("Unauthorized access token", e);
            return "Error: Access token is expired or invalid. Please log in again.";
        } catch (Exception e) {
            logger.error("Error fetching contacts", e);
            return "Error fetching contacts: " + e.getMessage();
        }
    }

    // Add a new contact
    public String addContact(String accessToken, String firstName, String lastName, String birthday, List<String> emails, List<String> phoneNumbers) {
        String url = "https://people.googleapis.com/v1/people:createContact";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.putArray("names").addObject().put("givenName", firstName).put("familyName", lastName);
        
        // Add birthday if provided
        if (birthday != null && !birthday.isEmpty()) {
            ObjectNode birthdayNode = requestBody.putArray("birthdays").addObject();
            String[] dateParts = birthday.split("-");
            ObjectNode dateNode = birthdayNode.putObject("date");
            dateNode.put("year", Integer.parseInt(dateParts[0]));
            dateNode.put("month", Integer.parseInt(dateParts[1]));
            dateNode.put("day", Integer.parseInt(dateParts[2]));
        }
        
        // Add multiple email addresses
        ArrayNode emailArray = requestBody.putArray("emailAddresses");
        for (String email : emails) {
            if (email != null && !email.isEmpty()) {
                emailArray.addObject().put("value", email);
            }
        }
        
        // Add multiple phone numbers
        ArrayNode phoneArray = requestBody.putArray("phoneNumbers");
        for (String phone : phoneNumbers) {
            if (phone != null && !phone.isEmpty()) {
                phoneArray.addObject().put("value", phone);
            }
        }

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException.Unauthorized e) {
            logger.error("Unauthorized access token", e);
            return "Error: Access token is expired or invalid. Please log in again.";
        } catch (Exception e) {
            logger.error("Error adding contact", e);
            return "Error adding contact: " + e.getMessage();
        }
    }

    public String updateContact(String accessToken, String resourceName, String firstName, String lastName,
            String birthday, List<String> emails, List<String> phoneNumbers) {

        // Build the update mask based on which fields are being updated
        StringBuilder updateMask = new StringBuilder();
        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            updateMask.append("names,");
        }
        if (birthday != null && !birthday.isEmpty()) {
            updateMask.append("birthdays,");
        }
        if (emails != null && !emails.isEmpty()) {
            updateMask.append("emailAddresses,");
        }
        if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            updateMask.append("phoneNumbers,");
        }
        
        // Remove the trailing comma if present
        if (updateMask.length() > 0) {
            updateMask.setLength(updateMask.length() - 1);
        }

        String url = "https://people.googleapis.com/v1/" + resourceName + ":updateContact?updatePersonFields=" + updateMask.toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // First, get the current contact to get its etag
        String getUrl = "https://people.googleapis.com/v1/" + resourceName + "?personFields=names,birthdays,emailAddresses,phoneNumbers";
        HttpEntity<String> getEntity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> getResponse = restTemplate.exchange(getUrl, HttpMethod.GET, getEntity, String.class);
            ObjectNode currentContact = (ObjectNode) objectMapper.readTree(getResponse.getBody());
            String etag = currentContact.get("etag").asText();

            // Create the request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("etag", etag);

            // Add the fields to update
            if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
                requestBody.putArray("names").addObject().put("givenName", firstName).put("familyName", lastName);
            }
            
            // Add birthday if provided
            if (birthday != null && !birthday.isEmpty()) {
                ObjectNode birthdayNode = requestBody.putArray("birthdays").addObject();
                String[] dateParts = birthday.split("-");
                ObjectNode dateNode = birthdayNode.putObject("date");
                dateNode.put("year", Integer.parseInt(dateParts[0]));
                dateNode.put("month", Integer.parseInt(dateParts[1]));
                dateNode.put("day", Integer.parseInt(dateParts[2]));
            }
            
            // Add multiple email addresses
            if (emails != null && !emails.isEmpty()) {
                ArrayNode emailArray = requestBody.putArray("emailAddresses");
                for (String email : emails) {
                    if (email != null && !email.isEmpty()) {
                        emailArray.addObject().put("value", email);
                    }
                }
            }
            
            // Add multiple phone numbers
            if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
                ArrayNode phoneArray = requestBody.putArray("phoneNumbers");
                for (String phone : phoneNumbers) {
                    if (phone != null && !phone.isEmpty()) {
                        phoneArray.addObject().put("value", phone);
                    }
                }
            }

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            logger.info("Update Contact Request Body: {}", requestBody.toString());
            logger.info("Update Contact URL: {}", url);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException.Unauthorized e) {
            logger.error("Unauthorized access token", e);
            return "Error: Access token is expired or invalid. Please log in again.";
        } catch (Exception e) {
            logger.error("Error updating contact", e);
            return "Error updating contact: " + e.getMessage();
        }
    }

    // Delete a contact
    public String deleteContact(String accessToken, String resourceName) {
        String url = "https://people.googleapis.com/v1/" + resourceName + ":deleteContact";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            return response.getStatusCode() == HttpStatus.NO_CONTENT ? "Contact deleted successfully"
                    : "Failed to delete contact";
        } catch (HttpClientErrorException.Unauthorized e) {
            logger.error("Unauthorized access token", e);
            return "Error: Access token is expired or invalid. Please log in again.";
        } catch (Exception e) {
            logger.error("Error deleting contact", e);
            return "Error deleting contact: " + e.getMessage();
        }
    }
}