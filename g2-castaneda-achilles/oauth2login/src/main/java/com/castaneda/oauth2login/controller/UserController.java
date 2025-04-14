package com.castaneda.oauth2login.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/user-info")
    public Map<String,Object> getUserProfile(@AuthenticationPrincipal OAuth2User oAuth2User){
        return oAuth2User.getAttributes();
    }

    @GetMapping("/secured")
    public String securedEndPoint(){
        return "<h1> This is a secured endpoint <h1>";
    }


    @GetMapping
    public String index(){
        return "<html>" +
                "<head>" +
                "<style>" +
                "body {" +
                "  font-family: 'Arial', sans-serif;" +
                "  background: linear-gradient(to right, #6a11cb, #2575fc);" +
                "  height: 100vh;" +
                "  display: flex;" +
                "  justify-content: center;" +
                "  align-items: center;" +
                "  margin: 0;" +
                "  color: white;" +
                "  text-align: center;" +
                "}" +
                "h1 {" +
                "  font-size: 3rem;" +
                "  font-weight: bold;" +
                "  margin-bottom: 20px;" +
                "  animation: colorChange 5s infinite;" +
                "}" +
                "p {" +
                "  font-size: 1.2rem;" +
                "  margin-bottom: 30px;" +
                "  font-weight: lighter;" +
                "}" +
                "button {" +
                "  background-color: #ffffff;" +
                "  color: #2575fc;" +
                "  border: none;" +
                "  padding: 12px 20px;" +
                "  font-size: 1rem;" +
                "  border-radius: 25px;" +
                "  cursor: pointer;" +
                "  transition: background-color 0.3s ease;" +
                "}" +
                "button:hover {" +
                "  background-color: #2575fc;" +
                "  color: white;" +
                "}" +
                "@keyframes colorChange {" +
                "  0% { color: #ff6347; }" +
                "  25% { color: #ff9900; }" +
                "  50% { color: #32cd32; }" +
                "  75% { color: #1e90ff; }" +
                "  100% { color: #9932cc; }" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div>" +
                "<h1>Welcome!</h1>" +
                "<p>This is the landing page.</p>" +
                "<button onclick=\"window.location.href='https://www.youtube.com/shorts/t7e0yoRUxq8';\">Click Me! (^-^) <333</button>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
