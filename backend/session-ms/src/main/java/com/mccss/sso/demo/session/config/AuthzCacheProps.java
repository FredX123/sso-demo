package com.mccss.sso.demo.session.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.authz.cahe")
public class AuthzCacheProps {

    private Duration ttl = Duration.ofMinutes(15);
    private boolean hashKey = false;
}
