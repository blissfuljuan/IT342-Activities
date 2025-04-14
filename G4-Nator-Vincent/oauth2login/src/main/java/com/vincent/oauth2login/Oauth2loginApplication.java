package com.vincent.oauth2login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication

public class Oauth2loginApplication {

	public static void main(String[] args) {
		SpringApplication.run(Oauth2loginApplication.class, args);
	}

}
