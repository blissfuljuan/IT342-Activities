package com.miparanummidterm.Midterm.Miparanum.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class HelloController {

    public HelloController() {

    }

    @GetMapping
    public String hello() {
        return "Hello, Social";
    }

    @GetMapping({"/secured"})
    public String secured() {
        return "Hello, Secured";
    }
}
