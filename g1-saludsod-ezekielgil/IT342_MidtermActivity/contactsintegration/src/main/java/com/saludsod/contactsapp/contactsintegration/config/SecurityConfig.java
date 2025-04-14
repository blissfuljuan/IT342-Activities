package com.saludsod.contactsapp.contactsintegration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login").permitAll() // Allow public access to home & login
                .requestMatchers(HttpMethod.GET, "/contacts").authenticated() // View contacts
                .requestMatchers(HttpMethod.POST, "/contacts/add").authenticated() // Add contact
                .requestMatchers(HttpMethod.PUT, "/contacts/edit/**").authenticated() // Edit contact
                .requestMatchers(HttpMethod.POST, "/contacts/delete/**").authenticated() // Delete contact
                .anyRequest().authenticated() // Other requests require authentication
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/oauth2/authorization/google") // Redirect to Google's OAuth2 login
                .defaultSuccessUrl("/contacts", true) // Redirect to contacts after login
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // Ensure logout is triggered by visiting /logout
                .logoutSuccessUrl("/") // Redirect to home page after logout
                .invalidateHttpSession(true) // Clear session
                .deleteCookies("JSESSIONID") // Remove session cookies
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/contacts/add", "/contacts/edit/**", "/contacts/delete/**")) // Disable CSRF for these endpoints
            .sessionManagement(session -> session.sessionCreationPolicy(IF_REQUIRED)); // Maintain session for OAuth2

        return http.build();
    }
}
