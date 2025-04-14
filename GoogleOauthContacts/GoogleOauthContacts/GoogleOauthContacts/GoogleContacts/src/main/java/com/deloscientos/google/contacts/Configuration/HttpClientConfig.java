package com.deloscientos.google.contacts.Configuration;

import org.apache.http.impl.client.CloseableHttpClient;

import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClients.createDefault();
    }
}