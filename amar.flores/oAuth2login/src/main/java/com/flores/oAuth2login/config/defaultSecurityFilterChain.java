package com.flores.oAuth2login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class defaultSecurityFilterChain {  // Renamed the class here

    // Define the security filter chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {  // Renamed the bean here
        return http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated())  // Require authentication for all requests
                .oauth2Login(oauth2 ->
                        oauth2.defaultSuccessUrl("http://localhost:8080/user-info", true))  // Redirect after successful OAuth login
                .logout(logout -> logout.logoutSuccessUrl("/"))  // Redirect to home page after logout
                .formLogin(form -> form.defaultSuccessUrl("/secured", true))  // Redirect after successful form login
                .csrf(csrf -> csrf.disable())  // Disable CSRF protection (use cautiously)
                .build();
    }
}
