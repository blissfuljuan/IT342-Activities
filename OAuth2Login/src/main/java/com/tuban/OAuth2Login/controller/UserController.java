package com.tuban.OAuth2Login.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    @GetMapping
    public String index(){
        return "<h1>I love you sir fred</h1>";
    }
    @GetMapping("/user-info")
    public Map<String, Object> getUserProfile(@AuthenticationPrincipal OAuth2User oAuth2User) {
        return oAuth2User.getAttributes();
    }
    @GetMapping("/secured")
    public String securedEndpoint(){
        return "<h1>This is a Secured Endpoint</h1>";
    }

}
