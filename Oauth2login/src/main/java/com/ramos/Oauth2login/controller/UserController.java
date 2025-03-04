package com.ramos.Oauth2login.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @GetMapping
    public String index(){
        return "<h1>Welcome, This is the Landing Page</h1>";
    }
}
