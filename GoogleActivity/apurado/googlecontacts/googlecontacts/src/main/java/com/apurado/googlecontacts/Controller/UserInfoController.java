package com.apurado.googlecontacts.Controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserInfoController {

    @GetMapping("/userinfo")
    public String displayUserInfo(@AuthenticationPrincipal OAuth2User oAuth2User, Model model) {
        model.addAttribute("userInfo", oAuth2User.getAttributes());
        return "user-info"; // Make sure there's a user-info.html template in your templates folder
    }
}
