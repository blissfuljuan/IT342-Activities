package com.carbungco.GoogleContacts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity)
            throws Exception {
        return httpSecurity
                .authorizeHttpRequests(oauth -> oauth.anyRequest().authenticated())
                .oauth2Login(oauth2login -> oauth2login.defaultSuccessUrl("/contacts",true))
                // .formLogin(formLogin-> formLogin.defaultSuccessUrl("/secured",true))
                .logout(logout-> logout.logoutSuccessUrl("/"))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri("https://accounts.google.com/.well-known/openid-configuration").build();

        // Allow 5 minutes of clock skew
        jwtDecoder.setJwtValidator(new JwtTimestampValidator(Duration.ofMinutes(5)));

        return jwtDecoder;
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}