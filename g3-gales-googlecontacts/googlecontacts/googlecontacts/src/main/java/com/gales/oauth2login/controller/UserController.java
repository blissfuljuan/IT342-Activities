package com.gales.oauth2login.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gales.oauth2login.model.Contact;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public UserController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping
    public String index() {
        return "<h1>Hello, This is the landing page.</h1>";
    }

    @GetMapping("/user-info")
    public String getUserInfo(Model model, @AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return "redirect:/login";
        }

        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        String email = oAuth2User.getAttribute("email");

        String phone = "N/A";
        Object phoneNumbers = oAuth2User.getAttribute("phoneNumbers");
        if (phoneNumbers instanceof List && !((List<?>) phoneNumbers).isEmpty()) {
            Object firstPhoneEntry = ((List<?>) phoneNumbers).get(0);
            if (firstPhoneEntry instanceof Map) {
                phone = ((Map<String, String>) firstPhoneEntry).getOrDefault("value", "N/A");
            }
        }

        model.addAttribute("user", Map.of(
                "name", name != null ? name : "Unknown",
                "picture", picture != null ? picture : "/default-profile.png",
                "email", email != null ? email : "No email available",
                "phone", phone
        ));

        return "user-info";
    }

    @GetMapping("/google-contacts")
    public String getGoogleContacts(Model model, OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), authentication.getName());

        String accessToken = client.getAccessToken().getTokenValue();

        String url = UriComponentsBuilder.fromHttpUrl("https://people.googleapis.com/v1/people/me/connections")
                .queryParam("personFields", "names,emailAddresses,phoneNumbers")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        List<Map<String, Object>> contacts = (List<Map<String, Object>>) response.getBody().get("connections");

        model.addAttribute("contacts", contacts);
        return "contacts";
    }

    @GetMapping("/add-contact")
    public String addContactPage() {
        return "add-contact";
    }

    @PostMapping("/add-contact")
    public String addContact(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phone,
            OAuth2AuthenticationToken authentication) {

        try {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    authentication.getAuthorizedClientRegistrationId(), authentication.getName());

            String accessToken = client.getAccessToken().getTokenValue();

            String url = "https://people.googleapis.com/v1/people:createContact";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = "{\n" +
                    "  \"names\": [{\"givenName\": \"" + firstName + "\", \"familyName\": \"" + lastName + "\"}],\n" +
                    "  \"emailAddresses\": [{\"value\": \"" + email + "\"}],\n" +
                    "  \"phoneNumbers\": [{\"value\": \"" + phone + "\"}]\n" +
                    "}";

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            return "redirect:/google-contacts";
        } catch (Exception e) {
            return "redirect:/add-contact?error=Could not add contact";
        }
    }

    @PostMapping("/delete-contact")
    public String deleteContact(@RequestParam String resourceName, OAuth2AuthenticationToken authentication) {
        try {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    authentication.getAuthorizedClientRegistrationId(), authentication.getName());

            String accessToken = client.getAccessToken().getTokenValue();

            // Correct API URL with :deleteContact at the end
            String url = "https://people.googleapis.com/v1/" + resourceName + ":deleteContact";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

            return "redirect:/google-contacts";
        } catch (Exception e) {
            e.printStackTrace(); // Log error for debugging
            return "redirect:/google-contacts?error=Could not delete contact";
        }
    }

    @GetMapping("/edit-contact")
    public String editContactForm(@RequestParam String resourceName, Model model, OAuth2AuthenticationToken authentication) {
        try {
            // 🔹 Fetch OAuth token
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    authentication.getAuthorizedClientRegistrationId(), authentication.getName());
            String accessToken = client.getAccessToken().getTokenValue();

            // 🔹 Fetch contact details from Google API
            String getUrl = "https://people.googleapis.com/v1/" + resourceName + "?personFields=names,emailAddresses,phoneNumbers";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> getRequest = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(getUrl, HttpMethod.GET, getRequest, Map.class);

            Map<String, Object> contactData = response.getBody();
            if (contactData == null) {
                return "redirect:/google-contacts?error=Contact not found";
            }

            // 🔹 Ensure fields exist, or provide default empty values
            List<Map<String, Object>> names = (List<Map<String, Object>>) contactData.getOrDefault("names", new ArrayList<>());
            List<Map<String, Object>> emailAddresses = (List<Map<String, Object>>) contactData.getOrDefault("emailAddresses", new ArrayList<>());
            List<Map<String, Object>> phoneNumbers = (List<Map<String, Object>>) contactData.getOrDefault("phoneNumbers", new ArrayList<>());

            // Add to model
            model.addAttribute("contact", Map.of(
                    "names", !names.isEmpty() ? names : List.of(Map.of("givenName", "", "familyName", "")),
                    "emailAddresses", !emailAddresses.isEmpty() ? emailAddresses : List.of(Map.of("value", "")),
                    "phoneNumbers", !phoneNumbers.isEmpty() ? phoneNumbers : List.of(Map.of("value", ""))
            ));
            model.addAttribute("resourceName", resourceName);
            return "edit-contact";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/google-contacts?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    @PatchMapping("/edit-contact")
    public ResponseEntity<String> updateContact(
            @RequestBody Map<String, String> requestData,
            OAuth2AuthenticationToken authentication) {
        try {
            String resourceName = requestData.get("resourceName");
            String firstName = requestData.getOrDefault("firstName", "").trim();
            String lastName = requestData.getOrDefault("lastName", "").trim();
            String email = requestData.getOrDefault("email", "").trim();
            String phone = requestData.getOrDefault("phone", "").trim();

            // 🔹 Fetch OAuth Token
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    authentication.getAuthorizedClientRegistrationId(), authentication.getName());
            String accessToken = client.getAccessToken().getTokenValue();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 🔹 Fetch Contact to get etag
            String getUrl = "https://people.googleapis.com/v1/" + resourceName + "?personFields=metadata";
            HttpEntity<Void> getRequest = new HttpEntity<>(headers);
            ResponseEntity<Map> getResponse = restTemplate.exchange(getUrl, HttpMethod.GET, getRequest, Map.class);

            Map<String, Object> contactData = getResponse.getBody();
            if (contactData == null || !contactData.containsKey("etag")) {
                return ResponseEntity.badRequest().body("Error: Contact etag is missing.");
            }

            String etag = (String) contactData.get("etag");

            // 🔹 Construct Request Body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("etag", etag);

            List<Map<String, String>> names = new ArrayList<>();
            if (!firstName.isEmpty() || !lastName.isEmpty()) {
                names.add(Map.of(
                        "givenName", firstName.isEmpty() ? "" : firstName,
                        "familyName", lastName.isEmpty() ? "" : lastName
                ));
            }

            List<Map<String, String>> emails = new ArrayList<>();
            if (!email.isEmpty()) {
                emails.add(Map.of("value", email));
            }

            List<Map<String, String>> phones = new ArrayList<>();
            if (!phone.isEmpty()) {
                phones.add(Map.of("value", phone));
            }

            if (!names.isEmpty()) requestBody.put("names", names);
            if (!emails.isEmpty()) requestBody.put("emailAddresses", emails);
            if (!phones.isEmpty()) requestBody.put("phoneNumbers", phones);

            // 🔹 Send "PATCH" Request (Using POST Override)
            String patchUrl = "https://people.googleapis.com/v1/" + resourceName + ":updateContact?updatePersonFields=names,emailAddresses,phoneNumbers";
            headers.set("X-HTTP-Method-Override", "PATCH");
            HttpEntity<Map<String, Object>> patchRequest = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(patchUrl, HttpMethod.POST, patchRequest, String.class);

            return ResponseEntity.ok("Contact updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/secured")
    public String secured() {
        return "<h1>Hello, This is a secured page.</h1>";
    }
}
