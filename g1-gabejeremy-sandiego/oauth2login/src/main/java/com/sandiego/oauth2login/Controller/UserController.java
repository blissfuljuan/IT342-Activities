package com.sandiego.oauth2login.Controller;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RestController
public class UserController {

    @GetMapping
    public String hello() {
        return "<h1>Welcome, this is the landing page.</h1>";
    }

    @GetMapping("/user-info") 
    public Map<String, Object> userInfo(@AuthenticationPrincipal OAuth2User oAuth2User) {
        return oAuth2User.getAttributes();
    }
}
