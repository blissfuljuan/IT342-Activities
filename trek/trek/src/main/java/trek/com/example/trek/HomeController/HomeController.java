package trek.com.example.trek.HomeController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
@RestController
public class HomeController {

    @GetMapping("/user-info")
    public String getUserInfo(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            return "User Info: " + principal.getAttributes().toString();
        } else {
            return "No user information available.";
        }
    }
    @GetMapping("/message")
    public String getMessage() {
        return "Hello, Spring Boot!";
    }
}
