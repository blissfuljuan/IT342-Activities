package com.bacaltos.oauth2login.Controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    @GetMapping
    public String index() {
        return "Welcome to the landing page.";
    }
    @GetMapping("/user-info")
    public Map<String, Object> getUser (@AuthenticationPrincipal OAuth2User principal) {
        return principal.getAttributes();
    }
}
