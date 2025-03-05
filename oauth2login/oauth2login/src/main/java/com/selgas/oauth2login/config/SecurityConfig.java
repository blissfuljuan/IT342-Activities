package com.selgas.oauth2login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login").permitAll()  // Allow public access to home and login
                        .anyRequest().authenticated()  // All other endpoints require authentication
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/user-info", true)  // Redirect after successful OAuth login
                )
                .formLogin(formLogin -> formLogin
                        .defaultSuccessUrl("/secured", true)  // Redirect after successful form login
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")  // Redirect to home on logout
                )
                .csrf(AbstractHttpConfigurer::disable)  // Disable CSRF for testing (not recommended for production)
                .build();
    }
}
