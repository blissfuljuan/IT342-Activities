package com.miparanummidterm.Midterm.Miparanum.controller;

import com.miparanummidterm.Midterm.Miparanum.service.ContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class UserController {
    public UserController() {
    }

    @GetMapping
    public String index() {
        return "Welcome, this is my landing page";
    }

    @GetMapping({"/user-info"})
    public String getUser(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            model.addAttribute("userAttributes", principal.getAttributes());
        }
        return "user-info"; // This should match the Thymeleaf template name
    }

}