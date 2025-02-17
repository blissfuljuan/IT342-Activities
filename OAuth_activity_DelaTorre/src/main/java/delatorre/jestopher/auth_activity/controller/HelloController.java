package delatorre.jestopher.auth_activity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/secured")
    public String secured() {
        return "Hello, secured";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, hello";
    }
}
