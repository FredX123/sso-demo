package com.mccss.sso.demo.auth.integration;

import com.mccss.sso.demo.commonlib.exception.ApplicationException;
import com.mccss.sso.demo.commonlib.model.UserRoles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ExternalClient {

    @Value("${app.mock-external-ms.base-url}")
    private String mockExternalBaseUrl;

    private final WebClient.Builder externalClientBuilder;

    public ExternalClient(@Qualifier("externalClientBuilder") WebClient.Builder externalClientBuilder) {
        this.externalClientBuilder = externalClientBuilder;
    }

    public Mono<UserRoles> getUserRoles(String subject) {
        return externalClientBuilder.build()
                .get()
                .uri(getUserRolesApi() + subject)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from Mock External API")))
                .bodyToMono(UserRoles.class);
    }

    private String getUserRolesApi() {
        return mockExternalBaseUrl + "/user-roles/";
    }
}
