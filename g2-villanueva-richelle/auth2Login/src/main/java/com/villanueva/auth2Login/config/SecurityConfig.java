package com.villanueva.auth2Login.config;

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
    public SecurityFilterChain defaultSecurityChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(oauth -> oauth.anyRequest().authenticated())
                .oauth2Login(oauth2Login -> oauth2Login.defaultSuccessUrl("/user-info", true))
                .formLogin(formLogin -> formLogin.defaultSuccessUrl("/secured", true))
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }


}
