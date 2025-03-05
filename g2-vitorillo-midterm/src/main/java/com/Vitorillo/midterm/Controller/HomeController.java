package com.Vitorillo.midterm.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("/")
    public String home() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getAuthorities().toString().contains("ROLE_ANONYMOUS")) {
            logger.info("User is authenticated, redirecting to contacts page");
            return "redirect:/contacts";
        }
        logger.info("User is not authenticated, redirecting to login page");
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getAuthorities().toString().contains("ROLE_ANONYMOUS")) {
            logger.info("User is already authenticated, redirecting to contacts page");
            return "redirect:/contacts";
        }
        
        if (error != null) {
            logger.warn("Login error detected");
            model.addAttribute("error", "Authentication failed. Please check your credentials and try again.");
        }
        
        return "login";
    }
}
