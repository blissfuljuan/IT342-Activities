package com.example.gadiane.johnkarl.demolition.service;

import com.example.gadiane.johnkarl.demolition.model.ContactForm;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.FieldMetadata;
import com.google.api.services.people.v1.model.Person;
/*
import com.google.api.services.people.v1.model.PersonName;
*/
import com.google.api.services.people.v1.model.PhoneNumber;
import com.google.api.services.people.v1.model.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoogleContactsService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleContactsService.class);
    private static final String PEOPLE_API_BASE_URL = "https://people.googleapis.com/v1";
    private static final String CONTACTS_RESOURCE = "/people:createContact";
    private static final String CONTACTS_LIST_URL = "/people/me/connections";
    private static final String CONTACT_FIELDS = "names,emailAddresses,phoneNumbers";
    
    private final GoogleCredentialService googleCredentialService;
    private final HttpTransport httpTransport;
    private final JsonFactory jsonFactory;

    public GoogleContactsService(GoogleCredentialService googleCredentialService) {
        this.googleCredentialService = googleCredentialService;
        this.httpTransport = new NetHttpTransport();
        this.jsonFactory = GsonFactory.getDefaultInstance();
    }

    public Map<String, Object> createContact(ContactForm contactForm) throws IOException {
        logger.info("Creating contact for: {} {}", contactForm.getFirstName(), contactForm.getLastName());
        
        Map<String, Object> contactData = createContactPayload(contactForm);
        
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory(request -> 
            request.setParser(jsonFactory.createJsonObjectParser())
                  .setHeaders(request.getHeaders().setAuthorization(
                      "Bearer " + googleCredentialService.getCredential().getAccessToken())));
        
        JsonHttpContent content = new JsonHttpContent(jsonFactory, contactData);
        
        HttpRequest request = requestFactory.buildPostRequest(
            new GenericUrl(PEOPLE_API_BASE_URL + CONTACTS_RESOURCE), content);
        
        try {
            HttpResponse response = request.execute();
            Map<String, Object> result = response.parseAs(Map.class);
            logger.info("Contact created successfully with resource name: {}", result.get("resourceName"));
            return result;
        } catch (GoogleJsonResponseException e) {
            logger.error("Error creating contact: {}", e.getDetails().getMessage());
            throw e;
        }
    }
    
    public List<Map<String, Object>> listContacts() throws IOException {
        logger.info("Fetching contacts list");
        
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory(request -> 
            request.setParser(jsonFactory.createJsonObjectParser())
                  .setHeaders(request.getHeaders().setAuthorization(
                      "Bearer " + googleCredentialService.getCredential().getAccessToken())));
        
        GenericUrl url = new GenericUrl(PEOPLE_API_BASE_URL + CONTACTS_LIST_URL);
        url.put("personFields", CONTACT_FIELDS);
        url.put("pageSize", 100);
        
        HttpRequest request = requestFactory.buildGetRequest(url);
        
        try {
            HttpResponse response = request.execute();
            Map<String, Object> result = response.parseAs(Map.class);
            
            List<Map<String, Object>> connections = (List<Map<String, Object>>) result.get("connections");
            if (connections == null) {
                return new ArrayList<>();
            }
            
            logger.info("Retrieved {} contacts", connections.size());
            return connections;
        } catch (GoogleJsonResponseException e) {
            logger.error("Error fetching contacts: {}", e.getDetails().getMessage());
            throw e;
        }
    }
    
    public Map<String, Object> getContact(String resourceName) throws IOException {
        logger.info("Fetching contact with resource name: {}", resourceName);
        
        // Ensure resourceName is properly formatted
        if (!resourceName.startsWith("people/")) {
            resourceName = "people/" + resourceName;
        }
        
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory(request -> 
            request.setParser(jsonFactory.createJsonObjectParser())
                  .setHeaders(request.getHeaders().setAuthorization(
                      "Bearer " + googleCredentialService.getCredential().getAccessToken())));
        
        GenericUrl url = new GenericUrl(PEOPLE_API_BASE_URL + "/" + resourceName);
        url.put("personFields", CONTACT_FIELDS);
        
        HttpRequest request = requestFactory.buildGetRequest(url);
        
        try {
            HttpResponse response = request.execute();
            Map<String, Object> result = response.parseAs(Map.class);
            logger.info("Retrieved contact: {}", resourceName);
            return result;
        } catch (GoogleJsonResponseException e) {
            logger.error("Error fetching contact: {}", e.getDetails().getMessage());
            throw e;
        }
    }
    
    public Map<String, Object> updateContact(ContactForm contactForm) throws IOException {
        try {
            logger.info("Updating contact with resourceName: {}", contactForm.getResourceName());
            
            // Ensure resourceName is properly formatted
            String resourceName = contactForm.getResourceName();
            if (!resourceName.startsWith("people/")) {
                resourceName = "people/" + resourceName;
            }
            
            // Get OAuth2 access token
            String accessToken = googleCredentialService.getCredential().getAccessToken();
            logger.info("Access token obtained for update operation");
            
            // Create headers with OAuth2 token
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create the contact data payload
            Map<String, Object> contactData = new HashMap<>();
            
            // Add names
            List<Map<String, Object>> names = new ArrayList<>();
            Map<String, Object> name = new HashMap<>();
            name.put("givenName", contactForm.getFirstName());
            name.put("familyName", contactForm.getLastName());
            
            // Add metadata for names
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("primary", true);
            metadata.put("source", createSource());
            name.put("metadata", metadata);
            
            names.add(name);
            contactData.put("names", names);
            
            // Add email addresses
            List<Map<String, Object>> emailAddresses = new ArrayList<>();
            if (contactForm.getEmail() != null && !contactForm.getEmail().isEmpty()) {
                Map<String, Object> email = new HashMap<>();
                email.put("value", contactForm.getEmail());
                
                // Add metadata for email
                Map<String, Object> emailMetadata = new HashMap<>();
                emailMetadata.put("primary", true);
                emailMetadata.put("source", createSource());
                email.put("metadata", emailMetadata);
                
                emailAddresses.add(email);
                contactData.put("emailAddresses", emailAddresses);
            }
            
            // Add phone numbers
            List<Map<String, Object>> phoneNumbers = new ArrayList<>();
            if (contactForm.getPhoneNumber() != null && !contactForm.getPhoneNumber().isEmpty()) {
                Map<String, Object> phone = new HashMap<>();
                phone.put("value", contactForm.getPhoneNumber());
                
                // Add metadata for phone
                Map<String, Object> phoneMetadata = new HashMap<>();
                phoneMetadata.put("primary", true);
                phoneMetadata.put("source", createSource());
                phone.put("metadata", phoneMetadata);
                
                phoneNumbers.add(phone);
                contactData.put("phoneNumbers", phoneNumbers);
            }
            
            // Construct the correct URL according to the API documentation
            // Format: PATCH https://people.googleapis.com/v1/{resourceName}:updateContact
            String updateUrl = PEOPLE_API_BASE_URL + "/" + resourceName + ":updateContact?updatePersonFields=names,emailAddresses,phoneNumbers";
            logger.info("Update URL: {}", updateUrl);
            
            // Log the request payload for debugging
            logger.info("Update payload: {}", new GsonFactory().toString(contactData));
            
            // Create the HTTP request with proper headers
            HttpEntity<Map<String, Object>> requestEntity = 
                new HttpEntity<>(contactData, headers);
            
            // Use Google's HTTP client directly for PATCH requests
            com.google.api.client.http.HttpRequestFactory requestFactory = 
                new com.google.api.client.http.javanet.NetHttpTransport().createRequestFactory();
            
            com.google.api.client.http.GenericUrl genericUrl = new com.google.api.client.http.GenericUrl(updateUrl);
            
            // Convert the Map to JSON string
            String jsonPayload = new GsonFactory().toPrettyString(contactData);
            
            // Create and execute the PATCH request
            com.google.api.client.http.HttpRequest request = requestFactory.buildPatchRequest(
                genericUrl, 
                new com.google.api.client.http.json.JsonHttpContent(
                    new GsonFactory(), 
                    contactData
                )
            );
            
            // Add authorization header
            request.getHeaders().setAuthorization("Bearer " + accessToken);
            
            // Execute the request
            com.google.api.client.http.HttpResponse httpResponse = request.execute();
            
            // Parse the response
            InputStream content = httpResponse.getContent();
            Map<String, Object> response = new GsonFactory().fromInputStream(content, Map.class);
            content.close();
            
            logger.info("Contact updated successfully: {}", response);
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to update contact: {}", e.getMessage(), e);
            throw new IOException("Failed to update contact: " + e.getMessage(), e);
        }
    }
    
    public void deleteContact(String resourceName) throws IOException {
        logger.info("Deleting contact with resource name: {}", resourceName);
        
        // Ensure resourceName is properly formatted
        if (!resourceName.startsWith("people/")) {
            resourceName = "people/" + resourceName;
        }
        
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory(request -> 
            request.setParser(jsonFactory.createJsonObjectParser())
                  .setHeaders(request.getHeaders().setAuthorization(
                      "Bearer " + googleCredentialService.getCredential().getAccessToken())));
        
        GenericUrl url = new GenericUrl(PEOPLE_API_BASE_URL + "/" + resourceName + ":deleteContact");
        
        HttpRequest request = requestFactory.buildDeleteRequest(url);
        
        try {
            HttpResponse response = request.execute();
            logger.info("Contact deleted successfully: {}", resourceName);
        } catch (GoogleJsonResponseException e) {
            logger.error("Error deleting contact: {}", e.getDetails().getMessage());
            throw e;
        }
    }
    
    private Map<String, Object> createContactPayload(ContactForm contactForm) {
        Map<String, Object> contactData = new HashMap<>();
        
        // Add names
        List<Map<String, Object>> names = new ArrayList<>();
        Map<String, Object> name = new HashMap<>();
        name.put("givenName", contactForm.getFirstName());
        name.put("familyName", contactForm.getLastName());
        
        Map<String, Object> metadata = new HashMap<>();
        Map<String, Object> source = new HashMap<>();
        source.put("type", "CONTACT");
        metadata.put("source", source);
        name.put("metadata", metadata);
        
        names.add(name);
        contactData.put("names", names);
        
        // Add email addresses if provided
        if (contactForm.getEmail() != null && !contactForm.getEmail().isEmpty()) {
            List<Map<String, Object>> emailAddresses = new ArrayList<>();
            Map<String, Object> email = new HashMap<>();
            email.put("value", contactForm.getEmail());
            email.put("type", "HOME");
            email.put("metadata", metadata);
            emailAddresses.add(email);
            contactData.put("emailAddresses", emailAddresses);
        }
        
        // Add phone numbers if provided
        if (contactForm.getPhoneNumber() != null && !contactForm.getPhoneNumber().isEmpty()) {
            List<Map<String, Object>> phoneNumbers = new ArrayList<>();
            Map<String, Object> phone = new HashMap<>();
            phone.put("value", contactForm.getPhoneNumber());
            phone.put("type", "MOBILE");
            phone.put("metadata", metadata);
            phoneNumbers.add(phone);
            contactData.put("phoneNumbers", phoneNumbers);
        }
        
        return contactData;
    }
    
    public ContactForm mapToContactForm(Map<String, Object> contact) {
        ContactForm form = new ContactForm();
        
        // Extract resource name
        String resourceName = (String) contact.get("resourceName");
        // Remove 'people/' prefix if it exists
        if (resourceName != null && resourceName.startsWith("people/")) {
            resourceName = resourceName.substring(7);
        }
        form.setResourceName(resourceName);
        
        // Extract name information
        if (contact.containsKey("names") && ((List<?>) contact.get("names")).size() > 0) {
            Map<String, Object> name = (Map<String, Object>) ((List<?>) contact.get("names")).get(0);
            form.setFirstName((String) name.get("givenName"));
            form.setLastName((String) name.get("familyName"));
        }
        
        // Extract email information
        if (contact.containsKey("emailAddresses") && ((List<?>) contact.get("emailAddresses")).size() > 0) {
            Map<String, Object> email = (Map<String, Object>) ((List<?>) contact.get("emailAddresses")).get(0);
            form.setEmail((String) email.get("value"));
        }
        
        // Extract phone information
        if (contact.containsKey("phoneNumbers") && ((List<?>) contact.get("phoneNumbers")).size() > 0) {
            Map<String, Object> phone = (Map<String, Object>) ((List<?>) contact.get("phoneNumbers")).get(0);
            form.setPhoneNumber((String) phone.get("value"));
        }
        
        return form;
    }
    
    private Map<String, Object> createSource() {
        Map<String, Object> source = new HashMap<>();
        source.put("type", "CONTACT");
        return source;
    }
}
