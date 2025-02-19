package com.danrave.oauth2login.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
public class UserControler {

    @GetMapping
    public String index(){
        return "<h1>Welcome, This is the landing page";
    }

    @GetMapping("/user-info")
    public Map<String, Object> getUserProfile(@AuthenticationPrincipal OAuth2User user){
        return user.getAttributes();
    }

    @GetMapping("/secured")
    public String securedEndPoint(){
        return "<h1>This is secured endpoint</h1>";
    }
}
