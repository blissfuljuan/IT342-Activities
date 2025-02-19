package com.destura.oauthlogin;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

public class SecurityConfigJava {
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("http://localhost:8080/user-info", true))
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .formLogin(form -> form.defaultSuccessUrl("/secured", true))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
}

