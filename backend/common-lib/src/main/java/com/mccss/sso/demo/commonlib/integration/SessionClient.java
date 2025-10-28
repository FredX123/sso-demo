package com.mccss.sso.demo.commonlib.integration;

import com.mccss.sso.demo.commonlib.config.IntegrationProps;
import com.mccss.sso.demo.commonlib.exception.ApplicationException;
import com.mccss.sso.demo.commonlib.model.UserSession;
import com.mccss.sso.demo.commonlib.util.SecurityUtil;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SessionClient {

    private final WebClient.Builder sessionClientBuilder;
    private final SecurityUtil securityUtil;
    private final IntegrationProps integrationProps;

    public SessionClient(@Qualifier("sessionClientBuilder") WebClient.Builder sessionClientBuilder,
                         SecurityUtil securityUtil,
                         IntegrationProps integrationProps) {
        this.sessionClientBuilder = sessionClientBuilder;
        this.securityUtil = securityUtil;
        this.integrationProps = integrationProps;
    }

    public Mono<UserSession> cacheUserSession(UserSession userSession, @Nullable String bearer) {
        String bearerToken = securityUtil.getAuthHeader();
        return sessionClientBuilder.build()
                .post()
                .uri(integrationProps.getSessionMs().getBaseUrl() + "/cache/user-session")
                .headers(h -> setBearer(h, bearer))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userSession)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from session-ms API")))
                .bodyToMono(UserSession.class);
    }

    public Mono<UserSession> getUserSession(@Nullable String bearer) {
        return sessionClientBuilder.build()
                .get()
                .uri(integrationProps.getSessionMs().getBaseUrl() + "/cache/user-session")
                .headers(h -> setBearer(h, bearer))
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from session-ms API")))
                .bodyToMono(UserSession.class);
    }

    private static void setBearer(HttpHeaders h, @Nullable String bearer) {
        if (bearer != null && !bearer.isBlank()) {
            h.set(HttpHeaders.AUTHORIZATION, bearer.startsWith("Bearer ") ? bearer : "Bearer " + bearer);
        }
    }
}
