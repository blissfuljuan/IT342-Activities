package com.ligan.oauth2login.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserContoller {
    @GetMapping
    public String index(){
        return "<h1>Hello, This is my Landing Page";
    }

    @GetMapping("user-info")
    public Map<String,Object> getUser(@AuthenticationPrincipal OAuth2User oAuth2User){
        return oAuth2User.getAttributes();
    }

    @GetMapping("/secured")
    public String secured(){
        return "This is secured page";
    }
}

