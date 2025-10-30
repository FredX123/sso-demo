package com.mccss.sso.demo.commonlib.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "integration")
public class IntegrationProps {

    private Service authMs = new Service();
    private Service frontofficeMs = new Service();
    private Service backofficeMs = new Service();
    private Service permissionMs = new Service();
    private Service mockExternalMs = new Service();
    private Service sessionMs = new Service();

    @Getter
    @Setter
    public static class Service {
        private String baseUrl;
    }
}
