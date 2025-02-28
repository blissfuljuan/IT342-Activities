package com.cabana.GoogleAPIIntegration.Controller;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GoogleContactsController {

    @GetMapping("/contacts")
    public String getContacts(Authentication authentication, Model model) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User user = ((OAuth2AuthenticationToken) authentication).getPrincipal();

            String email = "";
            String name = "";
            String picture = "";

            if (user instanceof OidcUser) {
                OidcUser oidcUser = (OidcUser) user;
                email = oidcUser.getEmail();
                name = oidcUser.getFullName();
                picture = oidcUser.getPicture();
            } else {
                email = (String) user.getAttributes().get("email");
                name = (String) user.getAttributes().get("name");
                picture = (String) user.getAttributes().get("picture");
            }

            model.addAttribute("email", email);
            model.addAttribute("name", name);
            model.addAttribute("picture", picture);
        }
        return "contacts";
    }
}