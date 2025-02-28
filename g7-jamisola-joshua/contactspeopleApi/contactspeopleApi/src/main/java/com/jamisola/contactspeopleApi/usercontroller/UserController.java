package com.jamisola.contactspeopleApi.usercontroller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public UserController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }


    @GetMapping("/")
    public String index() {
        return "index"; // Create index.html
    }

    @GetMapping("/user-profile")
    public String getUserProfile(Model model, @AuthenticationPrincipal OAuth2User user) {
        if (user != null) {
            model.addAttribute("name", user.getAttribute("name"));
            model.addAttribute("email", user.getAttribute("email"));
            model.addAttribute("picture", user.getAttribute("picture"));
        }
        return "user-profile"; // Ensure user-profile.html exists
    }

    @GetMapping("/contacts")
    public String getContacts(Model model, OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        String accessToken = client.getAccessToken().getTokenValue();

        // Call Google People API to get contacts
        String url = "https://people.googleapis.com/v1/people/me/connections"
                + "?personFields=names,emailAddresses,phoneNumbers"
                + "&access_token=" + accessToken;

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        List<Map<String, String>> contactsList = new ArrayList<>();
        if (response != null && response.containsKey("connections")) {
            List<Map<String, Object>> connections = (List<Map<String, Object>>) response.get("connections");

            for (Map<String, Object> person : connections) {
                String name = person.containsKey("names") ?
                        ((List<Map<String, Object>>) person.get("names")).get(0).get("displayName").toString() :
                        "Unknown";

                String email = person.containsKey("emailAddresses") ?
                        ((List<Map<String, Object>>) person.get("emailAddresses")).get(0).get("value").toString() :
                        "N/A";

                String phone = person.containsKey("phoneNumbers") ?
                        ((List<Map<String, Object>>) person.get("phoneNumbers")).get(0).get("value").toString() :
                        "N/A";

                contactsList.add(Map.of("name", name, "email", email, "phone", phone));
            }
        }

        model.addAttribute("contacts", contactsList);
        return "contacts"; // Returns contacts.html
    }
}
