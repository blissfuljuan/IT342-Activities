package com.paras.googlecontactintegration.controller;

import com.google.api.services.people.v1.model.Person;
import com.paras.googlecontactintegration.service.GooglePeopleService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ui.Model;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/landingpage")
    public String index() {
        return "<h1>Hello, This is the landing page.";
    }

    @GetMapping("/user-info")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User oAuth2User) {
        return oAuth2User.getAttributes();
    }

}
