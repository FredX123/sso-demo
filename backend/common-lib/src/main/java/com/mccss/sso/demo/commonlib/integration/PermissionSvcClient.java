package com.mccss.sso.demo.commonlib.integration;

import com.mccss.sso.demo.commonlib.exception.ApplicationException;
import com.mccss.sso.demo.commonlib.model.PermissionSet;
import com.mccss.sso.demo.commonlib.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PermissionSvcClient {

    private final SecurityUtil securityUtil;
    private final WebClient permissionClient;

    public PermissionSvcClient(@Qualifier("permissionSvcClient") WebClient permissionClient,
                               SecurityUtil securityUtil) {
        this.permissionClient = permissionClient;
        this.securityUtil = securityUtil;
    }

    public Mono<PermissionSet> getPermissionsByApp(String appKey) {
        log.info("Getting RBAC permissions for app {}", appKey);
        return permissionClient
                .get()
                .uri("/" + appKey)
                .header(HttpHeaders.AUTHORIZATION, securityUtil.getAuthHeader())
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from Backoffice API")))
                .bodyToMono(PermissionSet.class);
    }
}
