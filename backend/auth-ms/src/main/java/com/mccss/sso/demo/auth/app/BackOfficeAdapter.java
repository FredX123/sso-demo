package com.mccss.sso.demo.auth.app;

import com.mccss.sso.demo.commonlib.config.IntegrationProps;
import com.mccss.sso.demo.auth.integration.ExternalClient;
import com.mccss.sso.demo.commonlib.integration.PermissionClient;
import com.mccss.sso.demo.commonlib.model.AuthorizationBundle;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class BackOfficeAdapter extends BaseAuthAdapter implements AppAuthAdapter {

    public BackOfficeAdapter(PermissionClient permissionClient,
                             ExternalClient externalClient,
                             IntegrationProps props) {
        super(permissionClient, externalClient, props);
    }

    @Override
    public String appKey() {
        return "backoffice";
    }

    @Override
    public Mono<AuthorizationBundle> buildDecisions(Jwt jwt) {
        return super.buildDecisions(jwt);
    }
}
