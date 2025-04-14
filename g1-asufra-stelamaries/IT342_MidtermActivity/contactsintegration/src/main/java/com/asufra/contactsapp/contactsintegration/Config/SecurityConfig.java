package com.asufra.contactsapp.contactsintegration.Config;

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
                .requestMatchers("/images/**", "/default-profile.png").permitAll() 
                .requestMatchers("/", "/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/contacts").authenticated()
                .requestMatchers(HttpMethod.POST, "/contacts/add").authenticated()
                .requestMatchers(HttpMethod.PUT, "/contacts/edit/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/contacts/delete/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/oauth2/authorization/google")
                .defaultSuccessUrl("/contacts", true)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/contacts/add", "/contacts/edit/**", "/contacts/delete/**"))
            .sessionManagement(session -> session.sessionCreationPolicy(IF_REQUIRED));

        return http.build();
    }
}
