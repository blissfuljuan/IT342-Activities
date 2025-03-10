package com.revilleza.google.contacts.utils;

import com.revilleza.google.contacts.model.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class Utils {
    public static User OAuth2UserToUser(OAuth2User oAuth2User){
        User user = new User();
        user.setDisplayName(oAuth2User.getAttribute("name"));
        user.setFirstName(oAuth2User.getAttribute("given_name"));
        user.setLastName(oAuth2User.getAttribute("family_name"));
        user.setEmail(oAuth2User.getAttribute("email"));
        return user;
    }
}
