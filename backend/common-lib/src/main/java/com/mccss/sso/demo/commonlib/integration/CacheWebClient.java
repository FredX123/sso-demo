package com.mccss.sso.demo.commonlib.integration;

import com.mccss.sso.demo.commonlib.dto.AuthMe;
import com.mccss.sso.demo.commonlib.exception.ApplicationException;
import com.mccss.sso.demo.commonlib.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CacheWebClient {

    @Value("${app.authz.api.cache:}")
    private String authzCacheApi;

    private final SecurityUtil securityUtil;
    private final WebClient.Builder cacheWebClientBuilder;

    public CacheWebClient(@Qualifier("cacheWebClientBuilder") WebClient.Builder cacheWebClientBuilder,
                          SecurityUtil securityUtil) {
        this.cacheWebClientBuilder = cacheWebClientBuilder;
        this.securityUtil = securityUtil;
    }

    public Mono<AuthMe> cacheAuthz(AuthMe data) {
        return cacheWebClientBuilder.build()
                .post()
                .uri(authzCacheApi)
                .header(HttpHeaders.AUTHORIZATION, securityUtil.getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(data)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from session-ms API")))
                .bodyToMono(AuthMe.class);
    }

    public Mono<AuthMe> getAuthz() {
        return cacheWebClientBuilder.build()
                .get()
                .uri(authzCacheApi)
                .header(HttpHeaders.AUTHORIZATION, securityUtil.getAuthHeader())
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from session-ms API")))
                .bodyToMono(AuthMe.class);
    }
}
