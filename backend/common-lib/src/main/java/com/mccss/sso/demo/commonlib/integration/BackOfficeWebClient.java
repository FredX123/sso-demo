package com.mccss.sso.demo.commonlib.integration;

import com.mccss.sso.demo.commonlib.dto.BoAppDto;
import com.mccss.sso.demo.commonlib.exception.ApplicationException;
import com.mccss.sso.demo.commonlib.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class BackOfficeWebClient {

    @Value("${url.backoffice.api.whoami:}")
    private String boWhoAmIApi;

    private final SecurityUtil securityUtil;
    private final WebClient.Builder boWebClientBuilder;

    public BackOfficeWebClient(@Qualifier("boWebClientBuilder") WebClient.Builder boWebClientBuilder,
                               SecurityUtil securityUtil) {
        this.boWebClientBuilder = boWebClientBuilder;
        this.securityUtil = securityUtil;
    }

    public Mono<BoAppDto> whoamiFromBo() {
        return boWebClientBuilder.build()
                .get()
                .uri(boWhoAmIApi)
                .header(HttpHeaders.AUTHORIZATION, securityUtil.getAuthHeader())
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from Backoffice API")))
                .bodyToMono(BoAppDto.class);
    }

    public Mono<BoAppDto> callBoNoToken() {
        return boWebClientBuilder.build()
                .get()
                .uri(boWhoAmIApi)
                .retrieve()
                .bodyToMono(BoAppDto.class);
    }
}
