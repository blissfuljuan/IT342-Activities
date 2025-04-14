package biacolo.com.example.OAuth2Login;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class OAuth2LoginController {

    @GetMapping("/user-info")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User principal) {
        return principal.getAttributes();

    }
    @GetMapping
    public String getMessage() {
        return "<h1>Hello, This is a Landing Page</h1>";
    }

    @GetMapping("/secured")
    public String secured() {
        return "<h1>Hello, This is a Secured Page</h1>";
    }
}
