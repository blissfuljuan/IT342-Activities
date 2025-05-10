package com.ligan.googlecontact.peopleintegration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    private static class HttpComponentsClientHttpRequestFactoryBasicAuth extends SimpleClientHttpRequestFactory {
        @Override
        protected void prepareConnection(java.net.HttpURLConnection connection, String httpMethod) throws java.io.IOException {
            if ("PATCH".equals(httpMethod)) {
                connection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                connection.setRequestMethod("POST");
            } else {
                connection.setRequestMethod(httpMethod);
            }
            super.prepareConnection(connection, httpMethod);
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactoryBasicAuth());
    }
}