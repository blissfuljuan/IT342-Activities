package com.matunog.oauth2login.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.Map;



@RestController
public class UserController {
    
    @GetMapping
    public String index() {
        return "<h1>WELCOME! This is the landing page.";
    }

    @GetMapping("user-info")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User OAuth2User) {
        return OAuth2User.getAttributes();
    }
    
    
}
