package com.mccss.sso.demo.commonlib.integration;

import com.mccss.sso.demo.commonlib.exception.ApplicationException;
import com.mccss.sso.demo.commonlib.model.UserSession;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SessionSvcClient {

    private final WebClient sessionClient;

    public SessionSvcClient(@Qualifier("sessionClient") WebClient sessionClient) {
        this.sessionClient = sessionClient;
    }

    public Mono<UserSession> cacheUserSession(UserSession userSession, @Nullable String bearer) {
        log.info("Caching user session data for application: {}", userSession.getAuthz().app());
        return sessionClient
                .post()
                .uri("/cache/user-session")
                .headers(h -> setBearer(h, bearer))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userSession)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from session-ms API")))
                .bodyToMono(UserSession.class);
    }

    public Mono<UserSession> getUserSession(@Nullable String bearer, String app) {
        log.info("Getting user session data for application: {}", app);
        return sessionClient
                .get()
                .uri("/cache/user-session")
                .headers(h -> {
                    setBearer(h, bearer);
                    h.set("X-App", app);
                })
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from session-ms API")))
                .bodyToMono(UserSession.class);
    }

    private static void setBearer(HttpHeaders headers, @Nullable String bearer) {
        if (StringUtils.isNotBlank(bearer)) {
            headers.set(HttpHeaders.AUTHORIZATION, bearer.startsWith("Bearer ") ? bearer : "Bearer " + bearer);
        }
    }
}
