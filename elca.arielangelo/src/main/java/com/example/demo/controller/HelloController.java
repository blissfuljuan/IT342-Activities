package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
public class HelloController {
   
    @GetMapping("/")
    public String home() {
        return "Welcome to the application!";
    }
 
    @GetMapping("/secured")
    public String secured() {
        return "This is a secured endpoint!";
    }
}