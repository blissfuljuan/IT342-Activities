package com.snaiel.oauth2login.Controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;


@RestController
public class UserController {

    @GetMapping
    public String index(){
        return "<h1>Hello, This is the landing page.";
    }
    @GetMapping("/user-info")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User oAuth2User){
        return oAuth2User.getAttributes();
    }

    @GetMapping("/secured")
    public String secured(){
        return "<h1>Hello, This is a secured page.</h1>";
    }



}
