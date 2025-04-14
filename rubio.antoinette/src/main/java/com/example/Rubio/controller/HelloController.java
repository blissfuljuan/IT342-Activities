package com.example.Rubio.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;



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
