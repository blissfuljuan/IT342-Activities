package com.tuban.oauth2Login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity

public class SecurityConfig {

    @Bean
    public SecurityFilterChain defaultSecurityChain(HttpSecurity http) throws Exception{
        return http
                .authorizeHttpRequests(oaut-> oaut.anyRequest().authenticated())
                .oauth2Login(oauth2login->oauth2login.defaultSuccessUrl("/user-info",true))
                .formLogin(formlogin->formlogin.defaultSuccessUrl("/secured",true))
                .logout(logout->logout.logoutSuccessUrl("/"))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
    @Bean
    public UserDetailsService userDetailsService(){
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);

    }

}
