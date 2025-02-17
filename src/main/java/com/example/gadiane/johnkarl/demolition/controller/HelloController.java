package com.example.gadiane.johnkarl.demolition.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Hello, John!";
    }

    @GetMapping("/Secured")
    public String secured() {
        return "Hello, Secured";
    }
}
