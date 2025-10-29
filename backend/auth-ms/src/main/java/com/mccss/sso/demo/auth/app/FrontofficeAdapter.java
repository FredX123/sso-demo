package com.mccss.sso.demo.auth.app;

import com.mccss.sso.demo.auth.integration.MockExternalSvcClient;
import com.mccss.sso.demo.commonlib.config.IntegrationProps;
import com.mccss.sso.demo.commonlib.integration.PermissionSvcClient;
import com.mccss.sso.demo.commonlib.model.AuthorizationBundle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class FrontofficeAdapter extends BaseAuthAdapter implements AppAuthAdapter {

    public FrontofficeAdapter(PermissionSvcClient permissionSvcClient,
                              MockExternalSvcClient mockExternalSvcClient,
                              IntegrationProps props) {
        super(permissionSvcClient, mockExternalSvcClient, props);
    }

    @Override
    public String appKey() {
        return "frontoffice";
    }

    @Override
    public Mono<AuthorizationBundle> buildDecisions(Jwt jwt) {
        return super.buildDecisions(jwt);
    }
}
