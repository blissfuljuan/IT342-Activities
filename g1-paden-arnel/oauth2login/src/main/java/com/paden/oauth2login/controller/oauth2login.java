package com.paden.oauth2login.controller;
 
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class oauth2login {
    @GetMapping
    public String index(){
        return "Welcome, this is the landing page";
    }

   @GetMapping("user-info")
    public Map<String,Object> getUser(@AuthenticationPrincipal OAuth2User OAuth2User){
        return OAuth2User.getAttributes();
    }
}


