package com.catulong.oauth2login.Controller;

import java.util.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;



@RestController
public class UserController {


    @GetMapping("/")
    public String landingPage() {
        return "<h1> Welcome Juan, this is the landing page";
    }

    @GetMapping("user-info")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User oAuth2User){
        return oAuth2User.getAttributes();
    }

}