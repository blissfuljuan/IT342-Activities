package com.Lacaba.oauth2login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.Map;

@SpringBootApplication
public class OAuth2loginApplication {

	public static void main(String[] args) {
		SpringApplication.run(OAuth2loginApplication.class, args);

	}


}
