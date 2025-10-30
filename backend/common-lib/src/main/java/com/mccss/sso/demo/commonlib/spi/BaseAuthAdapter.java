package com.mccss.sso.demo.commonlib.spi;

import com.mccss.sso.demo.commonlib.integration.PermissionSvcClient;
import com.mccss.sso.demo.commonlib.model.AuthorizationBundle;
import com.mccss.sso.demo.commonlib.model.Decision;
import com.mccss.sso.demo.commonlib.model.PermissionSet;
import com.mccss.sso.demo.commonlib.model.UserRoles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Component
public abstract class BaseAuthAdapter {

    private final PermissionSvcClient permissionSvcClient;

    protected abstract String appKey();

    protected abstract Mono<UserRoles> getUserRoles(String sub);

        protected Mono<AuthorizationBundle> buildDecisions(String issuer, String subject) {
        Mono<PermissionSet> rulesMono = permissionSvcClient.getPermissionsByApp(appKey());
        Mono<UserRoles> rolesMono = getUserRoles(subject);

        return Mono.zip(rulesMono, rolesMono).map(tuple2 -> {
            PermissionSet permissions = tuple2.getT1();
            UserRoles ur = tuple2.getT2();
            Set<String> userRoles = new HashSet<>(ur.roles() != null ? ur.roles() : List.of());

            List<Decision> decisions = permissions.rules().stream().map(rule -> {
                boolean allowed = intersects(userRoles, rule.roles());
                return new Decision(rule.action(), rule.resource(), allowed);
            }).toList();

            return new AuthorizationBundle(issuer, subject, appKey(), decisions, ur.roles());
        });
    }

    protected static boolean intersects(Set<String> userRoles, List<String> allowedRoles) {
        if (userRoles.isEmpty() || allowedRoles == null || allowedRoles.isEmpty()) {
            return false;
        }

        return allowedRoles.stream().anyMatch(userRoles::contains);
    }
}
