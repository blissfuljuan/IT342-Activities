package com.catulong.oauth2login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.catulong.oauth2login"})
public class Oauth2loginApplication {

	public static void main(String[] args) {
		SpringApplication.run(Oauth2loginApplication.class, args);
	}

}
