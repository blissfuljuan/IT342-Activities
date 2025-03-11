package com.vijar.oauth2login.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/user-info").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .defaultSuccessUrl("/user-info", true)
                        .userInfoEndpoint(userInfo -> userInfo.userService(this.customOAuth2UserService()))
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // Set logout URL
                        .logoutSuccessUrl("/login") // Redirect to /login after logout
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID") // Optional: clear cookies
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/user-info", true)
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService() {
        return userRequest -> {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            OAuth2User oauthUser;

            // Check if the provider supports OpenID Connect (OIDC)
            if ("google".equals(registrationId) && userRequest.getClientRegistration().getScopes().contains("openid")) {
                // Use OidcUserService for OIDC providers
                OidcUserService oidcUserService = new OidcUserService();
                oauthUser = oidcUserService.loadUser((OidcUserRequest) userRequest);
            } else {
                // Use DefaultOAuth2UserService for non-OIDC providers
                DefaultOAuth2UserService defaultService = new DefaultOAuth2UserService();
                oauthUser = defaultService.loadUser(userRequest);
            }

            return oauthUser;
        };
    }


}