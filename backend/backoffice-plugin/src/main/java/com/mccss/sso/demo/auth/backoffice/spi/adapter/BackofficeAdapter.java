package com.mccss.sso.demo.auth.backoffice.spi.adapter;

import com.mccss.sso.demo.commonlib.integration.PermissionSvcClient;
import com.mccss.sso.demo.commonlib.model.AuthorizationBundle;
import com.mccss.sso.demo.commonlib.model.UserRoles;
import com.mccss.sso.demo.commonlib.spi.AppAuthAdapter;
import com.mccss.sso.demo.commonlib.spi.AppKey;
import com.mccss.sso.demo.commonlib.spi.BaseAuthAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@AppKey("backoffice")
@Component
public class BackofficeAdapter extends BaseAuthAdapter implements AppAuthAdapter {

    private final BackofficeMockExternalSvcClient mockExternalSvcClient;

    public BackofficeAdapter(PermissionSvcClient permissionSvcClient,
                             BackofficeMockExternalSvcClient mockExternalSvcClient) {
        super(permissionSvcClient);
        this.mockExternalSvcClient = mockExternalSvcClient;
    }

    @Override
    public String appKey() {
        return "backoffice";
    }

    @Override
    public Mono<AuthorizationBundle> buildDecisions(String issuer, String subject) {
        return super.buildDecisions(issuer, subject);
    }

    @Override
    public Mono<UserRoles> getUserRoles(String sub) {
        return mockExternalSvcClient.getUserRoles(sub);
    }

    @Override
    public Mono<Map<String, Object>> getAttributes(String sub) {
        return null;
    }
}
