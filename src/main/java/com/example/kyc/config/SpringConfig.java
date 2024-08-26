package com.example.kyc.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SpringConfig {

    @Value("${uri.stream}")
    private String streamServerUrl;

    @Bean
    @Qualifier("streamWebClient")
    public WebClient streamWebClient(WebClient.Builder builder) {
        return builder.baseUrl(streamServerUrl)
                .build();
    }
}
