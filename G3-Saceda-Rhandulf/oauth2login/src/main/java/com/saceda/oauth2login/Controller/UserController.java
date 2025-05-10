package com.saceda.oauth2login.Controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import  org.springframework.web.bind.annotation.RestController;
import  org.springframework.security.oauth2.core.user.OAuth2User;
import  org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Map;
import java.util.Objects;

@RestController
public class UserController {
    @GetMapping
    public String test(){
        return "Hello, this is landing page";
    }
    @GetMapping("/user-info")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User oAuth2User){
        return oAuth2User.getAttributes();
    }
    @GetMapping("/secured")
    public String secured(){
        return "Hello, this is landing page";
    }
}
