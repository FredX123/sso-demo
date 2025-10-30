package com.mccss.sso.demo.auth.frontoffice.spi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "integration.mock-external-ms")
public class FrontofficeMockExternalProps {
    private String baseUrl;
}
