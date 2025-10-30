package com.mccss.sso.demo.commonlib.integration;

import com.mccss.sso.demo.commonlib.dto.BoAppDto;
import com.mccss.sso.demo.commonlib.exception.ApplicationException;
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
public class BackofficeSvcClient {

    private final SecurityUtil securityUtil;
    private final WebClient backofficeClient;

    public BackofficeSvcClient(@Qualifier("backofficeClient") WebClient backofficeClient,
                               SecurityUtil securityUtil) {
        this.backofficeClient = backofficeClient;
        this.securityUtil = securityUtil;
    }

    public Mono<BoAppDto> whoamiFromBo() {
        log.info("whoami-from-bo");
        return backofficeClient
                .get()
                .uri("/whoami")
                .header(HttpHeaders.AUTHORIZATION, securityUtil.getAuthHeader())
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from Backoffice API")))
                .bodyToMono(BoAppDto.class);
    }

    public Mono<BoAppDto> callBoNoToken() {
        log.info("whoami-from-bo-no-token");
        return backofficeClient
                .get()
                .uri("/whoami")
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from Backoffice API")))
                .bodyToMono(BoAppDto.class);
    }
}
