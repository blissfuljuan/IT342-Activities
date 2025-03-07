package com.example.gadiane.johnkarl.demolition.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error";
    }
}
