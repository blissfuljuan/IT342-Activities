package com.vincent.oauth2login.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("")
public class UserController {
    @GetMapping("")
    private String HelloMessage(){
        return "<h1>Nudagan nagyud</h1>";
    }

    @GetMapping("/user-info")
    public Map<String, Object> getUserProfile(@AuthenticationPrincipal OAuth2User user){
        return user.getAttributes();
    }

    @GetMapping("/secured")
    public String securedEndpoint(){
        return "<h1>This is a secured endpoint</h1>";
    }
}
