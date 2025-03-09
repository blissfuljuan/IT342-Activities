package com.example.demo.config;
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
