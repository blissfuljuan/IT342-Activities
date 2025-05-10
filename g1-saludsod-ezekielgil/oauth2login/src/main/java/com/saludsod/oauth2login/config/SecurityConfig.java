package com.saludsod.oauth2login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
        .authorizeHttpRequests(auth -> auth.anyRequest(). authenticated())
        .oauth2Login(auth -> auth.defaultSuccessUrl("/user-info", true))
        .logout(Logout -> Logout.logoutSuccessUrl("/"))
        .build();
    }
}
