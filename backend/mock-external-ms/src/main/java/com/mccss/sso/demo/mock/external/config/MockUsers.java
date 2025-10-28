package com.mccss.sso.demo.mock.external.config;

import com.mccss.sso.demo.commonlib.model.UserRoles;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "mock")
public class MockUsers {
    private List<UserRoles> users;
}
