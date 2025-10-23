package com.mccss.sso.demo.commonlib.integration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @Qualifier("foWebClientBuilder")
    public WebClient.Builder foWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @Qualifier("boWebClientBuilder")
    public WebClient.Builder boWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @Qualifier("cacheWebClientBuilder")
    public WebClient.Builder cacheWebClientBuilder() {
        return WebClient.builder();
    }
}
