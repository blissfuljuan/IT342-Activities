package Delima.com.example.OAuth2demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("Delima.com.example.OAuth2demo.Repository")
@ComponentScan(basePackages = "Delima.com.example.OAuth2demo")
public class OAuth2demoApplication {

	public static void main(String[] args) {
		SpringApplication.run(OAuth2demoApplication.class, args);
	}

}
