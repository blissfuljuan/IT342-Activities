package com.moreno.oauth2login.Controller;

import org.springframework.web.bind.annotation.GetMapping;



public class HelloController {

    @GetMapping
    public String hello(){
        return "Hello, Social";
    }

    @GetMapping("/secured")
    public String secured(){
        return "Hello, Secured";
    }
}
