package com.regis.johnlawrence.testing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/secured")
    public String secured() {
        return "Hello, secured";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, User";
    }
}
