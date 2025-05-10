package com.nator.oauth2login.controller;

import com.nator.oauth2login.model.Contact;
import com.nator.oauth2login.service.GooglePeopleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final GooglePeopleService googlePeopleService;

    public UserController(GooglePeopleService googlePeopleService) {
        this.googlePeopleService = googlePeopleService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/googleuser";
    }

    @GetMapping("/user-info")
    @ResponseBody
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User oAuth2User) {
        return oAuth2User.getAttributes();
    }

    @GetMapping("/googleuser")
    public String getUserInfo(Model model, OAuth2AuthenticationToken authentication) {
        OAuth2User user = authentication.getPrincipal();
        Map<String, Object> attributes = user.getAttributes();
        logger.debug("User attributes: {}", attributes);

        // Check if the birthday attribute is available
        String birthday = (String) attributes.get("birthday");
        if (birthday == null) {
            // Fetch the birthday attribute using the Google People API
            birthday = googlePeopleService.getBirthday(authentication);
        }
        logger.debug("User birthday: {}", birthday);

        model.addAttribute("name", user.getAttribute("name"));
        model.addAttribute("email", user.getAttribute("email"));
        model.addAttribute("picture", user.getAttribute("picture"));
        model.addAttribute("birthday", birthday);

        String phoneNumber = googlePeopleService.getPhoneNumber(authentication);
        model.addAttribute("phone", phoneNumber);

        Map<String, Contact> contactsMap = googlePeopleService.getContactsMap(authentication);
        model.addAttribute("contactsMap", contactsMap);

        return "userinfo";
    }

    @GetMapping("/secured")
    public String secured() {
        return "Secured";
    }
}