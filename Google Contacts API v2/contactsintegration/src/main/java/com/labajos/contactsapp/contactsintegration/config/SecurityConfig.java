package com.labajos.contactsapp.contactsintegration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import static org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home", "/login").permitAll() // Allow public access to home & login
                .requestMatchers(HttpMethod.GET, "/contacts").authenticated() // View contacts
                .requestMatchers(HttpMethod.POST, "/contacts/add").authenticated() // Add contact
                .requestMatchers(HttpMethod.PUT, "/contacts/edit/**").authenticated() // Edit contact
                .requestMatchers(HttpMethod.DELETE, "/contacts/delete/**").authenticated() // Delete contact
                .anyRequest().authenticated() // Other requests require authentication
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/oauth2/authorization/google") // Redirect to Google's OAuth2 login
                .defaultSuccessUrl("/contacts", true) // Redirect to contacts after login
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // Logout endpoint
                .logoutSuccessUrl("/") // Redirect to login page after logout
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID") // Ensure session is fully cleared
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/contacts/add", "/contacts/edit/**", "/contacts/delete/**")) // Disable CSRF for these endpoints
            .sessionManagement(session -> session.sessionCreationPolicy(IF_REQUIRED)); // Maintain session for OAuth2

        return http.build();
    }
}