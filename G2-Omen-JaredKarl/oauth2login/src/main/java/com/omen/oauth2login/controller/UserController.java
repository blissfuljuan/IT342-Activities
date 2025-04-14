package com.omen.oauth2login.controller;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;


@RestController
public class UserController {
    @GetMapping
    public String index() {
        return "<h1>Welcome, This is my Landing Page</h1>";
    }


    @GetMapping("/user-info")
    public Map<String, Object> getUserProfile(@AuthenticationPrincipal OAuth2User OAuth2User) {
        return OAuth2User.getAttributes();
    }

    @GetMapping("/secured")
    public String securedEndPoint() {
        return "<h1>Welcome, This is a secured page</h1>";
    }
}
