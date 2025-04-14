package com.bacaltos.oauth2login.Controller;

import org.springframework.web.bind.annotation.GetMapping;

public class HelloController {
    @GetMapping
    public String hello() {
        return "Hello, Socialboi";
    }

    @GetMapping ("/secured")
    public String secured() {
        return "Hello, Secured";
    }
}
