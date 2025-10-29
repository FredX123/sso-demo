package com.mccss.sso.demo.commonlib.integration;

import com.mccss.sso.demo.commonlib.config.IntegrationProps;
import com.mccss.sso.demo.commonlib.dto.FoAppDto;
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
public class FrontofficeSvcClient {

    private final SecurityUtil securityUtil;
    private final WebClient frontofficeClient;

    public FrontofficeSvcClient(@Qualifier("frontofficeClient") WebClient frontofficeClient,
                                SecurityUtil securityUtil,
                                IntegrationProps integrationProps) {
        this.frontofficeClient = frontofficeClient;
        this.securityUtil = securityUtil;
    }

    public Mono<FoAppDto> whoamiFromFo() {
        log.info("whoami-from-fo");
        return frontofficeClient
                .get()
                .uri("/whoami")
                .header(HttpHeaders.AUTHORIZATION, securityUtil.getAuthHeader())
                .retrieve()
                .bodyToMono(FoAppDto.class);
    }

    public Mono<FoAppDto> callFoNoToken() {
        log.info("whoami-from-fo-no-token");
        return frontofficeClient
                .get()
                .uri("/whoami")
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from Frontoffice API")))
                .bodyToMono(FoAppDto.class);
    }
}
