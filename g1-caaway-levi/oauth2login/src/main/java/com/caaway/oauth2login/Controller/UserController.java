package com.caaway.oauth2login.Controller;

import java.util.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController{

    @GetMapping
    public String index() {
        return "<h1>Welcome, This is the landing page.</h1> ";
    }

    @GetMapping("user-info")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User oAuth2User){
        return oAuth2User.getAttributes();
    }
}
