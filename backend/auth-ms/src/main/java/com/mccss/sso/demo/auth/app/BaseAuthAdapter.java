package com.mccss.sso.demo.auth.app;

import com.mccss.sso.demo.commonlib.config.IntegrationProps;
import com.mccss.sso.demo.auth.integration.ExternalClient;
import com.mccss.sso.demo.commonlib.integration.PermissionClient;
import com.mccss.sso.demo.commonlib.model.AuthorizationBundle;
import com.mccss.sso.demo.commonlib.model.Decision;
import com.mccss.sso.demo.commonlib.model.PermissionSet;
import com.mccss.sso.demo.commonlib.model.UserRoles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Component
public abstract class BaseAuthAdapter {

    private final PermissionClient permissionClient;
    private final ExternalClient externalClient;
    private final IntegrationProps props;

    public abstract String appKey();

    protected Mono<AuthorizationBundle> buildDecisions(Jwt jwt) {
        String iss = jwt.getIssuer().toString();
        String sub = jwt.getSubject();

        Mono<PermissionSet> rulesMono = permissionClient.getPermissionsByApp(appKey());
        Mono<UserRoles> rolesMono = externalClient.getUserRoles(sub);

        return Mono.zip(rulesMono, rolesMono).map(tuple2 -> {
            PermissionSet permissions = tuple2.getT1();
            UserRoles ur = tuple2.getT2();
            Set<String> userRoles = new HashSet<>(ur.roles() != null ? ur.roles() : List.of());

            List<Decision> decisions = permissions.rules().stream().map(rule -> {
                boolean allowed = intersects(userRoles, rule.roles());
                return new Decision(rule.action(), rule.resource(), allowed);
            }).toList();

            return new AuthorizationBundle(iss, sub, appKey(), decisions, ur.roles(), props.getDecisions().getTtlSec());
        });
    }

    private static boolean intersects(Set<String> userRoles, List<String> allowedRoles) {
        if (userRoles.isEmpty() || allowedRoles == null || allowedRoles.isEmpty()) {
            return false;
        }

        return allowedRoles.stream().anyMatch(userRoles::contains);
    }
}
