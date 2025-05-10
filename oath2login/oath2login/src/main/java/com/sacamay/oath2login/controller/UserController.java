package com.sacamay.oath2login.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
@RestController
public class UserController {

    @GetMapping("/")
    public String index(){
        return "<h1>Hello, this is a landing page</h1>";
    }

    @GetMapping("/user-info")
    public ResponseEntity<String> getUser(@AuthenticationPrincipal OAuth2User oAuth2User) throws Exception {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> filteredData = new HashMap<>();

        filteredData.put("sub", attributes.get("sub")); // Google unique user ID
        filteredData.put("name", attributes.get("name"));
        filteredData.put("given_name", attributes.get("given_name"));
        filteredData.put("family_name", attributes.get("family_name"));
        filteredData.put("picture", attributes.get("picture")); // Profile picture
        filteredData.put("email", attributes.get("email"));
        filteredData.put("email_verified", attributes.get("email_verified"));

        // Use Jackson's ObjectMapper to format the JSON response
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String formattedJson = objectMapper.writeValueAsString(filteredData);

        return ResponseEntity.ok(formattedJson);
    }



}
