package com.duque.oauth2login.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("")
public class HelloController {

    @GetMapping("/")
    public String Hello(){
        return "Hi Social";
    }
    @GetMapping("/secured")
    public String secured(){
        return "Hi Secured";
    }

}
