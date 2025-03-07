package com.example.gadiane.johnkarl.demolition.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/").permitAll()
                                .requestMatchers("/webjars/**", "/css/**", "/js/**", "/images/**").permitAll()
                                .requestMatchers("/api/contacts/**").authenticated()
                                .requestMatchers("/contacts/**").authenticated()
                                .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/contacts", true)
                        .failureUrl("/login?error=true"))
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))
                .formLogin(form -> form.defaultSuccessUrl("/contacts", true))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .build();
    }
}