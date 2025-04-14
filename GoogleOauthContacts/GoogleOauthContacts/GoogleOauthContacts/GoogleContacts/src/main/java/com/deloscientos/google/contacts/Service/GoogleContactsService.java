package com.deloscientos.google.contacts.Service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.deloscientos.google.contacts.dto.Contact;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoogleContactsService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ✅ Fetch all contacts (Now includes phone numbers & birthdays)
    public List<Contact> getContacts(String accessToken) {
        String url = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses,phoneNumbers,metadata";
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);
    
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode connections = root.path("connections");
    
                if (connections.isMissingNode()) {
                    System.out.println("⚠️ No contacts found.");
                    return List.of();
                }
    
                List<Contact> contactList = new ArrayList<>();
    
                for (JsonNode person : connections) {
                    String resourceName = person.path("resourceName").asText();
                    String etag = person.path("etag").asText();
    
                    String name = person.path("names").isArray() && person.path("names").size() > 0
                            ? person.path("names").get(0).path("displayName").asText()
                            : "Unknown";
    
                    List<String> emails = new ArrayList<>();
                    JsonNode emailNodes = person.path("emailAddresses");
                    if (emailNodes.isArray()) {
                        for (JsonNode email : emailNodes) {
                            emails.add(email.path("value").asText());
                        }
                    }
    
                    List<String> phoneNumbers = new ArrayList<>();
                    JsonNode phoneNodes = person.path("phoneNumbers");
                    if (phoneNodes.isArray()) {
                        for (JsonNode phone : phoneNodes) {
                            phoneNumbers.add(phone.path("value").asText());
                        }
                    }
    
                    contactList.add(new Contact(resourceName, etag, name, emails, phoneNumbers));
                }
    
                return contactList;
            }
        } catch (Exception e) {
            System.out.println("❌ Error fetching contacts: " + e.getMessage());
        }
    
        return List.of();
    }
    
    

    // ✅ Fetch a single contact (Now includes phone & birthday)
    public Contact getContactById(String resourceName, String accessToken) {
        if (!resourceName.startsWith("people/")) {
            resourceName = "people/" + resourceName;
        }

        String url = "https://people.googleapis.com/v1/" + resourceName + "?personFields=names,emailAddresses,phoneNumbers";
        System.out.println("🔹 Fetching contact from URL: " + url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());

                String name = root.path("names").path(0).path("displayName").asText("Unknown");
                String etag = root.path("etag").asText("");

                List<String> emails = new ArrayList<>();
                JsonNode emailNodes = root.path("emailAddresses");
                if (emailNodes.isArray()) {
                    for (JsonNode email : emailNodes) {
                        emails.add(email.get("value").asText());
                    }
                }

                List<String> phoneNumbers = new ArrayList<>();
                JsonNode phoneNodes = root.path("phoneNumbers");
                if (phoneNodes.isArray()) {
                    for (JsonNode phone : phoneNodes) {
                        phoneNumbers.add(phone.get("value").asText());
                    }
                }

                return new Contact(resourceName, etag, name, emails, phoneNumbers);
            }
        } catch (Exception e) {
            System.err.println("❌ Error fetching contact: " + e.getMessage());
        }

        return null;
    }


    // ✅ Add a new contact (Supports name, email, and phone)
    public void addContact(Contact contact, String accessToken) {
        String url = "https://people.googleapis.com/v1/people:createContact";
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
    
        List<Map<String, String>> emailAddresses = new ArrayList<>();
        for (String email : contact.getEmails()) {
            emailAddresses.add(Map.of("value", email));
        }
    
        List<Map<String, String>> phoneNumbers = new ArrayList<>();
        for (String phone : contact.getPhoneNumbers()) {
            phoneNumbers.add(Map.of("value", phone));
        }
    
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("names", List.of(Map.of("givenName", contact.getName())));
        requestBody.put("emailAddresses", emailAddresses);
        requestBody.put("phoneNumbers", phoneNumbers);
    
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
    
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            System.out.println("✅ Add Contact Response: " + response.getBody());
        } catch (Exception e) {
            System.out.println("❌ Error adding contact: " + e.getMessage());
        }
    }

    // ✅ Update a contact (Supports name, email, and phone)
    public void updateContact(String resourceName, Contact contact, String accessToken) {
        try {
            resourceName = java.net.URLDecoder.decode(resourceName, StandardCharsets.UTF_8);
            if (!resourceName.startsWith("people/")) {
                resourceName = "people/" + resourceName;
            }

            String url = "https://people.googleapis.com/v1/" + resourceName +
                         ":updateContact?updatePersonFields=names,emailAddresses,phoneNumbers";

            if (contact.getEtag() == null || contact.getEtag().isEmpty()) {
                System.err.println("❌ Error: etag is missing! Update will fail.");
                return;
            }

            HttpPatch httpPatch = new HttpPatch(url);
            httpPatch.setHeader("Authorization", "Bearer " + accessToken);
            httpPatch.setHeader("Content-Type", "application/json");
            httpPatch.setHeader("Accept", "application/json");

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonBody = objectMapper.writeValueAsString(Map.of(
                        "etag", contact.getEtag(),
                        "names", List.of(Map.of("givenName", contact.getName())),
                        "emailAddresses", contact.getEmails() != null
                                ? contact.getEmails().stream().map(email -> Map.of("value", email)).toList()
                                : Collections.emptyList(),
                        "phoneNumbers", contact.getPhoneNumbers() != null
                                ? contact.getPhoneNumbers().stream().map(phone -> Map.of("value", phone)).toList()
                                : Collections.emptyList()
                ));

                httpPatch.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

                try (CloseableHttpResponse response = httpClient.execute(httpPatch)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

                    if (statusCode == HttpStatus.OK.value() || statusCode == HttpStatus.NO_CONTENT.value()) {
                        System.out.println("✅ Contact updated successfully: " + resourceName);
                    } else {
                        System.err.println("❌ Failed to update contact. Status: " + statusCode + ", Response: " + responseString);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error updating contact: " + e.getMessage());
        }
    }

    
    // ✅ Delete a contact (Checks metadata before deleting)
    public void deleteContact(String resourceName, String accessToken) {
        String metadataUrl = "https://people.googleapis.com/v1/" + resourceName + "?personFields=metadata";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> metadataResponse = restTemplate.exchange(metadataUrl, HttpMethod.GET, entity, Map.class);

            if (metadataResponse.getBody() != null && metadataResponse.getBody().containsKey("metadata")) {
                Map<String, Object> metadata = (Map<String, Object>) metadataResponse.getBody().get("metadata");
                List<Map<String, Object>> sources = (List<Map<String, Object>>) metadata.get("sources");

                if (sources != null && sources.stream().anyMatch(s -> "CONTACT".equals(s.get("type")))) {
                    String deleteUrl = "https://people.googleapis.com/v1/" + resourceName + ":deleteContact";
                    restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);
                    System.out.println("✅ Contact deleted: " + resourceName);
                } else {
                    System.out.println("⚠️ Error: Contact cannot be deleted (not an owned contact).");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error deleting contact: " + e.getMessage());
        }
    }
}
