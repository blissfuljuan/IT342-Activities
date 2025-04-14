package com.gitgano.midterm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/user-info").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .defaultSuccessUrl("/user-info", true)
                        .userInfoEndpoint(userInfo -> userInfo.userService(this.customOAuth2UserService()))
                )
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .formLogin(form -> form.defaultSuccessUrl("/user-info", true));

        return http.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService() {
        return userRequest -> {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            OAuth2User oauthUser;

            if ("google".equals(registrationId)) {
                OidcUserService oidcUserService = new OidcUserService();
                OidcUser oidcUser = oidcUserService.loadUser((org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest) userRequest);
                oauthUser = oidcUser; // Assign OidcUser to OAuth2User variable
            } else {
                DefaultOAuth2UserService defaultService = new DefaultOAuth2UserService();
                oauthUser = defaultService.loadUser(userRequest);
            }

            // Extract and print access token for debugging
            String accessToken = userRequest.getAccessToken().getTokenValue();
            System.out.println("Access Token: " + accessToken);

            return oauthUser; // Return the OAuth2User
        };
    }

}

