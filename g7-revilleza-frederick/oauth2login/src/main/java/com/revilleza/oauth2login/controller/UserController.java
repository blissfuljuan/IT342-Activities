package com.revilleza.oauth2login.controller;

import com.revilleza.oauth2login.model.User;
import com.revilleza.oauth2login.repository.UserRepository;
import com.revilleza.oauth2login.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.Map;

@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String index(){
        return "<h1>Welcome, This is the landing page.</h1>";
    }

    @GetMapping("/user-info")
    public String getUser(@AuthenticationPrincipal OAuth2User oAuth2User, Model model){
        if( oAuth2User != null) {
            User user = Utils.OAuth2UserToUser(oAuth2User);
            User currentUser = userRepository.findByEmail(user.getEmail());
            if(currentUser==null)
                userRepository.save(user);
            model.addAttribute("user", user);
        } else {
            model.addAttribute("user", Collections.emptyMap());
        }
        return "user-info";
    }
}
