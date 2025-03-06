package com.cabiling.oauth2login.Controller;

import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Controller
@RequestMapping("/contacts")
public class GoogleContactsController {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GoogleContactsController() {
        this.webClient = WebClient.create();
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping
    public String getGoogleContacts(
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
            OAuth2AuthenticationToken authentication,
            Model model) {

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String apiUrl = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses,phoneNumbers,organizations";

        String response = webClient.get()
                .uri(apiUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<Map<String, Object>> contactsList = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(response);

            if (rootNode.has("connections")) {
                for (JsonNode contactNode : rootNode.get("connections")) {
                    String resourceName = contactNode.has("resourceName") ? contactNode.get("resourceName").asText() : null;

                    String displayName = "Unknown";
                    if (contactNode.has("names")) {
                        JsonNode nameNode = contactNode.get("names").get(0);
                        displayName = nameNode.has("displayName") ? nameNode.get("displayName").asText() : "Unknown";
                    }

                    List<String> emails = new ArrayList<>();
                    if (contactNode.has("emailAddresses")) {
                        for (JsonNode emailNode : contactNode.get("emailAddresses")) {
                            emails.add(emailNode.get("value").asText());
                        }
                    }

                    List<String> phoneNumbers = new ArrayList<>();
                    if (contactNode.has("phoneNumbers")) {
                        for (JsonNode phoneNode : contactNode.get("phoneNumbers")) {
                            phoneNumbers.add(phoneNode.get("value").asText());
                        }
                    }

                    String jobTitle = "No Job Title";
                    String company = "No Company";
                    if (contactNode.has("organizations")) {
                        JsonNode orgNode = contactNode.get("organizations").get(0);
                        jobTitle = orgNode.has("title") ? orgNode.get("title").asText() : "No Job Title";
                        company = orgNode.has("name") ? orgNode.get("name").asText() : "No Company";
                    }

                    Map<String, Object> contactInfo = new HashMap<>();
                    contactInfo.put("resourceName", resourceName);
                    contactInfo.put("name", displayName);
                    contactInfo.put("emails", emails);
                    contactInfo.put("phones", phoneNumbers);
                    contactInfo.put("jobTitle", jobTitle);
                    contactInfo.put("company", company);
                    contactsList.add(contactInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("contacts", contactsList);
        return "contacts";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<String> addContact(@RequestBody Map<String, Object> contactData,
                                             @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String apiUrl = "https://people.googleapis.com/v1/people:createContact";

        Map<String, Object> requestBody = new HashMap<>();
        if (contactData.get("name") != null) {
            requestBody.put("names", List.of(Map.of("givenName", contactData.get("name"))));
        }
        if (contactData.get("email") != null) {
            requestBody.put("emailAddresses", List.of(Map.of("value", contactData.get("email"))));
        }
        if (contactData.get("phone") != null) {
            requestBody.put("phoneNumbers", List.of(Map.of("value", contactData.get("phone"))));
        }
        if (contactData.get("company") != null || contactData.get("jobTitle") != null) {
            Map<String, String> orgData = new HashMap<>();
            if (contactData.get("company") != null) orgData.put("name", (String) contactData.get("company"));
            if (contactData.get("jobTitle") != null) orgData.put("title", (String) contactData.get("jobTitle"));
            requestBody.put("organizations", List.of(orgData));
        }

        String response = webClient.post()
                .uri(apiUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/edit")
    @ResponseBody
    public ResponseEntity<String> editContact(@RequestBody Map<String, Object> contactData,
                                            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {
        try {
            String accessToken = authorizedClient.getAccessToken().getTokenValue();
            String resourceName = (String) contactData.get("resourceName");
            
            if (resourceName == null || !resourceName.startsWith("people/")) {
                return ResponseEntity.badRequest().body("Invalid resourceName");
            }

            String apiUrl = "https://people.googleapis.com/v1/" + resourceName + 
                           ":updateContact?updatePersonFields=names,emailAddresses,phoneNumbers,organizations" +
                           "&personFields=names,emailAddresses,phoneNumbers,organizations,metadata";

            // Get metadata first
            String getResponse = webClient.get()
                    .uri("https://people.googleapis.com/v1/" + resourceName + "?personFields=metadata")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode existingContact = objectMapper.readTree(getResponse);
            JsonNode sources = existingContact.get("metadata").get("sources");
            String etag = existingContact.get("etag") != null ? existingContact.get("etag").asText() : null;

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sources", objectMapper.convertValue(sources, List.class));
            requestBody.put("metadata", metadata);
            
            if (etag != null) {
                requestBody.put("etag", etag);
            }

            if (contactData.get("name") != null) {
                requestBody.put("names", List.of(Map.of("givenName", contactData.get("name"))));
            }
            if (contactData.get("email") != null) {
                requestBody.put("emailAddresses", List.of(Map.of("value", contactData.get("email"))));
            }
            if (contactData.get("phone") != null) {
                requestBody.put("phoneNumbers", List.of(Map.of("value", contactData.get("phone"))));
            }
            if (contactData.get("company") != null || contactData.get("jobTitle") != null) {
                Map<String, String> orgData = new HashMap<>();
                if (contactData.get("company") != null) orgData.put("name", (String) contactData.get("company"));
                if (contactData.get("jobTitle") != null) orgData.put("title", (String) contactData.get("jobTitle"));
                requestBody.put("organizations", List.of(orgData));
            }

            String response = webClient.patch()
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> 
                        clientResponse.bodyToMono(String.class)
                            .map(body -> {
                                try {
                                    JsonNode errorNode = objectMapper.readTree(body);
                                    String reason = errorNode.path("error").path("message").asText();
                                    return new RuntimeException("Failed to update contact: " + reason);
                                } catch (Exception e) {
                                    return new RuntimeException("Failed to update contact: " + body);
                                }
                            }))
                    .bodyToMono(String.class)
                    .block();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error updating contact: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @ResponseBody
    public ResponseEntity<String> deleteContact(@RequestParam String resourceName,
                                                @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String apiUrl = "https://people.googleapis.com/v1/" + resourceName + ":deleteContact";

        String response = webClient.delete()
                .uri(apiUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return ResponseEntity.ok(response != null ? response : "{}");
    }
}