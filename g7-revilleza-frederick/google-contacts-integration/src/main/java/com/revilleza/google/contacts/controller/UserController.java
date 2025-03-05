package com.revilleza.google.contacts.controller;

import com.revilleza.google.contacts.utils.Utils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;

@Controller
public class UserController {

    @GetMapping
    public String index(){
        return "index";
    }

    @GetMapping("/user-info")
    public String getUser(@AuthenticationPrincipal OAuth2User oAuth2User, Model model){
        System.out.println("================ I was here in user info");
        if( oAuth2User != null) {
            model.addAttribute("user", Utils.OAuth2UserToUser(oAuth2User));
        } else {
            model.addAttribute("user", Collections.emptyMap());
        }
        return "user-info";
    }
}
