package com.lacanglacang.google.oauth2googlecontacts.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
public class ContactController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public ContactController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/")
    @ResponseBody
    public Map<String, String> index() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to the API Landing Page");
        return response;
    }

    @GetMapping("/user-info")
    public String getUser(@AuthenticationPrincipal OAuth2User oAuth2User, Map<String, Object> model) {
        if (oAuth2User != null) {
            model.put("user", oAuth2User.getAttributes());
        } else {
            model.put("error", "User not authenticated");
        }
        return "profile"; // Returns profile.html
    }

    @GetMapping("/contacts")
    @ResponseBody
    public Map<String, Object> getGoogleContacts(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName());

        String accessToken = client.getAccessToken().getTokenValue();

        // Google People API URL with phone numbers included
        String url = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses,phoneNumbers,photos";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url + "&access_token=" + accessToken,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        return response.getBody();
    }

    @GetMapping("/contacts-page")
    public String contactsPage() {
        return "contacts"; // This corresponds to contacts.html in /templates
    }
}
