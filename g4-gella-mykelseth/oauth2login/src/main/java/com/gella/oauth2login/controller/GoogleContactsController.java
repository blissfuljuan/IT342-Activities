package com.gella.oauth2login.controller;

import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/contacts1")
public class GoogleContactsController {

    /*@GetMapping
    public String getContacts(@RequestParam String accessToken) {
        String url = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        
        return response.getBody();
    }*/
}
