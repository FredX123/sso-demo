package com.mccss.sso.demo.permission.config;

import com.mccss.sso.demo.commonlib.model.PermissionSet;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "mock")
public class MockPermissions {
    private List<PermissionSet> permissions;
}
