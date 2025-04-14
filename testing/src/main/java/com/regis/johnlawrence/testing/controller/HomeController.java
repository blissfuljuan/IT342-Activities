package com.regis.johnlawrence.testing.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class HomeController {
    @GetMapping("/")
    public String home() {
        return "home"; // Loads home.html from templates
    }
}
