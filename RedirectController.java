package com.example.demo;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class RedirectController {

    @GetMapping
    public String home(@AuthenticationPrincipal OidcUser user) {
        if (user != null) {
            return "Welcome. Hello " + user.getFullName();
        }
        return "Hello, Michael C. Gelera! :)";
    }
}