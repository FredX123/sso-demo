package com.mccss.sso.demo.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppClientsProps {

    private Map<String, Client> clients = new HashMap<>();

    @Getter
    @Setter
    public static class Client {
        private String clientId;
    }
}
