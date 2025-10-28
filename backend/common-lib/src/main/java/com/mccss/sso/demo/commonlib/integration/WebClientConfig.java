package com.mccss.sso.demo.commonlib.integration;

import com.mccss.sso.demo.commonlib.config.IntegrationProps;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @Qualifier("foClientBuilder")
    public WebClient.Builder foClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @Qualifier("boClientBuilder")
    public WebClient.Builder boClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @Qualifier("sessionClientBuilder")
    public WebClient.Builder sessionClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @Qualifier("permissionClientBuilder")
    public WebClient.Builder permissionClientBuilder(IntegrationProps props) {
        return WebClient.builder().baseUrl(props.getPermissionMs().getBaseUrl());
    }

    @Bean @Qualifier("externalClientBuilder")
    public WebClient.Builder externalClientBuilder(IntegrationProps props) {
        return WebClient.builder().baseUrl(props.getMockExternalMs().getBaseUrl());
    }

}
