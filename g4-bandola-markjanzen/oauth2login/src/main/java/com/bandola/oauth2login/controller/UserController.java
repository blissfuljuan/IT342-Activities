package com.bandola.oauth2login.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import  java.util.Collections;
import  java.util.Map;

@RestController
public class UserController {
    @GetMapping
    public String index(){
        return "hello this is 5 mins of code";
    }

    @GetMapping("/user-info")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal OAuth2User principal){
        return principal.getAttributes();
    }

    @GetMapping("/secured")
    public String secured(){
        return "This is a secured endpoint";
    }
}
