package com.largoza.googlecontactsapi_midterm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/secured")
    public String Logout() {
        return "secured";
    }

    @GetMapping("/home")
    public String home() {
        return "hello world!";
    }
}
