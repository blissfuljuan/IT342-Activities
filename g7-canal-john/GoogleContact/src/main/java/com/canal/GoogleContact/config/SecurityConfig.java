package com.canal.GoogleContact.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain defaultSecurityChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login").permitAll()
                        .requestMatchers("/profile").authenticated()
                        .requestMatchers("/contacts/add").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth.defaultSuccessUrl("/profile", true))
                .formLogin(formLogin -> formLogin.defaultSuccessUrl("/profile", true))
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/contacts/add"))
                .build();
    }
}
