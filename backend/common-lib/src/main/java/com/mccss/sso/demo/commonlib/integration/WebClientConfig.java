package com.mccss.sso.demo.commonlib.integration;

import com.mccss.sso.demo.commonlib.config.IntegrationProps;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Configuration
public class WebClientConfig {

    private final IntegrationProps integrationProps;

    @Bean
    @Qualifier("frontofficeClient")
    public WebClient frontofficeClient() {
        return WebClient.builder().baseUrl(integrationProps.getFrontofficeMs().getBaseUrl()).build();
    }

    @Bean
    @Qualifier("backofficeClient")
    public WebClient backofficeClient() {
        return WebClient.builder().baseUrl(integrationProps.getBackofficeMs().getBaseUrl()).build();
    }

    @Bean
    @Qualifier("sessionClient")
    public WebClient sessionClient() {
        return WebClient.builder().baseUrl(integrationProps.getSessionMs().getBaseUrl()).build();
    }

    @Bean
    @Qualifier("permissionSvcClient")
    public WebClient permissionClient() {
        return WebClient.builder().baseUrl(integrationProps.getPermissionMs().getBaseUrl()).build();
    }

    @Bean @Qualifier("mockExternalClient")
    public WebClient mockExternalClient() {
        return WebClient.builder().baseUrl(integrationProps.getMockExternalMs().getBaseUrl()).build();
    }

}
