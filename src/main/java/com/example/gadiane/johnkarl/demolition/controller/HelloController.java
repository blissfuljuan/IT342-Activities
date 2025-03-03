package com.example.gadiane.johnkarl.demolition.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/secured")
    public String secured() {
        return "secured";
    }
}