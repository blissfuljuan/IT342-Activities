package com.Enriquez.GoogleAPIIntegration.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain defaultsecuritychain(HttpSecurity http) throws Exception{
        return http
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/","/login","/error", "/dashboard", "/create-contact", "/contacts/delete").permitAll() // permit all for / and /login
                    .anyRequest().authenticated()
                )
                .oauth2Login(auth -> auth
                    .defaultSuccessUrl("/dashboard", true)
                )
                .logout(logout -> logout
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                )
                .csrf().disable()
                .build();
    }}
