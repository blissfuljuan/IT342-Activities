package com.tampus.oauth2login.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class userController {

    @GetMapping
    public String Hello(){
        return "hello Lord";
    }

    @GetMapping("/user-info")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User oAuth2User){
        return oAuth2User.getAttributes();
    }

    @GetMapping("/secured")
    public String Secured(){
        return "this is secured";
    }
}
