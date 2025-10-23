package com.mccss.sso.demo.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.authz")
public class AuthzProps {

    private Mock mock = new Mock();

    @Getter
    @Setter
    public static class Mock {
        private Claims claims = new Claims();
    }

    @Getter
    @Setter
    public static class Claims {
        private List<String> roles;
        private List<String> permissions;
    }
}
