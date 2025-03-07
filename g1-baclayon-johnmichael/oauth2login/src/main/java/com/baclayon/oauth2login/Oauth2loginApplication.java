package com.baclayon.oauth2login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class Oauth2loginApplication {

	public static void main(String[] args) {
		SpringApplication.run(Oauth2loginApplication.class, args);
	}
}