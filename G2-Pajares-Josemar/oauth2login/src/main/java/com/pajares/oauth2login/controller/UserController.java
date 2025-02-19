package com.pajares.oauth2login.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("")


@RestController
public class UserController {

    @GetMapping
    public String getMessage(){
        return "<h1>Welcome Pajares, This is the landing page";
    }

    @GetMapping("/user-info")
    public Map<String, Object> getUserProfile(@AuthenticationPrincipal OAuth2User oAuth2User){
        return oAuth2User.getAttributes();
    }
    @GetMapping("/secured")
    public String securedEndpoint(){
        return "<h1> this is a secured endpoint</h1>";
    }

}
