package edu.cit.CIT_UCommunityForums.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;



@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated()) // Require authentication for all endpoints
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("http://localhost:8080/user-info", true)) // Redirect user after successful login
                .logout(logout -> logout
                        .logoutSuccessUrl("/")) // Redirect after logout
                .formLogin(form -> form
                        .defaultSuccessUrl("/secured", true)) // Redirect after form login
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF (optional)
                .build();
    }
}
