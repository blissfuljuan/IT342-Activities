package com.batucan.oauth2login.oauth2loginController;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class OAuth2LoginApplicationController {
    
    @GetMapping
    public String index(){
        return "<h1>Mabuhay Welcome to siargao, Welcome to mobile legends.</h1>";
    }
    

    @GetMapping("user-info")
    public Map<String,Object> getUser(@AuthenticationPrincipal OAuth2User OAuth2User){
        return OAuth2User.getAttributes();
    }
}
