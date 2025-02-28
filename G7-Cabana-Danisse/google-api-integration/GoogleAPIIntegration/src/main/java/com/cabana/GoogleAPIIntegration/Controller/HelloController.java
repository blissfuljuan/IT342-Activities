package com.cabana.GoogleAPIIntegration.Controller;
import org.springframework.web.bind.annotation.GetMapping;

public class HelloController {
    @GetMapping
    public String hello() {
        return "Hello World!";
    }

    @GetMapping ("/secured")
    public String secured() {
        return "Hello, Secured";
    }
}