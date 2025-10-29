package com.mccss.sso.demo.auth.integration;

import com.mccss.sso.demo.commonlib.exception.ApplicationException;
import com.mccss.sso.demo.commonlib.model.UserRoles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class MockExternalSvcClient {

    private final WebClient mockExternalClient;

    public MockExternalSvcClient(@Qualifier("mockExternalClient") WebClient mockExternalClient) {
        this.mockExternalClient = mockExternalClient;
    }

    public Mono<UserRoles> getUserRoles(String subject) {
        log.info("Getting user roles for subject: {} from Mock External Service", subject);
        return mockExternalClient
                .get()
                .uri("/user-roles/" + subject)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from Mock External API")))
                .bodyToMono(UserRoles.class);
    }
}
